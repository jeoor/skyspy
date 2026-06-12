# DungeonForge 完整开发方案

## 0. 当前完成状态

当前实现已经覆盖本方案的核心必做目标和优先打磨目标：

- 当前项目定位已从回合制 Roguelike 参考转为实时动作 Roguelite：重点是走位、预兆、打击反馈和 Build 涌现。
- 楼层已加入 Catacombs、Fungal、Ashen、Frost 四种循环 biome，影响视觉色调、敌人构成、陷阱压力和敌人数值。
- 主菜单、游戏界面、暂停覆盖层、死亡界面、死亡重开已完成。
- 随机地牢、楼梯、楼层递增、墙体碰撞、摄像头平滑跟随已完成。
- 玩家移动、近战攻击、箭矢技能、Dash、药水、拾取已完成。
- Slime、Goblin/Orc、Skeleton 三种怪物类型已完成，数值、刷新权重、视觉 tint、行为和预兆攻击已区分。
- 怪物游荡、A* 绕墙追踪、冲锋、远程、重击、自动刷新、楼层属性缩放已完成。
- HP、EXP、Level、金币、药水、武器/护甲强化掉落已完成。
- 视野迷雾、小地图、HUD、浮动伤害数字、受击闪烁、死亡动画已完成。
- 屏幕震动、顿帧、击退、随机化音效池、最高层数本地保存已完成。
- 武器性格、可叠加遗物、宝箱 Build 奖励、Forge Sparks 局外解锁已完成。
- 主要 sprite sheet 缺失时，会生成程序兜底纹理，避免启动时因缺图崩溃。
- 宝箱、陷阱、每 3 层出现的 Boss 房间、音效、远程武器这些扩展目标也已接入主循环。
- 当前玩法已根据现有 `dungeon-pack_free` 资产重设计为寻宝开门循环：探索装饰房间，寻找钥匙，开启楼梯，Boss 层先击败守卫再开 Boss 宝箱。

## 1. 项目定位

`DungeonForge` 是一个 Java + libGDX 的 2D 实时动作 Roguelite 地牢 Demo。目标不是做简单移动方块，也不是和回合制 Roguelike 拼内容量，而是做一个可运行、可扩展、结构清晰，并且强调手感、走位和 Build 变化的地牢原型。

核心体验：

- 玩家从主菜单进入随机生成的地牢。
- 摄像头平滑跟随玩家，地图不会露出边界外区域。
- 玩家可以移动、Dash、攻击、射箭、拾取道具、切换武器、叠遗物、使用药水、升级、进入下一层。
- 怪物自动刷新，会游荡、发现玩家、追踪玩家，并用不同预兆攻击逼玩家走位。
- 地图有迷雾、小地图、HUD、楼梯、死亡重开。
- 每三层切换一个 biome，让楼层不只是数值上涨。
- 死亡会结算 Forge Sparks，用于解锁更多开局武器，让失败也推进下一把。
- 第一版优先 Desktop/lwjgl3，后续可扩展 Android。

参考方向：

- Shattered Pixel Dungeon / Pixel Dungeon 只作为系统拆分和 Roguelike 边界参考，不作为核心体验对标。
- 核心体验更接近实时动作地牢：预兆、闪避、武器节奏、遗物叠加和短局循环。
- 不复制 GPL 项目的源码、素材、关卡、数值表或具体实现。
- 本项目以自有代码、程序生成贴图和当前目录资产包为基础。

## 2. 开源参考记录

### Shattered Pixel Dungeon

仓库：

```text
https://github.com/00-Evan/shattered-pixel-dungeon
```

记录：

- 现代化 Pixel Dungeon 分支，当前支持 Android、iOS、Desktop。
- 使用 Java + libGDX，Desktop 侧使用 `gdx-backend-lwjgl3`。
- 当前仓库构建中使用 `gdxVersion = 1.14.0`。
- 代码中有自有上层框架 `com.watabou.noosa`，不要直接复制。
- 适合参考多平台结构、Scene 管理、Roguelike 系统拆分、物品/怪物/楼层组织方式。
- GPL-3.0 许可证，只做思想参考，不复制源码和素材。

### Pixel Dungeon

仓库：

