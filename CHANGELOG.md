# DungeonForge 迭代开发记录

## 一、macOS 兼容性修复

### 问题
项目原始代码无法在 macOS 上运行，存在多个环境和配置问题。

### 修改点

#### 1.1 gradlew 脚本替换
- **文件**: `gradlew`
- **问题**: 原脚本依赖 PowerShell，macOS 无法执行
- **修改**: 替换为标准 POSIX shell 脚本，在 macOS 上自动检测本机 JDK，不再要求把某台机器的绝对路径写进项目配置

#### 1.2 gradle-wrapper.jar 下载
- **文件**: `gradle/wrapper/gradle-wrapper.jar`
- **问题**: 项目缺少此文件，Gradle 无法启动
- **修改**: 从 Gradle 官方仓库下载 v9.1.0 版本的 wrapper jar

#### 1.3 JDK 路径配置
- **文件**: `gradle.properties`
- **修改**: 移除 `org.gradle.java.home` 这类机器专属绝对路径，避免 Windows 和 macOS 互相污染
- **说明**: macOS 由 `gradlew` 自动检测本机 JDK；Windows 使用本机 `JAVA_HOME` 或 PATH 中的 Java

#### 1.4 LWJGL3 macOS 启动参数
- **文件**: `lwjgl3/build.gradle`
- **问题**: macOS 上 LWJGL3/OpenGL 需要 `-XstartOnFirstThread` JVM 参数
- **修改**: 在 run task 中添加该 JVM 参数

#### 1.5 StartupHelper macOS 重启机制
- **文件**: `lwjgl3/src/main/java/.../StartupHelper.java`
- **修改**: 实现 macOS 下自动检测并以 `-XstartOnFirstThread` 参数重启 JVM 的逻辑

---

## 二、性能优化

### 问题
游戏运行时每帧产生大量临时对象（`new Rectangle()`、`new Vector2()`），触发频繁 GC，影响流畅度。

### 修改点

#### 2.1 Entity 缓存 getBounds/getCenter
- **文件**: `core/.../entity/Entity.java`
- **修改**: 添加 `cachedBounds`（Rectangle）和 `cachedCenter`（Vector2）字段，`getBounds()` 和 `getCenter()` 方法改为复用这些实例而非每次 `new`
- **效果**: 消除约 100+ 次/帧的对象分配

```java
private final Rectangle cachedBounds = new Rectangle();
private final Vector2 cachedCenter = new Vector2();
public Rectangle getBounds() { return cachedBounds.set(position.x, position.y, size.x, size.y); }
public Vector2 getCenter() { return cachedCenter.set(position.x + size.x * 0.5f, position.y + size.y * 0.5f); }
```

#### 2.2 DungeonMap 越界哨兵
- **文件**: `core/.../world/DungeonMap.java`
- **修改**: 添加 `private static final Tile OUT_OF_BOUNDS = new Tile(TileType.WALL)` 静态哨兵对象，替代每次越界访问时 `new Tile()`

#### 2.3 Room.overlaps() 内联 AABB
- **文件**: `core/.../world/Room.java`
- **修改**: 将 `overlaps()` 从创建 2 个临时 Rectangle 改为内联 AABB 碰撞数学计算，移除 Rectangle import

#### 2.4 CollisionSystem 临时边界
- **文件**: `core/.../system/CollisionSystem.java`
- **修改**: 添加 `private final Rectangle tmpBounds = new Rectangle()` 避免修改 Entity 的缓存边界

#### 2.5 CombatSystem 临时向量
- **文件**: `core/.../system/CombatSystem.java`
- **修改**: 添加 `tmpFacing`、`tmpDirection`、`tmpKnockback` 三个 Vector2 字段，替代战斗计算中所有 `new Vector2()` 调用

#### 2.6 AttackEffect 内联计算
- **文件**: `core/.../entity/AttackEffect.java`
- **修改**: 构造函数中的法向量计算改为内联，避免临时 Vector2

