# SystemClient — clean Fabric client template (Minecraft 1.21.11)

A clean, **modular client-side** Fabric template: a `Module` architecture with
settings, a ClickGUI, keybind capture and JSON persistence. It ships three
ordinary quality-of-life modules (Auto Refill, Toggle Sprint, HUD Info) and
deliberately contains no combat/exploit switches — it is meant as a starting
point for writing your own client-side features on top of the vanilla,
server-approved interaction model.

## Toolchain (verified October 2026)

| Component       | Version              |
|-----------------|----------------------|
| Minecraft       | 1.21.11              |
| Yarn mappings   | 1.21.11+build.6      |
| Fabric Loader   | 0.20.1               |
| Fabric API      | 0.141.6+1.21.11      |
| Loom            | 1.18-SNAPSHOT        |
| Gradle wrapper  | 9.7.1 (official Fabric 1.21.11 template version) |
| Java            | 21                   |

## Build & run

```sh
./gradlew build      # produces build/libs/systemclient-1.0.0.jar
./gradlew runClient  # run in a dev environment
```

GitHub Actions (`.github/workflows/build.yml`) builds the jar on every push to
`main`/`master` and uploads it as the `systemclient-build` artifact.
On Windows without a `gradlew.bat` wrapper, run `sh gradlew ...` from Git Bash.

## Features

- **Modular architecture** — `Module` base class (`onTick`, `onEnable`,
  `onDisable`), categories, settings (`BooleanSetting`, `NumberSetting`) and a
  central `ModuleManager`.
- **Working keybind system** — every module (and the ClickGUI itself) has its
  own persisted keybind. Binds are our own GLFW bindings with edge detection:
  set them in the ClickGUI ("Bind:" row), press ESC/Backspace to clear, and
  they toggle exactly once per press. No vanilla key registration quirks.
- **ClickGUI** — one draggable panel per category. Left click toggles a
  module, right click expands settings, slider rows drag, panel positions
  persist. Open it with **Right Shift** by default (rebindable in the GUI).
- **Persistence** — modules, enabled state, custom binds, setting values and
  panel positions are saved to `config/systemclient.json` on every change.

## Modules included

- **Auto Refill** (Inventory) — hotbar slot range **freely configurable from
  slot 0 to slot 8** (Pre-1.8-style lock replaced by *Min slot* / *Max slot*
  sliders), plus a refill threshold and a swap delay. Refills by sending the
  same vanilla slot-swap packet the inventory screen itself sends.
- **Toggle Sprint** (Movement) — sprints while you move forward.
- **HUD Info** (Render) — FPS, position and GUI keybind overlay.

## Adding your own module

1. Create a class extending `Module`, call `super(name, description,
   ModuleCategory.YOUR_CATEGORY)` and add settings via `addSettings(...)`.
2. Put your logic in `onTick(MinecraftClient)` (runs while enabled).
3. Register it in `ModuleManager.init()`.

Keybinds, enabled state and settings persist automatically — no extra code.

## Project layout

```
src/main/java/com/systemclient/
├── SystemClient.java               # entry point (client initializer)
├── config/ConfigManager.java       # JSON persistence
├── gui/ClickGUIScreen.java         # ClickGUI screen
├── gui/Panel.java                  # panel geometry
└── module/
    ├── Module.java                 # base class + keybind handling
    ├── ModuleCategory.java
    ├── ModuleManager.java
    ├── modules/AutoRefillModule.java
    ├── modules/ToggleSprintModule.java
    ├── modules/HudInfoModule.java
    └── settings/…                  # Setting, BooleanSetting, NumberSetting
```