```text
https://github.com/watabou/pixel-dungeon
```

记录：

- 原版 Pixel Dungeon，README 描述为传统 Roguelike、像素图形、简洁界面。
- 仓库语言为 Java，早期项目结构更偏 Android。
- README 说明构建它需要 `watabou/PD-classes` 这个外部游戏库。
- 适合参考极简 UI、低复杂度 Roguelike 节奏、地牢探索核心体验。
- GPL-3.0 许可证，只做玩法和架构边界参考，不复制源码和素材。

## 3. 当前项目基础

当前目录已经具备 Gradle + libGDX Desktop 骨架：

```text
settings.gradle
build.gradle
gradle.properties
gradlew.bat
core/
lwjgl3/
assets/
```

当前运行方式：

```powershell
.\gradlew.bat lwjgl3:run
```

当前编译验证方式：

```powershell
.\gradlew.bat lwjgl3:classes
```

技术约束：

- Java 代码包名：`com.kayro.dungeon`
- Gradle：项目本地 bootstrap 下载 Gradle 9.1.0
- libGDX：`gdxVersion=1.14.0`
- Desktop 入口：`lwjgl3/src/main/java/com/kayro/dungeon/lwjgl3/Lwjgl3Launcher.java`
- 运行工作目录：`assets/`

## 4. 资产包使用方案

当前运行使用的主要资产：

```text
assets/dungeon-pack_free_items.png
assets/dungeon-pack_free_player.png
assets/dungeon-pack_free_props.png
assets/dungeon-pack_free_slime.png
assets/dungeon-pack_free_tileset_1.png
assets/dungeon-pack_free_tileset_2.png
assets/dungeon-pack_free_ui.png
assets/Audio/*.ogg
```

接入原则：

- 运行工作目录是 `assets/`，代码中使用相对路径加载。
- 不在代码里硬编码下载包的嵌套路径。
- 地牢瓦片、角色、怪物、道具、箭矢和音效优先使用当前资产。
- 如果主要 sprite sheet 缺失，`ProceduralTextures` 会生成基础兜底纹理。
- 音效按事件建立随机池，避免攻击、受击、脚步等声音重复单调。

当前资产映射：

| 游戏对象 | 当前素材 | 兜底 |
| --- | --- | --- |
| Player | `dungeon-pack_free_player.png` | 程序生成 actor sheet |
| Slime | `dungeon-pack_free_slime.png` | 程序生成 actor sheet |
| Goblin/Orc | Slime 动画 + 暖色 tint + 独立数值 | 程序生成 actor sheet |
| Skeleton | Slime 动画 + 骨色 tint + 独立数值 | 程序生成 actor sheet |
| Floor/Wall/Stairs | `dungeon-pack_free_tileset_1.png` | 程序生成 tileset |
| Coin/Potion/Upgrade/Arrow | `dungeon-pack_free_items.png` | 程序生成 item sheet |
| SFX | `assets/Audio/*.ogg` | 缺失时跳过对应声音 |

渲染尺寸：

```text
Tile：32 x 32
Sprite source cell：16 x 16
角色逻辑碰撞盒：28 x 28
角色显示尺寸：32 x 32
箭矢逻辑碰撞盒：18 x 8
```

## 5. 目标功能清单

必须完成：

1. 主菜单界面
2. 游戏界面
3. 死亡界面
4. 随机地牢生成
5. 玩家移动
6. 墙体碰撞
7. 摄像头平滑跟随
8. 自动刷怪
9. 至少 3 种怪物：Slime、Goblin/Orc、Skeleton
10. 怪物追踪玩家
11. 玩家近战攻击
12. 怪物攻击玩家
13. HP、EXP、等级系统
14. 道具掉落
15. 药水恢复
16. 金币计数
17. 楼梯进入下一层
18. 地牢层数递增
19. 视野迷雾
20. 小地图
21. HUD
22. 死亡后重开

优先选做：

1. Dash 冲刺
2. 浮动伤害数字
3. 攻击闪光/受击闪烁
4. 屏幕轻微震动
5. 保存最高层数

扩展目标：

1. 宝箱
2. 陷阱
3. Boss 房间
4. 音效
5. 远程武器

## 6. 最终项目结构