#### 2.7 PathfindingSystem 代数计数器
- **文件**: `core/.../system/PathfindingSystem.java`
- **修改**: 将 `Arrays.fill()` 清空 3×9600 元素数组的方式改为代数计数器（`openedGen[]`、`closedGen[]`、`generation++`），每次搜索只需 `generation++` 而非清空数组
- **效果**: 消除每次寻路 28,800 次数组赋值

---

## 三、迷雾系统重写

### 问题
原始迷雾系统使用简单的圆形范围检查，墙壁无法遮挡视线，玩家可以"透视"墙后区域。

### 修改点

#### 3.1 递归阴影投射算法
- **文件**: `core/.../system/FogOfWarSystem.java`
- **修改**: 完全重写为 8 八分区递归阴影投射（Recursive Shadowcasting）算法
- **效果**: 墙壁正确阻挡视线，探索过但不可见的区域显示为半透明迷雾

---

## 四、楼层切换安全重构

### 问题
`nextFloor()` 方法通过手动复制 11 个玩家字段来保持状态，容易遗漏导致 bug。

### 修改点

#### 4.1 Player.State 模式
- **文件**: `core/.../entity/Player.java`
- **修改**: 添加 `State` 内部类和 `saveState()`/`restoreState()` 方法，将脆弱的字段逐一复制改为结构化的状态保存/恢复

```java
public static class State {
    public int hp, maxHp, attack, defense, level, exp, gold, keys, potions;
    public WeaponType weapon;
    public final Array<RelicType> relics = new Array<>();
}
```

#### 4.2 GameWorld.nextFloor() 简化
- **文件**: `core/.../world/GameWorld.java`
- **修改**: `nextFloor()` 改为 `Player.State saved = player.saveState(); loadFloor(); player.restoreState(saved);`

---

## 五、难度选择系统

### 背景
游戏原本没有任何难度设定，所有玩家体验相同的挑战。

### 修改点

#### 5.1 Difficulty 枚举
- **文件**: `core/.../util/Difficulty.java`（新建）
- **内容**: 定义 EASY / NORMAL / HARD 三档难度，包含以下参数：

| 参数 | Easy | Normal | Hard |
|------|------|--------|------|
| 玩家血量倍率 | ×1.3 | ×1.0 | ×0.8 |
| 敌人HP倍率 | ×0.85 | ×1.1 | ×1.5 |
| 敌人伤害倍率 | ×0.80 | ×1.1 | ×1.5 |
| 初始药水 | 3瓶 | 2瓶 | 1瓶 |
| 陷阱伤害倍率 | ×0.6 | ×1.0 | ×1.5 |
| 刷怪间隔倍率 | ×1.2 | ×0.85 | ×0.55 |
| 额外最大怪物数 | +0 | +2 | +4 |
| 每层敌人HP成长 | +12%/层 | +18%/层 | +25%/层 |
| 每层敌人伤害成长 | +8%/层 | +14%/层 | +20%/层 |

- 提供 `next()` 和 `prev()` 方法用于循环切换

#### 5.2 DungeonForgeGame 集成
- **文件**: `core/.../DungeonForgeGame.java`
- **修改**: 添加 `public Difficulty difficulty = Difficulty.NORMAL;` 字段

#### 5.3 GameWorld 难度应用
- **文件**: `core/.../world/GameWorld.java`
- **修改**:
  - 添加 `public final Difficulty difficulty` 字段
  - 新增构造函数 `GameWorld(Sfx, WeaponType, Difficulty)`
  - `loadFloor()` 中应用 `difficulty.playerHpMultiplier` 到玩家 maxHp/hp，设置 `difficulty.startingPotions`
  - 陷阱伤害乘以 `difficulty.trapDamageMultiplier`
  - `addEnemy()` 辅助方法应用 `difficulty.enemyHpMultiplier` 和 `difficulty.enemyDamageMultiplier`
  - 箱子交互距离从 48f 扩大至 56f
  - 楼梯交互距离从 40f 扩大至 52f

#### 5.4 Enemy 成长率改用 Difficulty 参数
- **文件**: `core/.../entity/Enemy.java`
- **修改**:
  - 新增构造函数接受 `Difficulty` 参数
  - HP 成长率从固定 `floor * 0.12f` 改为 `floor * difficulty.enemyHpGrowth`
  - 伤害成长率从固定 `floor * 0.08f` 改为 `floor * difficulty.enemyDamageGrowth`
  - 新增速度成长：`speed * (1f + floor * 0.02f)`，高层怪移动更快

