# GlitchDeath – Minecraft 26.2

Server-side Fabric mod for Minecraft 26.2.

When an invisible player kills someone, the vanilla death message is kept intact and only the invisible killer's name is replaced according to **that killer's own setting**.

## Player commands

Every player can use these commands; OP is not required:

- `/glitchdeath glitch` – replace your killer name with obfuscated `GLITCH`
- `/glitchdeath name` – use your normal/display name
- `/glitchdeath random` – use a random Minecraft-style name
- `/glitchdeath status` – show your current mode
- `/glitchdeath mode <glitch|name|random>` – same as the commands above

The setting is stored by the player's UUID in `config/glitchdeath.properties`, so it survives server restarts.

The default mode for a player who has never selected one is `glitch`.

## Example

If the vanilla message is:

`Steve was killed by Freddy`

and Freddy is invisible:

- Freddy = `name` → `Steve was killed by Freddy`
- Freddy = `glitch` → `Steve was killed by GLITCH` with the Minecraft obfuscated effect
- Freddy = `random` → `Steve was killed by Shadow4821` (example)

Only the killer's name component is replaced. The rest of the original vanilla death message remains unchanged.
