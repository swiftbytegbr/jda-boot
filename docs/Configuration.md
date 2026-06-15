| Key                            | Description                                                                                                                                                     | Default           |
|--------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------|
| `discord.token`                | Discord application token used to authenticate with the Discord API. Create one at the [Discord Developer Portal](https://discord.com/developers/applications). | N/A               |
| `discord.sharding.enabled`     | Enables sharding. When disabled, JDABoot starts exactly one shard and ignores the remaining sharding settings.                                                  | `false`           |
| `discord.sharding.totalShards` | Total number of shards used by the Discord bot across all running processes.                                                                                    | `1`               |
| `discord.sharding.minShardId`  | Lowest shard ID assigned to this process.                                                                                                                       | `0`               |
| `discord.sharding.maxShardId`  | Highest shard ID assigned to this process. The value is inclusive and must be lower than `discord.sharding.totalShards`.                                        | `totalShards - 1` |
| `scheduler.threadPoolSize`     | Number of threads available for scheduled tasks.                                                                                                                | `5`               |

When a bot is split across multiple processes, every process must use the same
`discord.sharding.totalShards` value. The configured shard ID ranges must not overlap.