#### 5.5 SpawnerSystem 难度集成
- **文件**: `core/.../system/SpawnerSystem.java`
- **修改**:
  - 刷怪间隔乘以 `world.difficulty.spawnIntervalMultiplier`
  - 最大怪物数加上 `world.difficulty.extraMaxEnemies`
  - 创建敌人时传入 `world.difficulty`

#### 5.6 主菜单难度选择器
- **文件**: `core/.../screen/MainMenuScreen.java`
- **修改**:
  - 新增 `diffButton` 矩形区域（位于 Start Run 按钮上方）
  - 点击按钮或按左/右方向键循环切换 Easy → Normal → Hard
  - 只显示短难度名，移除特殊箭头符号和描述长句，避免字体缺字和文字错位

#### 5.7 暂停界面显示难度
- **文件**: `core/.../screen/GameScreen.java`
- **修改**: 暂停面板标题 "PAUSED" 下方显示当前难度名称（对应颜色）

#### 5.8 结算界面显示难度
- **文件**: `core/.../screen/GameOverScreen.java`
- **修改**: 结算界面改为与主菜单、暂停界面一致的暗色面板样式，显示短统计、当前难度和可点击 Retry/Menu 按钮

---

## 六、UX 改进——物品标签和交互提示

### 背景
用户反馈："放在地上的棕色东西玩了好几局都没弄明白是干嘛的"，"有的箱子按什么键都没反应"。

### 修改点

#### 6.1 物品浮动标签
- **文件**: `core/.../render/WorldRenderer.java`
- **修改**: 新增 `drawPrompts()` 方法，在 `drawDamageTexts()` 之后调用
- **效果**: 玩家靠近物品（100 单位内）时，物品上方显示金色文字标签：
  - COIN → "Gold"
  - POTION → "Potion"
  - SWORD_UPGRADE → "+ATK"
  - ARMOR_UPGRADE → "+DEF"
  - KEY → "Key"
  - RELIC → 遗物具体名称（如 "Crit Charm"）
  - WEAPON → 武器具体名称（如 "Dagger"）

#### 6.2 装饰物标签
- **文件**: `core/.../render/WorldRenderer.java`
- **修改**: `drawPrompts()` 中为靠近的装饰物（80 单位内）显示灰色标签：
  - BARREL → "Barrel"
  - CRATE → "Crate"
  - TORCH → "Torch"
  - RUBBLE → "Rubble"
- **效果**: 玩家一眼就能分清装饰物（灰色标签）和可拾取物品（金色标签）

#### 6.3 箱子交互提示
- **文件**: `core/.../render/WorldRenderer.java`
- **修改**: 玩家靠近未开启的箱子（70 单位内）时显示提示：
  - 普通箱子：绿色 "Press E"
  - Boss 箱子且 Boss 存活：红色 "Defeat Boss"

#### 6.4 楼梯交互提示
- **文件**: `core/.../render/WorldRenderer.java`
- **修改**: 玩家靠近楼梯（70 单位内）时显示提示：
  - 有钥匙且无 Boss：绿色 "Press E"
  - 无钥匙：金色 "Need Key"
  - Boss 存活：红色 "Defeat Boss"

#### 6.5 楼梯交互不再阻断游戏循环
- **文件**: `core/.../world/GameWorld.java`
- **修改**: 原代码中，玩家在楼梯旁按 E 但没钥匙/Boss 存活时，`return` 会跳过整帧的所有更新（移动、战斗、AI 等）。改为使用 `usedStairs` 布尔标记，仅在成功使用楼梯时跳过后续更新

---

## 七、射箭自动瞄准

### 背景
按 K 射箭原本朝鼠标方向射出，纯键盘玩家难以控制方向。用户建议自动瞄准最近的敌人。

### 修改点

