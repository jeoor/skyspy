# Sky Spy Compliance Audit

This audit checks the current implementation against `SKY_SPY_STORY_PLAN.md`.

## Verdict

Current implementation is mostly aligned with the plan: Sky Spy identity, Blind Jump
asset use, floating-island roguelike generation, ranged combat, white self
enemies, cat memory line, final debate, true ending, and review mode are
implemented. The floating-island generator now uses room-node graph generation:
candidate islands are connected through Delaunay-style edges, an MST guarantees
reachability, optional loop edges add reroutes, and corridor carving uses
weighted path search while the runtime enemy pathfinder remains A*. Floor-specific
memory props now spawn on floating islands, white self overlays recede by layer,
and the final debate validates the three memories earned from floors 2-4.
The player-facing core loop has also been tightened around the report-backed
promise of "white-fog floating islands plus knocking white selves into the
void": player shots now carry stronger per-projectile knockback, white overlays
briefly crack open on hit, knockoff kills return rewards near the player, and
the death screen reports knockoffs, relics, strongest hit, and a short run
highlight line. The map generator now has an explicit seed path plus automated
contract tests for same-seed determinism, exit reachability, and disconnected
walkable islands.

This pass also found and removed one runtime mismatch: traps were no longer
visible because their sprite had been disabled, but the world still generated
and updated them as damaging entities. New floors no longer seed traps, so the
current build avoids invisible non-interactive hazards.

Full completion is still not proven until one runtime visual pass confirms that
floor-specific props, cat states, fog, final debate readability, and ending
screens read correctly on screen. The very detailed encounter animation scripts
in section 15 are implemented as simplified gameplay equivalents, not exact
frame-by-frame staging. The previous drawn-weapon document mismatch has been
resolved in the design docs: current player-facing language describes eye-origin
projectiles rather than a separate weapon sprite.

## Requirement Status

