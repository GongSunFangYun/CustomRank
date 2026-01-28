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

# Building from Source

## Prerequisites
- **Java 21 or higher**
- **Apache Maven 3.6+**
- **Git**

## Clone the Repository and Install Requirements
```bash
git clone https://github.com/GongSunFangYun/CustomRank.git
cd CustomRank
mvn install
```

## Project Structure
```
CustomRank/
├── src/main/java/cn/gsfy/
│   ├── RankMain.java          # Main plugin class
│   ├── RankCommand.java       # Command handler and event listener
│   ├── RankForm.java          # GUI form manager
│   ├── RankScheduler.java     # Rank expiration scheduler
│   ├── RankVariable.java      # Tips variable integration
│   └── RankVarRegister.java   # Variable registration
├── pom.xml                    # Maven configuration
└── README.md                  # Documentation
```

## Build Process

### 1. Compile the Plugin
```bash
mvn clean compile
```

### 2. Package into JAR
```bash
mvn package
```

### 3. Locate the Output
The compiled JAR file will be available at:
```
target/CustomRank-1.6.8.jar
```

## Maven Configuration Details

The `pom.xml` file configures the following:

### Java Version
- **Source Compatibility**: Java 21
- **Target Compatibility**: Java 21

### Dependencies
```xml
<dependency>
    <groupId>cn.nukkit</groupId>
    <artifactId>nukkit</artifactId>
    <version>1.0-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>

<dependency>
	<groupId>com.smallaswater.tips</groupId>
	<artifactId>Tips</artifactId>
	<version>2.2.2</version>
	<scope>provided</scope>
</dependency>
```

**Note**: The `provided` scope means Nukkit API is expected to be available at runtime and is not bundled in the final JAR.

### Build Process
- Standard Maven lifecycle (compile, test, package)
- UTF-8 encoding for all source files
- No external dependencies beyond Nukkit API

## Development Setup

### Import to IDE
- **IntelliJ IDEA**: `File → Open → Select pom.xml`
- **Eclipse**: `File → Import → Maven → Existing Maven Projects`

### Project Structure
- **Source code**: `src/main/java/`
- **Resources**: `src/main/resources/`
- **Tests**: `src/test/java/` (if applicable)

## Quick Build Scripts

### Linux/Mac (`build.sh`)
```bash
#!/bin/bash
echo "Building CustomRank..."
mvn clean package
if [ $? -eq 0 ]; then
    echo "Build successful! JAR file: target/CustomRank-1.6.8.jar"
else
    echo "Build failed!"
    exit 1
fi
```

### Windows (`build.bat`)
```batch
@echo off
echo Building CustomRank...
call mvn clean package
if %errorlevel% equ 0 (
    echo Build successful! JAR file: target/CustomRank-1.6.8.jar
) else (
    echo Build failed!
    exit /b 1
)
```

Make the script executable (Linux/Mac):
```bash
chmod +x build.sh
```

## Testing the Build

After successful build, you can test the plugin by:

1. **Copy the JAR** to your Nukkit server's plugins folder:
```bash
cp target/CustomRank-1.6.8.jar /path/to/nukkit/plugins/
```

2. **Start/Restart** your Nukkit server

3. **Verify the plugin loads** in the console:
```
[CustomRank] Plugin successfully enabled!
[CustomRank] Registered {cusrank} variable successfully
```

## Troubleshooting

### Common Issues

#### Maven not found
**Ubuntu/Debian:**
```bash
sudo apt-get install maven
```

**macOS:**
```bash
brew install maven
```

**Windows:**
Download from [Apache Maven website](https://maven.apache.org/download.cgi)

#### Java version mismatch
Ensure Java 21 is installed and set as default:
```bash
java -version
# Should show version 21 or higher
```

Set JAVA_HOME (if needed):
```bash
# Linux/Mac
export JAVA_HOME=/path/to/java21

# Windows
set JAVA_HOME=C:\path\to\java21
```

#### Nukkit dependency not resolved
Make sure you have access to the Nukkit Maven repository. If using a custom Nukkit build, install it to your local Maven repository:
```bash
mvn install:install-file \
    -Dfile=path/to/nukkit.jar \
    -DgroupId=cn.nukkit \
    -DartifactId=nukkit \
    -Dversion=1.0-SNAPSHOT \
    -Dpackaging=jar
```

#### Build failures
Clean the project and rebuild:
```bash
mvn clean
mvn compile
```

Check for compilation errors:
```bash
mvn compile 2>&1 | grep -A 5 -B 5 "error"
```

## Continuous Integration

The project is Maven-ready for CI/CD pipelines. Sample GitHub Actions workflow (`.github/workflows/build.yml`):

```yaml
name: Build CustomRank

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 21
      uses: actions/setup-java@v3
      with:
        java-version: '21'
        distribution: 'temurin'
        cache: 'maven'
    
    - name: Build with Maven
      run: mvn clean package
      
    - name: Upload Artifact
      uses: actions/upload-artifact@v3
      with:
        name: CustomRank
        path: target/*.jar
        retention-days: 7
```

## Version Information

- **Current Version**: 1.6.8
- **Nukkit Compatibility**: 1.0.0+
- **Build Tool**: Apache Maven 3.6+
- **Java Version**: 21+

## Release Build

To create a release build with optimized settings:

```bash
# Skip tests and create optimized build
mvn clean package -DskipTests

# Create JAR with timestamp
mvn clean package -DbuildTimestamp=$(date +%Y%m%d%H%M%S)
```

## Dependency Management

This plugin has minimal external dependencies:
- **Required**: Nukkit API (provided at runtime)
- **Optional**: Tips plugin (runtime only, for variable support)
- **No bundled dependencies**: All dependencies are `provided` scope

The build process follows standard Maven conventions, making it easy to integrate with existing development workflows and CI/CD pipelines.

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

## License

GPL-V3.0 License
(Because it inherits from a class in the Tips plugin, but Tips is open-sourced under the GPL-V3.0 license.)