#### 7.1 自动瞄准最近敌人
- **文件**: `core/.../system/CombatSystem.java`
- **修改**:
  - 新增 `findNearestEnemy(GameWorld, Vector2)` 方法，遍历所有存活敌人找最近的
  - `playerArrowSkill()` 逻辑改为：
    1. 鼠标右键触发时，始终朝鼠标方向射箭
    2. 键盘 K 触发时，优先自动朝最近敌人射箭
    3. K 触发且无敌人时，朝当前朝向射箭

#### 7.2 近战/远程伤害区分
- **文件**: `core/.../entity/Player.java`
- **修改**: 箭矢伤害改为基于近战伤害折算，并封顶低于近战伤害，保证近战攻击值大于远程攻击值

---

## 八、难度平衡调整

### 背景
用户反馈即使 Hard 难度也太简单，提出：增加怪物数量、不要总是同一种小兵、随层数递增强怪、钥匙太容易获得。

### 修改点

#### 8.1 三难度全面调参
- **文件**: `core/.../util/Difficulty.java`
- **修改**: 见上方 5.1 的最终参数表。所有三个难度都做了调整，不仅仅是 Hard。Normal 模式也变得比原来更有挑战性（敌人 HP/伤害 ×1.1、刷怪间隔 ×0.85、多 2 个怪物位）

#### 8.2 敌人比例连续公式
- **文件**: `core/.../world/BiomeType.java`
- **修改**: 将原来的固定分段（1-2层/3-5层/6层+）改为连续数学公式：
  - 史莱姆比例：`max(0.05, 0.50 - floor × 0.07)`
  - 骷髅比例：`min(0.75, 0.12 + floor × 0.08)`
  - 哥布林比例：`1 - 史莱姆 - 骷髅`
- **效果**: 第 1 层就有 20% 骷髅，每层骷髅 +8%、史莱姆 -7%，弱怪永远存在但比例越来越低

| 层数 | 史莱姆 | 哥布林 | 骷髅 |
|------|--------|--------|------|
| 1 | 43% | 37% | 20% |
| 2 | 36% | 36% | 28% |
| 3 | 29% | 35% | 36% |
| 4 | 22% | 34% | 44% |
| 5 | 15% | 33% | 52% |
| 6 | 8% | 32% | 60% |
| 7+ | 5% | 27%+ | 68%+ |

#### 8.3 钥匙掉率降低
- **文件**: `core/.../world/GameWorld.java` (`dropChestLoot` 方法)
- **修改**:
  - 原逻辑：没钥匙时 100% 掉，有钥匙时 25% 掉
  - 新逻辑：没钥匙时 55% 掉，有钥匙时仅 8% 掉
  - Boss 箱子仍然 100% 掉钥匙

#### 8.4 玩家升级攻击力提升
- **文件**: `core/.../system/LevelSystem.java`
- **修改**: 升级时攻击力增加从 +3 改为 +4，补偿敌人整体变强

---

## 九、商店系统

### 背景
金币在游戏中原本无实际用途，仅在结算时换算为少量 Forge Sparks（25金 = 1火花）。用户建议给金币加上实际消费用途。

### 修改点

#### 9.1 Shop 实体
- **文件**: `core/.../entity/Shop.java`（新建）
- **内容**: 继承 Entity，交互距离 60f，纯数据实体

#### 9.2 ShopItem 枚举
- **文件**: `core/.../entity/ShopItem.java`（新建）
- **内容**: 定义 5 种可购买商品：

| 商品 | 效果 | 基础价格 | 每层涨价 |
|------|------|----------|----------|
| Potion | 获得 1 瓶药水 | 40g | +8g/层 |
| +3 Attack | 永久攻击 +3 | 80g | +8g/层 |
| +2 Defense | 永久防御 +2 | 65g | +8g/层 |
| Key | 获得 1 把钥匙 | 55g | +8g/层 |
| +20 Max HP | 永久 maxHP+20 并回复 20 | 90g | +8g/层 |

- 价格公式：`basePrice + floor * 8`

#### 9.3 GameWorld 商店生成与购买
- **文件**: `core/.../world/GameWorld.java`
- **修改**:
  - 添加 `public Shop shop` 和 `public boolean shopOpen` 字段
  - 新增 `seedShop()` 方法：每层在随机地板位置生成一个商店
  - 新增 `openShop()` 方法：玩家在商店 60 单位内按 E 打开商店界面
  - 新增 `tryBuy(ShopItem)` 方法：检查金币是否足够，扣款并应用效果，播放音效和飘字