```text
core/src/main/java/com/kayro/dungeon/
  DungeonForgeGame.java

  screen/
    MainMenuScreen.java
    GameScreen.java
    PauseScreen.java
    GameOverScreen.java

  world/
    GameWorld.java
    DungeonMap.java
    DungeonGenerator.java
    Room.java
    Tile.java
    TileType.java

  entity/
    AttackEffect.java
    Chest.java
    Entity.java
    LivingEntity.java
    Player.java
    Enemy.java
    EnemyType.java
    Item.java
    ItemType.java
    DamageText.java
    Projectile.java
    Trap.java

  system/
    InputHandler.java
    CameraController.java
    CollisionSystem.java
    CombatSystem.java
    AISystem.java
    SpawnerSystem.java
    LootSystem.java
    LevelSystem.java
    FogOfWarSystem.java

  render/
    WorldRenderer.java
    HudRenderer.java
    MinimapRenderer.java
    DebugRenderer.java

  asset/
    Assets.java
    GameAssets.java
    ProceduralTextures.java
    SpriteAnimations.java
    SpriteAnimationSet.java
    FrameAnimationSet.java
    DirectionalFrameAnimationSet.java

  util/
    Constants.java
    Direction.java
    GameMath.java
```

## 7. Screen 设计

### DungeonForgeGame

职责：

- 继承 `Game`，统一切换 Screen。
- 持有全局 `SpriteBatch`、`ShapeRenderer`、`Assets`。
- 提供 `startNewGame()` 和 `showGameOver(...)`。

关键字段：

```java
public SpriteBatch batch;
public ShapeRenderer shapes;
public Assets assets;
```

关键方法：

```java
public void startNewGame();
public void showMainMenu();
public void showGameOver(int floor, int kills, int gold);
```

### MainMenuScreen

功能：

- 显示 `DungeonForge` 标题。
- 显示当前操作提示。
- `Enter` 开始新游戏。
- `Esc` 退出。

### GameScreen

功能：

- 创建并持有 `GameWorld`。
- 创建 `worldCamera/worldViewport` 和 `hudCamera/hudViewport`。
- 每帧先处理全局输入，再更新世界，再渲染世界和 HUD。
- `Esc` 暂停/恢复。
- 玩家死亡时切换到 `GameOverScreen`。

### PauseScreen

第一版可以不独立成 Screen，而是在 `GameScreen` 内用暂停覆盖层实现。

### GameOverScreen

功能：

- 显示死亡统计：最高楼层、击杀数、金币数。
- `R` 重开。
- `Enter` 回主菜单。

## 8. 世界与地图系统

地图常量：

```text
MAP_WIDTH = 120
MAP_HEIGHT = 80
TILE_SIZE = 32
VIEW_WIDTH = 1280
VIEW_HEIGHT = 720
```

Tile 类型：

```java
public enum TileType {
    WALL,
    FLOOR,
    STAIRS_DOWN
}
```

当前版本不随机生成门；如果后续加入门，必须同时实现门实体、碰撞、交互和开门动画。

Tile 数据：

```java
public class Tile {
    public TileType type;
    public boolean visible;
    public boolean explored;
}
```

地图生成算法：

1. 全图初始化为 `WALL`。
2. 随机生成 18 到 28 个房间。
3. 房间宽 6 到 14，高 5 到 12。
4. 房间边缘至少留 1 格墙。
5. 新房间不能与已有房间过度重叠。
6. 放置成功后将房间区域挖成 `FLOOR`。
7. 依次连接房间中心点，生成 L 型走廊。
8. 第一个房间中心作为玩家出生点。
9. 找到距离出生点最远的房间，把中心设为 `STAIRS_DOWN`。
10. 初始怪物和道具只刷在可走地板上。

连通性保障：

- 房间按生成顺序逐个连接，保证主连通。
- 生成结束后可以做一次 flood fill 验证可走区域。
- 如果可走区域过小，重新生成。

## 9. 实体系统

### Entity

基础字段：

```java
Vector2 position;
Vector2 size;
boolean removed;
```

基础方法：

```java
Rectangle getBounds();
Vector2 getCenter();
void update(float delta, GameWorld world);
```

### LivingEntity

共享战斗字段：

