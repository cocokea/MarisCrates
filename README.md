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
- Java 21+
- `NBTAPI` plugin
- `PlaceholderAPI` is optional

## Installation

1. Put the plugin jar in `plugins`.
2. Install `NBTAPI`.
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

Each crate file contains:

- crate name
- display color
- world location
- reward list

## Quick Setup

Use this flow for every new crate:

1. Run `/crates create <name>`.
2. Look at the block that should act as the crate.
3. Run `/crates setloc <name>`.
4. Run `/crates setcolor <name> <color>`.
5. Run `/crates edit <name>` and place reward items.
6. Close the edit GUI to save rewards.
7. Run `/crates give <player> <name> <amount>` for testing.
8. Click the crate block and verify preview, confirm, reward, and key deduction.

## Detailed Setup

### 1. Create the crate

```text
/crates create common
```

This creates a crate entry and the corresponding crate file.

### 2. Bind the physical block

```text
/crates setloc common
```

The plugin uses your currently targeted block as the clickable crate location.

### 3. Set crate color

```text
/crates setcolor common &#FFD700
```

The color is used in hologram lines and formatted crate text.

### 4. Add rewards

```text
/crates edit common
```

Place reward items into the editor GUI. When you close the GUI, the reward set is saved to the crate file.

### 5. Test key flow

```text
/crates give YourName common 3
```

Then click the crate block and verify:

- preview GUI opens
- confirm GUI opens
- reward is granted
- one key is removed
- hologram updates

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

`config.yml` includes:

- `auto-keyall.enabled`
- `auto-keyall.interval-minutes`
- `auto-keyall.crate`
- `auto-keyall.amount`

If enabled, the plugin periodically gives the configured key amount for the configured crate.

## Warp Support

`warp-command` in `config.yml` is used for clickable keyall messages.

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

## Files

- `config.yml` - Storage, keyall, warp, and hologram settings.
- `guis.yml` - Edit, preview, and confirm GUI layout.
- `message.yml` - Chat and actionbar messages.
- `crates/*.yml` - Saved crate definitions.

## Common Mistakes

- Missing `NBTAPI` prevents startup.
- Forgetting `/crates setloc` leaves a crate file with no usable world block.
- Editing rewards without closing the GUI means the save callback never fires.

## Notes

- This plugin is marked as Folia supported.
- If you switch from SQLite to MySQL, migrate player key data deliberately.