#### 9.4 商店世界渲染
- **文件**: `core/.../render/WorldRenderer.java`
- **修改**:
  - 新增 `drawShopSprite()` 方法：用青色染色的 crate 贴图渲染商店
  - `drawPrompts()` 中新增商店提示：玩家靠近商店（70 单位内）时显示青色 "Shop [E]"

#### 9.5 商店 UI 叠加层
- **文件**: `core/.../screen/GameScreen.java`
- **修改**:
  - 导入 `ShopItem`
  - 添加 `shopButtons[]`（5 个矩形）和 `shopCloseButton` 矩形
  - 商店打开时游戏暂停（`!paused && !world.shopOpen` 时才更新世界）
  - 输入处理：
    - ESC 或 E 关闭商店
    - 点击商品按钮调用 `world.tryBuy()`
    - 点击 Close 按钮关闭商店
  - 新增 `renderShopOverlay()` 方法渲染商店界面：
    - 半透明黑色遮罩 + 面板
    - 标题 "SHOP"（青色）
    - 当前金币数（金色）
    - 5 个商品行：名称 + 短说明 + 价格
    - 买得起的商品绿色高亮，买不起的灰显
    - 鼠标悬停变色
    - "Close" 按钮

---

## 十、UI 文本和字体布局收敛

### 背景
开始界面的难度选择区出现字体异常和文字重叠。问题主要来自特殊符号、长文案和装饰元素挤在同一块区域。

### 修改点

#### 10.1 主菜单文字收敛
- **文件**: `core/.../screen/MainMenuScreen.java`
- **修改**:
  - 难度选择只显示 `Mode: Easy/Normal/Hard`
  - 移除难度栏左右特殊箭头，避免字体缺字
  - 移除与难度栏重叠的 Relics 标签和底部怪物装饰，保留玩家右侧的史莱姆装饰
  - 右侧信息面板改为短词，降低文本溢出风险

#### 10.2 暂停、商店、HUD 短文本
- **文件**: `core/.../screen/GameScreen.java`、`core/.../render/HudRenderer.java`
- **修改**:
  - 暂停界面提示改为短键位文本
  - 商店关闭按钮改为 `Close`
  - 游戏内底部控制提示压缩为短键位串

#### 10.3 结算界面统一
- **文件**: `core/.../screen/GameOverScreen.java`
- **修改**:
  - 从纯文字结算页改为统一面板式 UI
  - Retry/Menu 支持鼠标点击
  - 统计文案改为短标签，减少错位

#### 10.4 难度描述缩短
- **文件**: `core/.../util/Difficulty.java`
- **修改**: 将难度描述缩短为 `Light` / `Standard` / `Brutal`，避免未来重新引用时再次撑开布局

---

## 十一、资源映射修正

### 背景
箱子交互前使用了开启动画的空白帧，导致未打开时看起来没有贴图；下一层入口只取了大入口素材的一小格，表现不像下楼入口。

### 修改点

#### 11.1 箱子关闭帧
- **文件**: `core/.../asset/GameAssets.java`
- **修改**:
  - 关闭箱子改用 props 表中独立的关闭箱子格
  - 开启动画跳过空白帧，只使用可见的开启帧
  - `chestFrame()` 增加关闭帧兜底，避免缺素材时返回空贴图

#### 11.2 下楼入口贴图
- **文件**: `core/.../asset/GameAssets.java`、`core/.../render/WorldRenderer.java`
- **修改**:
  - 下一层入口改用 tileset 中 2x3 的入口素材区域
  - 入口贴图移到地板层之后统一绘制，避免被相邻地板覆盖
  - 去掉入口的蓝色染色，保留素材原色

#### 11.3 程序生成备用素材同步
- **文件**: `core/.../asset/ProceduralTextures.java`
- **修改**: 备用 props/tileset 同步生成关闭箱子和 2x3 下楼入口，保证缺真实图片时仍有可见贴图

