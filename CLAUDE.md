# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```powershell
.\gradlew.bat lwjgl3:run          # Run the game
.\gradlew.bat lwjgl3:fatJar       # Build all-in-one JAR
.\gradlew.bat lwjgl3:buildExe     # Build Windows .exe (needs JDK 17+)
```

No Gradle install required — wrapper bootstraps automatically. Java 17, Gradle 9.1.0.
Core generator contract tests run with `.\gradlew.bat core:test`.

## Architecture

**DungeonForge** — 2D real-time action roguelite, desktop-only (LWJGL3), built on libGDX 1.14.0.

### Modules

- `core/` — All game logic (57 Java files). Platform-independent.
- `lwjgl3/` — Desktop launcher only (`Lwjgl3Launcher.java` → `DungeonForgeGame`).
- `assets/` — Runtime sprites and sounds from Blind Jump, SmileySans font, license notices, plus `title.png`.

### Game Loop

```
Lwjgl3Launcher.main()
  └─ DungeonForgeGame.create()          # Shared resources: SpriteBatch, ShapeRenderer, BitmapFont, Assets, Sfx
       └─ MainMenuScreen → GameScreen   # GameScreen holds GameWorld
            └─ GameWorld.update(delta)   # Orchestrates all systems each frame
                 ├─ InputHandler
                 ├─ AISystem (A* pathfinding via PathfindingSystem)
                 ├─ CombatSystem (shooting, melee, projectile collisions, damage)
                 ├─ CollisionSystem (tile-based)
                 ├─ SpawnerSystem, LevelSystem, LootSystem, FogOfWarSystem
                 └─ WorldRenderer + HudRenderer + MinimapRenderer
```

### Entity Hierarchy (OOP, not ECS)

```
Entity (abstract: position, size, removed, update)
  └─ LivingEntity (abstract: hp, attack, defense, speed, facing, animationState)
       ├─ Player (relics, weapon, level, exp, gold, keys, potions, dash)
       └─ Enemy (type: SLIME/GOBLIN/SKELETON/BOSS, wander/action state, pathfinding)
  + Projectile, Item, Chest, Trap, Shop, DecorProp, DamageText, AttackEffect, Particle
```

### Key Packages

| Package | Responsibility |
|---------|---------------|
| `asset` | `GameAssets` loads textures from the assets root, provides sprite frames, builds sky map pixmaps. `Assets` is an empty subclass (type alias). |
| `entity` | Data classes for all game objects + enums (`EnemyType`, `WeaponType`, `RelicType`, `ItemType`, `ShopItem`, `PropType`) |
| `system` | All logic: AI, combat, collision, A* pathfinding, fog of war (recursive shadowcasting), spawning, leveling, loot, input, camera |
| `render` | `WorldRenderer` (tiles + entities + fog), `HudRenderer` (HP/EXP bars, counters), `MinimapRenderer`, `DebugRenderer` |
| `world` | `GameWorld` (central state container), `DungeonGenerator` (cellular automata + flood fill), `DungeonMap`, `Tile`, `BiomeType` |
| `audio` | `Sfx` — pooled sound effects with random selection from pools |
| `util` | `Constants` (map 120×80, tile 32×26, viewport 1280×720), `Difficulty`, `Direction`, `GameMath` |

### Asset System

All sprites come from `gameObjects.png` via pixel-coordinate extraction: `pixels(texture, x, y, w, h)`. No `.atlas` files. The sprite sheet originates from the Blind Jump project — see `C:\Users\1\Desktop\blind-jump` for reference.

The sky map is built at runtime by compositing `soilPixmap`, `grassPixmap`, and `grassEdgePixmap` into a full texture via `buildBlindJumpMap()`.

### Rendering Note

The sky map uses two Pixmap layers (`base` and `edge`) composited into separate textures. `edge` draws on top of `base` at the same position — used for tiles that need to overlap wall boundaries.

## Coding Guidelines

See `AGENTS.md` — think before coding, simplicity first, surgical changes, goal-driven execution. These are enforced project conventions.
