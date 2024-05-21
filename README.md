# SMP Utils

A simple and small survival multiplayer server plugin that adds some QOL features.

## Features

- **Time/Weather Vote**: Players can vote for any time of day and any type of weather.
- **Death Coordinates**: Shows the coordinates of death to the player who died.
- **Death Inventory**: Players can view their inventory from before they died. Server operators can also view and retrieve items from other players' death inventories.
- **Home**: Players can teleport themselves to their respawn point (bed).
- **Suicide**: Players can kill themselves if necessary.
- **Random Teleport**: Players can teleport themselves to a random location within a specified range.

## Commands

| Command                              | Description                                                        | Permission              |
|--------------------------------------|--------------------------------------------------------------------|-------------------------|
| /vote \<type\> \<state\>             | Vote for the time of day or the weather.                           | smputils.vote           |
| /di \[player\]                       | View your or another player's inventory from before you/they died. | smputils.deathInventory |
| /home                                | Teleport yourself to your respawn point (bed).                     | smputils.home           |
| /suicide                             | Kill yourself.                                                     | smputils.suicide        |
| /rtp \<minDistance\> \<maxDistance\> | Teleport yourself to a random location within a specified range.   | smputils.randomTeleport |

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
deathInventory:
  # Whether to be able to view the inventory from before your or another player's death.
  enabled: true
home:
  # Weather to be able to teleport yourself to your respawn point.
  enabled: true
  # Delay before a player is teleported. The teleport will be canceled if the player moves or is attacked during this timeframe.
  teleportDelaySeconds: 5
suicide:
  # Weather to be able to kill yourself.
  enabled: true
randomTeleport:
  # Weather to be able to teleport to a random location.
  enabled: true
```