```java
int hp;
int maxHp;
int attack;
int defense;
float speed;
float attackCooldown;
float attackTimer;
```

### Player

初始数值：

```text
MaxHP = 100
HP = 100
Attack = 15
Defense = 2
Level = 1
EXP = 0
Gold = 0
Keys = 0
Potions = 2
MoveSpeed = 150
AttackCooldown = 0.35s
InvincibleTimer = 0
```

玩家操作：

```text
WASD：移动
鼠标左键 / J：攻击
Q：使用药水
E：拾取 / 开宝箱 / 进入楼梯
Shift：Dash，选做
Esc：暂停
```

升级规则：

```text
levelUpExp = 50 + level * 35
升级后：
MaxHP + 15
Attack + 3
Defense + 1
HP 回满
```

### Enemy

怪物类型：

| 类型 | HP | Attack | Defense | Speed | EXP | 特点 |
| --- | ---: | ---: | ---: | ---: | ---: | --- |
| Slime | 30 | 6 | 0 | 55 | 8 | 慢，数量多 |
| Goblin/Orc | 45 | 10 | 1 | 85 | 15 | 快，追击强 |
| Skeleton | 70 | 14 | 2 | 60 | 25 | 血厚，攻击高 |

楼层缩放：

```text
enemyHpMultiplier = 1.0 + floor * 0.12
enemyDamageMultiplier = 1.0 + floor * 0.08
```

## 10. 输入与碰撞

### InputHandler

职责：

- 读取键盘和鼠标输入。
- 计算移动方向。
- 记录攻击、拾取、药水、暂停等一次性事件。
- 根据鼠标屏幕坐标换算世界坐标，确定玩家攻击方向。

### CollisionSystem

职责：

- 处理玩家与墙体碰撞。
- 处理怪物与墙体碰撞。
- 处理实体移动时的 X/Y 分轴检测。

分轴移动策略：

1. 尝试 X 方向移动。
2. 如果目标碰撞盒不碰墙，提交 X。
3. 尝试 Y 方向移动。
4. 如果目标碰撞盒不碰墙，提交 Y。

这样可以自然贴墙滑动。

## 11. 摄像头系统

使用：

```text
OrthographicCamera worldCamera
FitViewport worldViewport
OrthographicCamera hudCamera
FitViewport hudViewport
```

世界摄像头：

- 平滑跟随玩家中心。
- 限制在地图边界内。
- resize 时更新 viewport。

HUD 摄像头：

- 固定 1280 x 720 逻辑尺寸。
- HUD 不受世界摄像头缩放和移动影响。

## 12. AI 与刷怪

### AISystem

状态：

```text
Wander：玩家距离远，随机游荡
Chase：玩家进入警戒范围，追踪玩家
Attack：进入攻击范围，停止移动并攻击
```

追踪策略：

1. 玩家在同一可走直线内时，怪物保持直接追击，保证开放房间里的移动手感。
2. 直线被墙体或未开启宝箱阻挡时，怪物每隔约 0.35 秒用 A* 在 tile 网格上计算下一格目标。
3. A* 只缓存下一步目标，不让怪物每帧整条路径重算。
4. 路径失败时回退为直接追击/游荡，避免 AI 卡死。

### SpawnerSystem

规则：

```text
spawnInterval = max(1.2, 3.0 - floor * 0.1)
maxEnemies = 8 + floor * 2
```

刷怪限制：

- 不能刷在墙里。
- 不能刷在玩家 12 格半径内。
- 不能刷在当前可见区域内。
- 不能刷在楼梯上。
- 当前怪物数达到上限时不刷。

初始楼层生成：

- 第一层初始怪物 4 到 6 个。
- 每层额外生成少量金币和药水。

## 13. 战斗系统

### 玩家攻击

规则：

- `J` 或鼠标左键触发。
- 有 0.35 秒冷却。
- 方向优先使用鼠标方向；没有鼠标输入时使用玩家 facing。
- 攻击判定使用前方圆形或扇形。
- 命中后扣血，生成伤害数字。
- 攻击瞬间可以渲染短暂剑弧。

伤害：

```text
damage = max(1, attacker.attack - target.defense)
```

### 怪物攻击

规则：