| Requirement | Current evidence | Status |
|---|---|---|
| Game identity is `Sky Spy` | `README.md`, main menu title, window title, jpackage name, Gradle root name use Sky Spy/SkySpy | Met |
| Menus use generated game-scene overlays | `BaseMenuScreen` renders a generated/handed-off `GameWorld` under the wash; pause overlays the live `GameScreen`; game over and ending receive the just-finished world as background | Met, needs visual pass |
| Menu/control hints do not overlap across screens | `HudRenderer.render(..., showControlHint)` hides the gameplay control hint while pause/shop overlays are active; `GameScreen` suppresses story-line overlays while paused or shopping | Met |
| Runtime assets come from Blind Jump resources | resources are flattened under `assets/`; `GameAssets.ROOT = ""`; `Sfx` loads `sounds`; font loads `fonts/SmileySans-Oblique-2.ttf`; the main menu title uses `assets/title.png` | Met |
| Do not use old dungeon tiles as runtime art | `GameAssets.hasSkySpyTiles()` is true and dungeon tile fallback is inactive | Met |
| Do not show or apply invisible hazards | `GameAssets.trap` is disabled and `GameWorld.loadFloor()` no longer calls `seedTraps()` for normal floors | Met |
| Random floating-island Roguelike | `DungeonGenerator` places room/island nodes, builds Delaunay-style candidate edges, applies MST reachability, adds loop edges, carves 2+ tile corridors, and validates rift distance/clearance | Met |
| Seeded generation is verifiable | `DungeonGenerator(long seed)`, `DungeonGenerator.generate(long seed)`, `DungeonMapAnalyzer`, and `DungeonGeneratorContractTest` cover deterministic signatures and reachability for representative seeds | Met |
| Runtime A* pathfinding is preserved | `PathfindingSystem` still drives enemy next-step movement with Manhattan heuristic and edge-exposure cost | Met |
| Void fall kills player and enemies | `GameWorld.updateVoidFalls()` kills player/enemies outside `map.isWalkableWorld()` | Met |
| Directional ranged attack is the primary player attack | `CombatSystem.updatePlayerShoot()` drives projectiles; HUD shows mouse shooting | Met |
| Mouse direction controls shooting | `CombatSystem.aimDirection()` uses `input.mouseWorld` when mouse shooting is held | Met |
| Projectile/player draw order follows facing | screen-facing projectiles draw after the player; other directions draw before the player | Met |
| Signature knockoff combat loop | `Projectile.knockback`, `Player.projectileKnockback()`, and `GameWorld.updateVoidFalls()` support strong shot knockback, enemy fall kills, and "击落" feedback | Met, needs visual/balance pass |
| White shell hit feedback | `Enemy.shellCrackTimer`, `CombatSystem.crackWhiteShell()`, and `WorldRenderer.enemyWhiteAlpha()` briefly thin the white overlay after hits | Met, needs visual pass |
| Run highlight summary | `GameWorld.voidKills`, `relicsFound`, and `strongestHit` feed the game over screen, including a concise high-point line for death/exit recap | Met |
| Build variety beyond flat stats | `RelicType` includes `回声瞳`, `裂白`, and `残影步`; player projectile damage, speed, life, knockback, shell-break damage, and dash-shot boosts can now stack | Met, needs balance pass |
| Eye-origin projectiles replace drawn weapon sprite | current projectiles originate at `CombatSystem.eyePosition()`; no separate weapon sprite is drawn; README and story plan describe eye-origin projectiles | Met |
| Enemies except cat share player sprite | `WorldRenderer` draws non-cat enemies from `assets.enemyFrame()` plus white overlay from `enemyWhiteFrame()` | Met |
| Enemy identity differs by behavior/tint | `Enemy.whiteHint`, AI states, telegraph colors, ranged/charge/heavy/mirror behavior distinguish self fragments | Met |
| White shell recedes toward upper layers | `Enemy.layerWhite` decreases with floor; `WorldRenderer.enemyWhiteAlpha()` reduces the final boss overlay after debate stages | Met, needs visual pass |
| White fog / blank memory field | `WorldRenderer.UNEXPLORED` and `FOG` are pale white-blue overlays | Met |
| Floor 1 opening and delayed top echo | `GameWorld.OPENING_START_LINES`, `OPENING_ECHO_LINES`, and `maybeTriggerOpeningEcho()` | Met |
| Floor 1 initial encounter | `SpawnerSystem.spawnInitial()` creates 4-6 forced blank selves; no cat is spawned on floor 1 | Met against section 15; section 7 conflicts |
| Floor 2 escape self / pleaser gate | `SpawnerSystem.randomType()` and `StoryBossKind.PLEASER`; `AISystem.updatePleaserBoss()` | Met |
| Floor 3 angry self / cat boss | floor 3 uses `SKELETON` and `StoryBossKind.CAT`; cat boss has staged behavior | Met |
| Floor 4 mirror self / non-hostile cat | floor 4 uses `MIRROR`; `storyCatVisible`, shot flee logic, and spare preservation are implemented | Met |
| Floor 5 empty self / no regular enemies | `SpawnerSystem` skips non-review floor >= 5; `StoryBossKind.EMPTY_SELF` is final boss | Met |
| Final boss memory debate | floors 2-4 unlock cold-floor, broken-bowl, and under-bed bits; `tryAdvanceFinalDebate()` rejects a stage when its memory is missing | Met |
| True ending conditions | death threshold, final debate completion, cat spared, no aggression are checked in `completeRun()` | Met |
| Death memory leaks use death and floor gates | `GameOverScreen.LEAK_DEATHS` is `{1,2,4,6,8,10,12,15,17,20}` and `LEAK_FLOORS` gates later lines by reached floor | Met |
| True ending requires the silence leak | `GameWorld.hasUnlockedSilenceLeak()` requires 15 deaths and floor 4+, matching "那天我没有出声" | Met |
| Review mode unlocks after true ending | `DungeonForgeGame.showEnding()` sets review availability; `MainMenuScreen` shows a warm Review-first menu; `showReviewComplete()` clears it | Met |
| Review mode changes combat to soothing | `GameWorld.updateReview()` maps attack/skill presses to `sootheNearestReviewSelf()` and only shows "No more" when no target is near | Met |
| Review mode cats watch each self | `WorldRenderer.drawReviewCatSprites()` draws a Laika cat beside each remaining review-mode self | Met, needs visual pass |
| Floor-specific memory props | `GameWorld.seedProps()` places only 2-4 floor-specific memory props after gameplay entities; `GameAssets.createMemoryProp()` creates semantic white outlines instead of reusing unrelated sprites | Met, needs visual pass |
| Cat remains visually special without fake old asset use | Cat uses Blind Jump's Laika frames from `gameObjects.png` instead of player/enemy sprites | Met, needs visual pass |
| Cat stage readability | Cat frames are overlaid with `whiteGameObjects` hint alpha; the floor 3 cat boss reveals more original color as it weakens, and the final cat gets a small belly/rest cue | Met, needs visual pass |
| True/review ending treatment-room scene | `EndingScreen.drawTreatmentRoom()` draws a minimal room, chairs, window light, and protagonist silhouette | Met, needs visual pass |

