# CustomRank Plugin

A comprehensive and feature-rich rank management plugin for Nukkit servers, providing advanced player rank systems with time-based permissions, customizable display names, and seamless integration with popular server utilities.

## Features

### Core Functionality
- **Dynamic Rank Management**: Create, assign, and manage player ranks with customizable display names
- **Time-Based Ranks**: Grant ranks with specific durations (minutes, hours, days, years) or permanent assignments
- **Player Selector Support**: Use `@p`, `@r`, and `@s` selectors for rank operations
- **Active Rank System**: Players can select and activate their available ranks
- **Automatic Expiration**: Real-time detection and removal of expired ranks
- **Chat Integration**: Automatic rank prefix display in chat messages

### Advanced Features
- **Multi-Player Operations**: Apply rank changes to multiple players simultaneously using selectors
- **Intelligent Name Matching**: Partial player name auto-completion for commands
- **Data Migration**: Automatic conversion of legacy data formats
- **Variable Integration**: Support for Tips plugin variables (`{cusrank}`)
- **Player Join Handling**: Automatic rank application on player join
- **Rank Cleanup**: Scheduled cleanup of expired ranks to optimize performance

## Commands

### Player Commands
| Command | Permission | Description |
|---------|------------|-------------|
| `/rank` | `rank.use` | Open rank selection GUI |
| `/rank use <baseID>` | `rank.use` | Activate a specific rank you own |
| `/rank clear` | `rank.use` | Clear your currently active rank |
| `/rank help` | None | Show help menu |

### Administrative Commands
| Command | Permission | Description |
|---------|------------|-------------|
| `/rank give <player> <baseID> [duration]` | `rank.give` | Give a rank to player (duration: 1m, 2h, 3d, 1y) |
| `/rank take <player> <baseID>` | `rank.take` | Remove a rank from player |
| `/rank create <baseID> <display>` | `rank.create` | Create a new rank definition |
| `/rank delete <baseID>` | `rank.delete` | Delete a rank definition |
| `/rank listall` | `rank.listall` | List all available ranks |
| `/rank check <player>` | `rank.check` | Check player's ranks and expiration |

### Command Examples
```bash
# Give a permanent VIP rank to a player
/rank give PlayerName VIP

# Give a 7-day VIP rank to nearest player
/rank give @p VIP 7d

# Create a new rank
/rank create MVP "§6★ §eMVP §6★"

# Check player's ranks
/rank check PlayerName

# Give rank to random player for 30 minutes
/rank give @r Helper 30m
```

## Build Support

### Prerequisites
- Java 21 or higher
- Maven 3.6+
- Nukkit API

### Build Steps
1. Clone the repository:
```bash
git clone https://github.com/GongSunFangYun/CustomRank.git
```

2. Navigate to the project directory:
```bash
cd CustomRank
```

3. Install Requirements and Build the plugin:
```bash
mvn install
mvn clean package
```

### Dependencies
The plugin requires the following dependencies during compilation:
- Nukkit API (included via Maven)
- Lombok (for cleaner code, optional)

## Runtime Dependencies

### Required Dependencies
1. **Nukkit Server**
   - Version: 1.0.0 or higher
   - The core server software required to run the plugin

2. **Tips Plugin** (Highly Recommended)
   - Resource URL: https://cloudburstmc.org/resources/tips.863/
   - Enables `{cusrank}` variable support in scoreboards and other displays
   - Provides variable replacement for player ranks
   - **Note**: Plugin will function without Tips, but variable features will be disabled

### Optional Integrations
1. **MemoriesOfTime GameCore**
   - Resource URL: https://cloudburstmc.org/resources/memoriesoftime-gamecore.717/
   - Enhances server performance and provides additional API features
   - Not required but recommended for optimal performance

2. **ScoreboardAPI**
   - Resource URL: https://cloudburstmc.org/resources/scoreboardapi.565/
   - Enables scoreboard integration with rank displays
   - Works well with Tips plugin variables

## Architecture

### Plugin Components
- **RankMain**: Core plugin class, handles initialization and data management
- **RankCommand**: Command executor and event handler for rank operations
- **RankScheduler**: Manages rank expiration and periodic cleanup tasks
- **RankForm**: GUI interface for player rank selection
- **RankVariable**: Variable provider for Tips plugin integration
- **RankVarRegister**: Handles variable registration and updates

### Data Storage
- **player_ranks.json**: Stores player-specific rank data including active ranks and expiration times
- **base_ranks.json**: Stores rank definitions (baseID → display name)

## Configuration

The plugin automatically creates necessary configuration files on first run:
- `plugins/CustomRank/player_ranks.json` - Player rank assignments
- `plugins/CustomRank/base_ranks.json` - Rank definitions

### Permissions
```yaml
rank.give: Allows giving ranks to players
rank.take: Allows taking ranks from players
rank.create: Allows creating new rank definitions
rank.delete: Allows deleting rank definitions
rank.use: Allows players to use their ranks
rank.listall: Allows listing all available ranks
rank.check: Allows checking player ranks
```

## Use Cases

### Server Applications
- **Role-Based Systems**: Implement VIP, MVP, Helper, Moderator ranks
- **Temporary Rewards**: Time-limited ranks for events or promotions
- **Progression Systems**: Rank upgrades based on playtime or achievements
- **Staff Management**: Visual identification of server staff
- **Donor Benefits**: Special ranks for supporting players

### Integration Examples
```bash
# Combine with economy plugins
/rank give @p Donor 30d

# Staff promotion workflow
/rank create Staff "§c[Staff]"
/rank give NewStaffMember Staff

# Event rewards
/rank give @r EventWinner "§6Event Winner" 24h
```

## Performance

- **Efficient Scheduling**: Uses Nukkit's scheduler for non-blocking operations
- **Memory Optimization**: Rank caching system for fast variable lookups
- **Data Integrity**: Automatic backup and migration of old data formats
- **Cleanup System**: Regular removal of expired ranks to prevent data bloat

## Support

For issues, feature requests, or contributions:
1. Check existing issues on the repository
2. Ensure you have the required dependencies installed
3. Provide detailed error logs if reporting bugs
4. Follow the existing code style for contributions

## 📝 License

GPL-V3.0 License
(Because it inherits from a class in the Tips plugin, but Tips is open-sourced under the GPL-V3.0 license.)

---

*Note: This plugin is designed for Nukkit servers and requires a compatible server implementation. Always test plugins in a development environment before deploying to production.*