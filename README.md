# ● Connect Four

**A classic two-player strategy game, built from scratch in Java**

---

## In One Sentence

Connect Four is a turn-based board game for two players who drop tokens into a 7-column grid, racing to be the first to line up four of their tokens in a row — across, up, or diagonally.

## How the Game Works

- The board is **6 rows tall by 7 columns wide**.
- Player 1 uses `X` (red), Player 2 uses `O` (yellow), `.` is empty.
- On each turn you either **add** a token to a column (it falls to the lowest free spot) or **remove** one of your own tokens from the bottom of a column (the tokens above it slide down).
- The first player to connect **four in a row** — horizontally, vertically, or diagonally — wins. A completely full board with no line is a tie.

## How It's Built (The Design)

This project is built around an **abstract class** called `AbstractStrategyGame`. It defines the "shape" every turn-based, perfect-information game must have: report the game state, whose turn it is, whether anyone has won, read a move, and make a move.

Connect Four is one concrete game that fills in that shape. Because it follows the same blueprint, the **same game-runner** (`Client`) can play it without any changes — and any other game built on the same blueprint could plug in too. This is the power of abstraction: **one driver, many games.**

```
AbstractStrategyGame   (the blueprint / rules of "what a game must do")
        ▲
        │  extends
        │
  ┌─────┴────────────────────────┐
ConnectFour              (your future games...)
```

## What's in This Repository

| File | Purpose |
|------|---------|
| `Run_Game.command` | Double-click this to compile and play the game (opens the graphical window) |
| **`Game_Code/`** | **The actual Connect Four game** |
| `Game_Code/ConnectFour.java` | **The main code.** The Connect Four game itself: the board, the moves, and the win-checking |
| `Game_Code/ConnectFourGUI.java` | The point-and-click game **window** (red vs. yellow discs, drop animation, win highlighting). It drives the same `ConnectFour` rules with the mouse |
| `Game_Code/AbstractStrategyGame.java` | The blueprint every game follows (abstract class) |
| `Game_Code/Client.java` | The text game-runner: prints the board, asks each player for a move, and announces the winner |
| **`Guides/`** | **Documentation** |
| `Guides/ABOUT.txt` | What the project is and does |
| `Guides/HOW_TO_RUN.txt` | Step-by-step instructions for beginners |
| `Guides/GAME_CODE.txt` | The main code used to build the game, with notes |

## Quick Start

```bash
# Compile the game
cd Game_Code
javac *.java

# Play with the graphical window (red vs. yellow discs)
java ConnectFourGUI

# Or play the classic text version in the terminal
java Client
```

Or just double-click `Run_Game.command` (macOS) to compile and launch automatically.

## Built With

| Tool | Role |
|------|------|
| Java (JDK 21) | The programming language and compiler |
| Java Swing | The built-in toolkit for the point-and-click window |
| JUnit 5 | The framework used to run the automated tests |

The game runs as a colourful desktop window (and also has a classic text-terminal version) — no extra software needed beyond Java.

## Why It Matters

Building Connect Four on top of a shared abstract class is a hands-on lesson in one of the most important ideas in programming: separating **"what something must do"** (the abstract blueprint) from **"how a specific thing does it"** (the concrete game). Master this, and you can grow a whole family of games that all plug into the same machinery.

---

*New here? Open `Guides/HOW_TO_RUN.txt` next.*
