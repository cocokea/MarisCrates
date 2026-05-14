# MarisCrates

MarisCrates is a Folia-safe crate plugin with physical crate locations, key storage, holograms, GUI reward previews, and SQL-backed player key data.

## What It Handles

- Named crates stored as YAML files
- Physical crate block locations in the world
- Preview and confirm GUIs before opening a reward
- Key give, take, and keyall flows
- Optional holograms above crate locations
- SQLite or MySQL storage for player keys
- PlaceholderAPI hook when present

## Requirements

- Paper / Folia 1.21+
- Java 21
- `NBTAPI` plugin
- `PlaceholderAPI` is optional

## Installation

1. Put the plugin jar in `plugins`.
2. Install `NBTAPI` on the server.
3. Start the server once.
4. Review `config.yml`, `guis.yml`, and `message.yml`.
5. Restart the server.

## Storage

MarisCrates supports:

- SQLite
- MySQL

Set the backend in `config.yml` under `storage.type`.

## Crate Files

Crates are stored here after creation:

- `plugins/MarisCrates/crates/*.yml`

Each crate file contains its color, location, and reward list.

## Arena Style Setup For A Crate

Use this flow for every new crate:

1. Stand in-game and decide the crate name.
2. Run `/crates create <name>`.
3. Look at the block that should act as the crate.
4. Run `/crates setloc <name>`.
5. Set the display color with `/crates setcolor <name> <color>`.
6. Run `/crates edit <name>`.
7. Put reward items into the edit GUI.
8. Close the GUI to save the reward list.
9. Give yourself or a test player keys with `/crates give <player> <name> <amount>`.
10. Click the crate block to confirm the preview, confirm, and reward flow.

## Detailed Setup Notes

### 1. Create the crate

```text
/crates create common
```

This creates a new crate entry and a matching crate file.

### 2. Set the crate block

```text
/crates setloc common
```

This uses the block you are currently targeting. The plugin stores that block location as the clickable crate location.

### 3. Set the crate color

```text
/crates setcolor common &#FFD700
```

The color is used in holograms, names, and crate text formatting.

### 4. Add rewards

```text
/crates edit common
```

Place the reward items into the edit GUI. When you close the GUI, the reward list is saved automatically.

### 5. Test the crate

```text
/crates give YourName common 3
```

Then click the crate block to test:

- preview GUI
- confirm GUI
- reward delivery
- key deduction
- hologram refresh

## Admin Commands

- `/crates create <crate>` - Create a new crate.
- `/crates edit <crate>` - Open the reward editor GUI.
- `/crates setloc <crate>` - Save the location of the targeted crate block.
- `/crates setcolor <crate> <color>` - Set the crate display color.
- `/crates give <player> <crate> <amount>` - Give keys.
- `/crates take <player> <crate> <amount>` - Remove keys.
- `/crates keyall <crate> <amount>` - Give keys to all online players.
- `/crates reload` - Reload files and crate data.

## Auto Keyall

`config.yml` includes an automatic keyall section:

- `auto-keyall.enabled`
- `auto-keyall.interval-minutes`
- `auto-keyall.crate`
- `auto-keyall.amount`

If enabled, the plugin periodically gives the configured key amount for the configured crate.

## Warp Support

`warp-command` in `config.yml` is used for clickable keyall chat lines.

Example:

```yml
warp-command: 'warp crates'
```

## Hologram Setup

Holograms are controlled in `config.yml`:

- `hologram.enabled`
- `hologram.y-offset`
- `hologram.view-distance`
- `hologram.text-shadow`
- `hologram.key-amount`
- `hologram.lines`

If holograms are enabled, they refresh when keys change or crate data changes.

## Files

- `config.yml` - Storage, keyall, warp, and hologram settings.
- `guis.yml` - Edit, preview, and confirm GUI layout.
- `message.yml` - Chat and actionbar messages.
- `crates/*.yml` - Saved crate definitions.

## Notes

- This plugin is marked as Folia supported.
- `NBTAPI` is a hard dependency and must be present before the plugin loads.
- If you switch from SQLite to MySQL, migrate player key data deliberately.