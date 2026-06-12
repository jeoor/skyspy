## DungeonForge

DungeonForge is a Java + libGDX desktop project.

The project uses a local Gradle bootstrap script, so you do not need to install
Gradle globally. You only need a JDK and network access for the first run.

## Run

From this directory:

```powershell
.\gradlew.bat lwjgl3:run
```

The first run downloads Gradle 9.1.0 and the libGDX dependencies. Later runs use
the cached files under `.gradle`.

## Controls

```text
Enter       Start from menu
WASD        Move
Shift       Dash
J / LMB     Melee attack
K / RMB     Fire arrow skill
Q           Use potion
E           Pick up / open chest / use stairs
ESC         Pause
R           Restart on game over
```

## Structure

```text
core/      Shared game code.
lwjgl3/    Desktop launcher.
assets/    Runtime working directory for desktop.
```

The desktop entry point is
`lwjgl3/src/main/java/com/kayro/dungeon/lwjgl3/Lwjgl3Launcher.java`.

## Implemented Gameplay

```text
Random dungeon generation, rotating biomes, A* enemy chase pathfinding, smooth
camera follow, wall collision, fog of war, minimap, HUD, three enemy types with
telegraphed attacks, melee combat, arrow skill, dash, damage numbers, hit-stop,
knockback, drops, potions, leveling, weapon styles, stackable relics, death
animation, restart flow, varied sound effects, saved best floor, and Forge
Sparks that unlock more starting weapons. Chests can be opened with E, traps
damage the player, and every third floor has a boss room that must be cleared
before using the stairs.
```

## Current Design

DungeonForge now uses the current dungeon-pack assets as a vault-hunting loop:

```text
1. Explore rotating biomes with different tint, enemy mix, trap pressure, and enemy stats.
2. Find a key from chests or rare enemy drops.
3. Use the key at the stairs to enter the next floor.
4. On every third floor, defeat the boss before the stairs and boss chest unlock.
5. Swap weapons and stack relics so each run has a different combat rhythm.
6. Death awards Forge Sparks, which expand the starting weapon pool for later runs.
7. Use the minimap to track explored rooms, unopened chests, visible traps, enemies, and stairs.
```

## Assets

The current build uses the All-In-One Dungeon Asset Pack free edition as the
primary runtime art set.

```text
assets/dungeon-pack_free_*.png  Dungeon tiles, player, slime, props, UI, items.
assets/Audio/                  RPG sound effects, CC0.
```

If the main sprite sheets are missing, the game creates simple procedural
fallback textures at startup so the desktop build can still run.