---

## 十二、全局字体替换

### 背景
默认 BitmapFont 字体偏小，并且对中文和像素风显示不友好。项目 assets 中已有 `fusion-pixel-10px-monospaced-zh_hans.ttf`。

### 修改点

#### 12.1 TTF 字体接入
- **文件**: `core/.../DungeonForgeGame.java`
- **修改**:
  - 全局 `font` 改为通过 `fusion-pixel-10px-monospaced-zh_hans.ttf` 生成
  - 字号设为 18，比原默认字体更大
  - 启用增量字形生成，保留中文字体扩展能力
  - 字体加载失败时回退到默认 `BitmapFont`

#### 12.2 FreeType 依赖
- **文件**: `core/build.gradle`、`lwjgl3/build.gradle`
- **修改**:
  - 添加 `gdx-freetype`
  - 桌面端添加 `gdx-freetype-platform:natives-desktop`

#### 12.3 大标题缩放整理
- **文件**: `core/.../screen/MainMenuScreen.java`、`core/.../screen/GameScreen.java`、`core/.../screen/GameOverScreen.java`
- **修改**: 在全局基础字号变大后，下调标题和按钮的局部 scale，防止文字溢出面板

---

## 十三、玩家碰撞手感优化

### 背景
玩家碰撞盒接近占满一格，贴墙和穿过窄口时容易感觉被空气挡住，不符合直觉。

### 修改点

#### 13.1 玩家碰撞盒缩小
- **文件**: `core/.../entity/Player.java`
- **修改**:
  - 保持玩家视觉尺寸 `28x28` 不变
  - 覆盖 `getBounds()`，实际碰撞盒向内收缩 4px
  - 实际碰撞盒变为 `20x20`，移动、陷阱、投射物命中都使用更小范围

#### 13.2 调试碰撞框同步
- **文件**: `core/.../render/DebugRenderer.java`
- **修改**: 玩家调试框改为绘制 `getBounds()` 返回的真实碰撞盒

---

## 十四、基础粒子反馈

### 背景
调研报告建议优先补充低成本高反馈的视觉效果。当前游戏已有屏震、飘字和音效，但命中、开箱、拾取、升级等事件缺少瞬时视觉爆点。

### 修改点

#### 14.1 Particle 实体
- **文件**: `core/.../entity/Particle.java`（新建）
- **内容**: 添加轻量粒子实体，包含位置、速度、颜色、大小、生命周期和淡出逻辑

#### 14.2 世界粒子生命周期
- **文件**: `core/.../world/GameWorld.java`
- **修改**:
  - 添加 `particles` 容器
  - 添加 `addParticleBurst()` 方法生成小范围爆发粒子
  - 在楼层加载时清空粒子
  - 在世界更新中推进并移除过期粒子
  - 开箱、购买、拾取、陷阱受伤时触发对应颜色粒子

#### 14.3 战斗和升级反馈
- **文件**: `core/.../system/CombatSystem.java`、`core/.../system/LevelSystem.java`
- **修改**:
  - 近战命中使用白色粒子，击杀使用金色粒子
  - 箭矢命中使用青色粒子
  - 玩家受伤使用红色粒子
  - 吸血回复使用绿色粒子
  - `gainExp()` 返回升级次数，升级时触发 `LEVEL UP` 飘字、金色粒子和轻微屏震

#### 14.4 世界渲染接入
- **文件**: `core/.../render/WorldRenderer.java`
- **修改**: 在攻击轨迹之后、迷雾之前绘制粒子，使粒子受迷雾遮挡且不盖住 UI

---

## 十五、Sky Spy（空谍）叙事重构

### 背景
项目方向不再是经典地牢，而是 `Sky Spy（空谍）`：众多个"自己"中的一个"我"向天堂顶层攀登，试图杀死所有挡路者并找出真相。

### 修改点

#### 15.1 独立叙事文档
- **文件**: `SKY_SPY_STORY_PLAN.md`（新建）
- **内容**:
  - 确定游戏名 `Sky Spy（空谍）`
  - 将地牢楼层重构为天堂层级
  - 将怪物解释为众多个白色"自己"
  - 新增爬行动物/白色猫作为关键记忆敌人
  - 补齐顶层"空自己"的动机
  - 将死亡设计为记忆推进，而非纯惩罚
  - 给出现有系统到新主题的映射表

