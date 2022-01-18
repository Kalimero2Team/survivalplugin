# Survival Plugin from Kalimero2's Community Server

## Currently contains:

- Chunk Claiming
- Discord Whitelist Bot
- An Introduction Command

## Commands:



| Command                                | Description                                                                     | Permission                              | Alias                            |
| -------------------------------------- | ------------------------------------------------------------------------------- | --------------------------------------- | -------------------------------- |
| **/admin**                             |                                                                                 |                                         |                                  |
| /admin reload                          | Reloads the Plugin                                                              | survivalplugin.admin.reload             |                                  |
| /admin database reload                 | Reloads the Database                                                            | survivalplugin.admin.database.reload    |                                  |
| /admin database purge [player]         | Removes Player from Whitelist                                                   | survivalplugin.admin.database.purge     |                                  |
| /admin alts [player]                   | Shows alternative Accounts linked to the same Discord Account                   | survivalplugin.admin.alts               |                                  |
| /admin tab header [header]             | Sets the Player List Header                                                     | survivalplugin.admin.tab.header         |                                  |
| /admin tab header [footer]             | Sets the Playe List Footer                                                      | survivalplugin.admin.tab.footer         |                                  |
| /admin claims [player]                 | Shows the claimed Chunks owned by the Player                                    | survivalplugin.admin.claims             |                                  |
| /admin unclaim-all [player]            | Removes all Claims owned by the Player                                          | survivalplugin.admin.unclaim-all        |                                  |
| /admin chunk claim [message]           | Creates a Team/Admin Claim                                                      | survivalplugin.admin.claim              |                                  |
| /admin chunk unclaim                   | Removes a Team/Admin Claim                                                      | survivalplugin.admin.unclaim            |                                  |
| /admin set-max-claims [player]         | Changes the Claim Limit of the Player                                           | survivalplugin.admin.set-max-claims     |                                  |
| /admin tpchunk [x] [z]                 | Teleports you to the Chunk                                                      | survivalplugin.admin.tpchunk            |                                  |
| /admin portal end [true/false]         | Enables/Disables End Portals                                                    | survivalplugin.admin.portal.end         |                                  |
| /admin portal end-gateway [true/false] | Enables/Disables End Gateways                                                   | survivalplugin.admin.portal.end-gateway |                                  |
| /admin portal nether [true/false]      | Enables/Disables Nether Portals                                                 | survivalplugin.admin.portal.nether      |                                  |
| /admin max-players [amount]            | Changes the Player Cap                                                          | survivalplugin.admin.max-players        |                                  |
| /admin maintenance on [text]           | Enables the Maintenace Mode with the given Text as the Kick Reason              | survivalplugin.admin.maintenance        |                                  |
| /admin maintenance off                 | Disables the Maintenace Mode                                                    | survivalplugin.admin.maintenance        |                                  |
| **/chunk**                             |                                                                                 |                                         |                                  |
| /chunk                                 | Shows Info about the current Chunk                                              |                                         |                                  |
| /chunk add [player]                    | Adds the Player to the current Chunk                                            |                                         |                                  |
| /chunk remove [player]                 | Removes the Player to the current Chunk                                         |                                         |                                  |
| /chunk claim                           | Claims the current Chunk as yours                                               |                                         |                                  |
| /chunk unclaim                         | Removes the Claim.                                                              |                                         |                                  |
| /chunk border                          | Enables a Chunk Border (Useful for Bedrock Players)                             |                                         | /cb<br/>/chunkborder             |
| /chunk force                           | Toggles Force Mode                                                              | survivalplugin.chunk.force              |                                  |
| **/discord**                           |                                                                                 |                                         |                                  |
| /discord [Name]                        | Shows the Discord Account of the Player                                         |                                         |                                  |
| **/introduction**                      |                                                                                 |                                         |                                  |
| /introduction                          | Shows the Player a Book/Form (Java/Bedrock) with Introductions about the Server |                                         | /info<br/>/intro<br/>/einführung |
| **/mute**                              |                                                                                 |                                         |                                  |
| /mute [player]                         | Mutes/Unmutes the Player                                                        | survivalplugin.mute                     |                                  |
| /mute global                           | Mutes/Unmutes the global Chat                                                   | survivalplugin.mute.global              |                                  |
| **/spawn**                             |                                                                                 |                                         |                                  |
| /spawn                                 | Teleports you to the World Spawn (only in Overworld)                            |                                         |                                  |