- 进入攻击范围后停止移动。
- 攻击冷却默认 0.9 到 1.2 秒。
- 玩家受伤后有 0.5 秒无敌时间。
- 无敌时间内玩家闪烁。

### 死亡处理

怪物死亡：

- 增加击杀数。
- 掉落经验。
- 调用 `LootSystem` 生成物品。
- 从敌人列表移除。

玩家死亡：

- 停止世界更新。
- 切换 `GameOverScreen`。

## 14. 道具与掉落

Item 类型：

```java
public enum ItemType {
    COIN,
    POTION,
    SWORD_UPGRADE,
    ARMOR_UPGRADE
}
```

掉落概率：

```text
金币：70%
药水：15%
短剑强化：5%
护甲强化：5%
无掉落：5%
```

物品效果：

- `COIN`：金币 + 1 到 8，随楼层增加。
- `POTION`：药水数量 + 1。
- `SWORD_UPGRADE`：攻击力 + 1。
- `ARMOR_UPGRADE`：防御力 + 1。

拾取规则：

- 玩家靠近物品自动拾取，或者按 E 拾取。
- 第一版建议自动拾取金币，药水/强化按 E 拾取。

## 15. 楼层系统

进入下一层：

- 玩家站在 `STAIRS_DOWN` 上按 E。
- `GameWorld.nextFloor()` 重新生成地图、敌人、地面道具。
- 保留玩家的等级、经验、攻击、防御、金币、药水。
- 玩家 HP 不自动回满，除非升级或使用药水。

楼层递增影响：

- 怪物上限增加。
- 怪物 HP 和伤害增加。
- 金币掉落数量增加。
- Skeleton 出现概率提高。

怪物权重示例：

| 楼层 | Slime | Goblin/Orc | Skeleton |
| --- | ---: | ---: | ---: |
| 1-2 | 70% | 30% | 0% |
| 3-5 | 45% | 40% | 15% |
| 6+ | 30% | 40% | 30% |

## 16. 迷雾系统

FogOfWarSystem 规则：

1. 每次更新先将所有 tile 的 `visible=false`。
2. 以玩家所在 tile 为中心。
3. 半径 8 格内设置 `visible=true`。
4. 可见 tile 同时设置 `explored=true`。
5. 未探索区域渲染为纯黑。
6. 已探索但不可见区域渲染为半透明黑。

第一版不做墙体遮挡视线。后续可以增加 Bresenham ray casting，让墙后区域不可见。

## 17. 小地图系统

MinimapRenderer：

- 位置：右下角。
- 逻辑尺寸：220 x 160。
- 只显示已探索区域。
- 显示玩家、楼梯、当前可见怪物。

颜色：

```text
已探索地板：暗灰蓝
当前可见地板：亮灰蓝
玩家：蓝色点
怪物：红色点
楼梯：黄色点
未探索：不绘制
墙：默认不绘制
```

## 18. HUD 设计

左上角：

- HP 条
- EXP 条
- Level

右上角：

- Floor
- Enemy count
- Kills

左下角：

- Gold
- Potions
- Keys

底部：

```text
WASD Move   J/LMB Attack   Q Potion   E Interact   ESC Pause
```

暂停覆盖层：

- 半透明黑色遮罩。
- 显示 `Paused`。
- `Esc` 返回游戏。

## 19. 渲染策略

WorldRenderer 绘制顺序：

1. 地板
2. 墙体
3. 楼梯
4. 道具
5. 怪物
6. 玩家
7. 攻击特效
8. 伤害数字
9. 迷雾

性能要求：

- 地图只绘制摄像头范围内 tile。
- 实体绘制前做简单视口裁剪。
- 纹理由 `Assets` 统一持有，Screen 释放时不重复释放。

可见 tile 计算：

```text
startX = floor(cameraLeft / TILE_SIZE)
endX = ceil(cameraRight / TILE_SIZE)
startY = floor(cameraBottom / TILE_SIZE)
endY = ceil(cameraTop / TILE_SIZE)
```

## 20. 开发阶段计划

### 阶段 0：构建链路

当前状态：已完成。

验收：

```powershell
.\gradlew.bat --version
.\gradlew.bat lwjgl3:classes
```