#### 15.2 主菜单标题
- **文件**: `core/.../screen/MainMenuScreen.java`
- **修改**: 主菜单标题从 `DUNGEONFORGE` 改为 `SKY SPY`，副标题改为更贴合新设定的短句

---

## 十六、操作、字体和地图手感

### 修改点

#### 16.1 字体生成参数
- **文件**: `core/.../DungeonForgeGame.java`
- **修改**:
  - Fusion Pixel 生成字号从 18 调整到 20
  - 增加 1px 横向字距，降低视觉压缩感

#### 16.2 鼠标近战朝向
- **文件**: `core/.../system/CombatSystem.java`
- **修改**: 鼠标左键近战时，角色朝向同步到鼠标攻击方向

#### 16.3 双格走廊
- **文件**: `core/.../world/DungeonGenerator.java`
- **修改**:
  - 房间之间的横向走廊扩展为 2 格高
  - 纵向走廊扩展为 2 格宽
  - 刷怪系统保持从全地图可行走地块随机取点，因此走廊地块也会成为刷怪候选

---

## 修改文件汇总

| 文件 | 操作 | 涉及章节 |
|------|------|----------|
| `core/build.gradle` | 修改 | 十二 |
| `lwjgl3/build.gradle` | 修改 | 十二 |
| `gradlew` | 替换 | 一 |
| `gradle/wrapper/gradle-wrapper.jar` | 新增 | 一 |
| `gradle.properties` | 修改 | 一 |
| `lwjgl3/build.gradle` | 修改 | 一 |
| `lwjgl3/.../StartupHelper.java` | 修改 | 一 |
| `core/.../entity/Entity.java` | 修改 | 二 |
| `core/.../world/DungeonMap.java` | 修改 | 二 |
| `core/.../world/Room.java` | 修改 | 二 |
| `core/.../system/CollisionSystem.java` | 修改 | 二 |
| `core/.../system/CombatSystem.java` | 修改 | 二、七、十四、十六 |
| `core/.../entity/AttackEffect.java` | 修改 | 二 |
| `core/.../system/PathfindingSystem.java` | 修改 | 二 |
| `core/.../system/FogOfWarSystem.java` | 重写 | 三 |
| `core/.../entity/Player.java` | 修改 | 四、十三 |
| `core/.../util/Difficulty.java` | 新建/修改 | 五、八、十 |
| `core/.../DungeonForgeGame.java` | 修改 | 五、十二、十六 |
| `core/.../world/GameWorld.java` | 修改 | 五、六、八、九、十四 |
| `core/.../entity/Enemy.java` | 修改 | 五、八 |
| `core/.../system/SpawnerSystem.java` | 修改 | 五、八 |
| `core/.../screen/MainMenuScreen.java` | 修改 | 五、十、十二、十五 |
| `core/.../screen/GameScreen.java` | 修改 | 五、九、十、十二 |
| `core/.../screen/GameOverScreen.java` | 修改 | 五、十、十二 |
| `core/.../render/HudRenderer.java` | 修改 | 十 |
| `core/.../render/WorldRenderer.java` | 修改 | 六、九、十四 |
| `core/.../render/DebugRenderer.java` | 修改 | 十三 |
| `core/.../world/BiomeType.java` | 修改 | 八 |
| `core/.../system/LevelSystem.java` | 修改 | 八、十四 |
| `core/.../entity/Particle.java` | 新建 | 十四 |
| `core/.../entity/Shop.java` | 新建 | 九 |
| `core/.../entity/ShopItem.java` | 新建 | 九 |
| `core/.../asset/GameAssets.java` | 修改 | UI 图标、箱子动画帧、十一 |
| `core/.../asset/ProceduralTextures.java` | 修改 | 十一 |
| `core/.../world/DungeonGenerator.java` | 修改 | 十六 |
| `SKY_SPY_STORY_PLAN.md` | 新建 | 十五 |
| `README.md` | 修改 | 十五 |
