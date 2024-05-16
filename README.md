# SMP Utils

A simple and small survival multiplayer server plugin that adds some QOL features.

## Commands

| Command                  | Description                              | Permission    |
|--------------------------|------------------------------------------|---------------|
| /vote \<type\> \<state\> | Vote for the time of day or the weather. | smputils.vote |

## Config

```yaml
vote:
  # Minimum percentage of players required for a time/weather vote to pass.
  minPlayerPercentage: 0.3
  # Cooldown of time/weather votes. Time and weather votes have separate cooldowns.
  cooldownSeconds: 300
  time:
    # Whether time voting is enabled.
    enabled: true
  weather:
    # Whether weather voting is enabled.
    enabled: true
deathCoordinates:
  # Whether to display the death coordinates to the player who died.
  enabled: true
```