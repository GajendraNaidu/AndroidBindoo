# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

AndroidBindu is a "Dots and Boxes" style game for Android where a player (Jerry) competes against a computer opponent (Tom). This project uses modern Gradle build system with:
- Minimum SDK: API 21 (Android 5.0 Lollipop)
- Target/Compile SDK: API 34 (Android 14)
- AndroidX support libraries
- Java source code

## Build Commands

This project uses the modern Gradle build system. The project is configured in `build.gradle` and `app/build.gradle` files.

**Building with Android Studio:**
- Open the project in Android Studio
- Gradle will automatically sync
- Build → Make Project or Run → Run 'app'
- The compiled APK will be placed in `app/build/outputs/apk/`

**Building from command line:**
```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Install on device
./gradlew installDebug

# Clean build
./gradlew clean

# Build and install
./gradlew clean assembleDebug installDebug
```

**Running tests:**
```bash
./gradlew test
```

**Other useful commands:**
```bash
# List all available tasks
./gradlew tasks

# View dependencies
./gradlew dependencies

# View logs (requires adb)
adb logcat
```

## Architecture

The application follows a classic Android MVC pattern with a custom game rendering system:

### Activity Flow
1. **SplashActivity** - Entry point, shows splash screen for 4 seconds then transitions to GameActivity
2. **GameActivity** - Main game screen, sets up GameView and GameState, manages game lifecycle

### Core Game Components

**GameView** (app/src/main/java/com/yellowmango/bindu/GameView.java)
- Custom SurfaceView that handles rendering and touch input
- Contains inner `PaintThread` class that continuously redraws the game at ~4 FPS (250ms delay)
- Manages bitmaps for game elements (dots with different connection states, player symbols, highlights)
- Communicates score updates to GameActivity via Handler/Message pattern
- Touch handling: First tap selects a point (shows highlight), second tap draws a line if valid

**GameState** (app/src/main/java/com/yellowmango/bindu/GameState.java)
- Central game state container
- Maintains collections of: Points (grid dots), ConnectedLines (drawn lines), GameFills (completed boxes), strayFills (all possible boxes)
- Tracks whose turn it is, game over status, and current status message

**GameUtils** (app/src/main/java/com/yellowmango/bindu/GameUtils.java)
- Static utility methods for game logic
- `GuesssALine()` - Computer AI logic: prioritizes completing boxes, otherwise draws randomly
- `IsNewFillExist()` - Detects when a box is completed by checking if all 4 sides are drawn
- `CanDrawFillWithThisLine()` - Finds lines that would complete boxes (used by AI)
- Line and fill validation logic

### Game Entities

**GamePoint** (app/src/main/java/com/yellowmango/bindu/GamePoint.java)
- Represents a dot in the grid with X,Y coordinates
- Implements equals/hashCode for proper collection usage

**GameLine** (app/src/main/java/com/yellowmango/bindu/GameLine.java)
- Represents a line between two GamePoints
- Tracks whether line is drawn and who drew it (player or computer)
- Custom `equals()` method handles bidirectional line comparison

**GameFill** (app/src/main/java/com/yellowmango/bindu/GameFill.java)
- Represents a completed box defined by four corner points
- Tracks whether it's owned by computer or player
- Used for scoring

**GameScore** (app/src/main/java/com/yellowmango/bindu/GameScore.java)
- Simple data container for score tracking

### Game Flow

1. GameActivity calculates grid size based on screen dimensions and fixed cell dimensions (80x80 from strings.xml)
2. Creates grid of GamePoints and all possible GameLines and GameFills
3. Player taps two adjacent points to draw a line
4. If line completes a box, player gets another turn
5. Otherwise, computer takes turn using AI logic in `GameUtils.GuesssALine()`
6. Computer continues playing if it completes boxes
7. Game ends when all boxes are filled, winner is determined by box count

### Rendering System

GameView uses a custom rendering thread pattern:
- `PaintThread` runs continuously while surface is active
- Locks canvas, calls `doDraw()`, unlocks and posts canvas
- `doDraw()` renders in layers: background → points → lines → fills → score update
- Each point uses different bitmaps based on position (corner, edge, middle) and available connections
- Lines colored differently for player (blue) vs computer (orange)
- Fills show player/computer symbols (bitmaps: me.png / you.png)

### Handler Communication

GameView sends score updates to GameActivity using Android Handler/Message pattern:
- Bundle contains: game over flag, player scores, status message resource ID
- GameActivity updates TextView with HTML-formatted score display
- On game over, shows AlertDialog to play again

## Key Constants

Located in `app/src/main/res/values/strings.xml`:
- `column_width`: 80 pixels
- `row_height`: 80 pixels
- Grid size calculated dynamically: columns = screen_width/80, rows = screen_height/80

## Important Implementation Details

1. **Thread Safety**: GameView's PaintThread synchronizes on SurfaceHolder when drawing
2. **Bitmap Loading**: All game bitmaps loaded in `GameView.initialize()` and reused
3. **Touch Tolerance**: 20-pixel radius for point detection (see `GameView.getGamePoint()`)
4. **AI Strategy**: Computer prioritizes box completion over random moves (GameUtils.java:165-181)
5. **Game Over Detection**: Checked every score update by comparing filled boxes to total possible boxes
6. **Surface Lifecycle**: PaintThread properly paused/resumed in surfaceDestroyed/surfaceCreated

## Modifying Game Logic

**To change grid size**: Modify `column_width` and `row_height` in strings.xml

**To adjust AI difficulty**: Modify `GameUtils.GuesssALine()` - currently uses simple greedy strategy

**To change rendering speed**: Modify `PaintThread.delay` (currently 250ms = 4 FPS)

**To add multiplayer**: Set `GameState.PlayWithFriend = true` and modify `GameView.ComputerToPlay()` to skip computer turn
