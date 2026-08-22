# Paradise Beyond Walls

A small Java 11 Swing survival game. Capture every glowing point in a sector while avoiding ghosts; completing a sector automatically advances to the next one.

## Run

Compile and run from the project root:

```powershell
javac -d out (Get-ChildItem -Recurse -Filter *.java src | ForEach-Object FullName)
java -cp out paradise.Main
```

Use **WASD** or the arrow keys to move. Press **R** or **Enter** to restart after a win or loss.

## Level progression

| Level | Ghosts | Ghost speed | Capture points |
| --- | ---: | ---: | ---: |
| 1 — The Fallen Courtyard | 2 | 2 | 5 |
| 2 — The Shattered District | 3 | 3 | 7 |
| 3 — Beyond the Last Wall | 4 | 4 | 10 |

Each level has its own wall maze. Walls block both the player and ghosts, so doorways and routes matter.

## Source layout

```text
src/paradise/
├── Main.java              # Swing window startup
├── core/                  # game loop, state, level content
├── entity/                # Ghost enemy
├── input/                 # keyboard input
├── object/                # capture points
├── ui/                    # heads-up display and overlays
└── world/                 # tiles, map generation, collision
```