### 阶段 1：主循环、Screen、地图、玩家、摄像头

目标：

- `DungeonForgeGame` 改为继承 `Game`。
- 实现 MainMenuScreen、GameScreen、GameOverScreen。
- 实现随机地牢。
- 实现玩家 WASD 移动。
- 实现墙体碰撞。
- 实现摄像头平滑跟随。
- 接入 Soldier 玩家贴图，缺失时使用程序贴图。

验收：

- `.\gradlew.bat lwjgl3:run` 可运行。
- Enter 从菜单进入游戏。
- 玩家不能穿墙。
- 摄像头跟随自然。
- 地图随机生成。

### 阶段 2：敌人、AI、刷怪

目标：

- 实现 Enemy、EnemyType。
- 实现 Slime、Goblin/Orc、Skeleton。
- Orc 使用资产包 Orc 动画。
- 实现游荡、追踪、攻击状态切换。
- 实现自动刷怪。

验收：

- 怪物不会刷在墙里。
- 怪物不会刷在玩家附近。
- 怪物不会刷在当前可见区域。
- 怪物会追踪玩家。
- 怪物不能穿墙。

### 阶段 3：战斗、经验、掉落

目标：

- 实现玩家攻击。
- 实现怪物攻击。
- 实现 HP、伤害、死亡。
- 实现经验和升级。
- 实现金币、药水、武器/护甲强化掉落。
- 实现浮动伤害数字。

验收：

- 玩家可以杀怪。
- 怪物可以攻击玩家。
- 玩家死亡进入 GameOverScreen。
- 玩家可以升级。
- 怪物死亡有掉落。

### 阶段 4：楼层、迷雾、小地图、HUD 完善

目标：

- 实现楼梯进入下一层。
- 实现楼层递增和怪物属性缩放。
- 实现 FogOfWarSystem。
- 实现 MinimapRenderer。
- 完善 HUD。

验收：

- 玩家站在楼梯按 E 进入下一层。
- 下一层重新生成地图。
- 楼层越高怪物越强。
- 小地图显示已探索区域。
- HUD 数据正确。

### 阶段 5：打磨与扩展

目标：

- Dash。
- 受击闪烁。
- 攻击特效。
- 屏幕震动。
- 最高层数保存。
- 简单音效。
- 远程箭矢技能。
- 宝箱。
- 陷阱。
- Boss 房间。

验收：

- 手感更完整。
- 不影响核心玩法稳定性。
- 无缺失资源报错。
- 宝箱可按 E 打开并掉落奖励。
- 陷阱会对玩家造成伤害。
- 每 3 层出现 Boss 房间，Boss 未击败时不能下楼。

## 21. 验收标准

最终验收：

1. 项目能直接运行。
2. 没有缺失资源报错。
3. 玩家可以移动。
4. 玩家不能穿墙。
5. 摄像头跟随自然。
6. 地图随机生成。
7. 怪物会自动刷新。
8. 怪物不会刷在玩家脸上。
9. 怪物会追踪玩家。
10. 玩家可以攻击并杀死怪物。
11. 怪物可以攻击玩家。
12. 有经验和升级。
13. 有金币和药水。
14. 有楼梯进入下一层。
15. 有视野迷雾。
16. 有小地图。
17. 有死亡重开。
18. 代码结构清晰。
19. update 和 render 分离。
20. 资产缺失时仍能使用程序生成贴图兜底。

每阶段固定验证：

```powershell
.\gradlew.bat lwjgl3:classes
```

功能完成后运行：

```powershell
.\gradlew.bat lwjgl3:run
```

## 22. 实现注意事项

- 不把所有逻辑写进 `GameScreen`。
- 系统类负责逻辑，Renderer 只负责渲染。
- 世界坐标、tile 坐标、屏幕坐标必须分清。
- 不直接复制 Shattered Pixel Dungeon / Pixel Dungeon 的 GPL 代码和素材。
- 不在代码里硬编码复杂资产包路径，统一从 `assets/` 加载。
- 新增素材前先确认路径和文件名，避免运行时找不到资源。
- 地图、敌人、掉落的随机逻辑集中管理，方便后续调参。
- 每个阶段都先保证可编译，再推进下一阶段。