## Section 15 Detail Status

The current build implements the section 15 encounters as playable equivalents:

- Floor 1 has blank selves, delayed top echo near the rift, and random floating-island nodes. It intentionally has no cat under the section 15 version.
- Floor 2 has escape/ranged selves and the pleaser gate boss.
- Floor 3 has angry/heavy selves and the cat boss.
- Floor 4 has mirror delayed-target behavior and a non-hostile cat spare/flee branch.
- Floor 5 has the empty self, final cat cue, and final debate.

The following section 15 details are intentionally simplified or still need
runtime proof:

- Exact idle poses such as bowing, joint-click scanning, anger loops, and
  half-body mirror split animations are represented by tint, telegraphs,
  movement behavior, particles, and short text rather than unique animation
  frames.
- Exact node counts per island type are not hard-scripted; the map remains a
  random floating-island roguelike with fixed story anchors.
- The floor 5 cat "belly" cue uses the real Laika sprite plus a small overlay
  because Blind Jump does not provide a dedicated belly-up cat frame.

## Known Non-Blocking Notes

- Class/package names still contain `DungeonForge` to avoid broad refactors.
- `PREFS_NAME` remains `DungeonForge` intentionally so existing saves are not reset.
- Internal code still has old names such as `DungeonGenerator`, `stairsPosition`, `WeaponType.SWORD`, and `Chest`; these are implementation names, while the runtime UI maps them to rift/shard/echo/gaze language.
- Cat art is not a separate PNG. It is sliced from Blind Jump's `gameObjects.png`: idle `SpriteSheet<423, 224, 32, 32>` and run `SpriteSheet<391, 253, 36, 32>`, matching the original Laika helper.
- Runtime text uses short Chinese lines through SmileySans incremental glyph loading.

## Verification Run

Last compile gate after menu scene overlays, Delaunay/MST map generation, layered props, receding white overlays, and final-memory validation:

```powershell
.\gradlew.bat clean lwjgl3:classes core:classes
```

Result: `BUILD SUCCESSFUL`.

Latest compile gate after knockoff combat, shell-crack hit feedback, expanded
relic effects, early knockoff rewards, and run highlight summaries:

```powershell
.\gradlew.bat clean lwjgl3:classes core:classes
```

Result: `BUILD SUCCESSFUL`.

Latest compile gate after this compliance pass, including menu hint isolation,
README wording cleanup, and disabling invisible runtime traps:

```powershell
.\gradlew.bat clean lwjgl3:classes core:classes
```

Result: `BUILD SUCCESSFUL`.

Latest automated generator contract test after introducing explicit seeded
generation:

```powershell
.\gradlew.bat core:test
```

Result: `BUILD SUCCESSFUL`.

Latest automated test pass after this compliance pass:

```powershell
.\gradlew.bat core:test
```

Result: `BUILD SUCCESSFUL`.

Runtime smoke attempt:

```powershell
.\gradlew.bat lwjgl3:run
```

Result: no immediate crash was observed before the 15 second tool timeout, but
the tool cannot inspect the rendered desktop window. This is not a visual pass.

The final remaining gate is a real rendered play pass across:

1. Floor 1: blank selves + delayed echo.
2. Floor 2-5: memory prop silhouettes, receding white overlays, and story bosses.
3. Floor 4: non-hostile cat spare/flee behavior.
4. Floor 5: final belly cat, three-memory validation, and memory debate.
5. True ending and review ending screens.
