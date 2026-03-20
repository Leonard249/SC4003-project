"""
Base maze environment – the 6x6 grid given in the SC4003 assignment.

Layout (row, col), 0-indexed, row 0 = top:

   Col:  0      1      2      3      4      5
  ───────────────────────────────────────────────
  Row 0: +1   Wall    +1   white  white   +1
  Row 1: white  -1   white   +1   Wall    -1
  Row 2: white white   -1   white  +1    white
  Row 3: white white  Start  -1   white   +1
  Row 4: white Wall   Wall   Wall   -1   white
  Row 5: white white  white  white white  white
"""
from maze import Maze
import numpy as np

ROWS = 6
COLS = 6

WALLS = {(0, 1), (1, 4), (4, 1), (4, 2), (4, 3)}

GREEN = {(0, 0), (0, 2), (0, 5), (1, 3), (2, 4), (3, 5)}   
BROWN = {(1, 1), (1, 5), (2, 2), (3, 3), (4, 4)}            # -1

START = (3, 2)          # Agent's initial position (for reference)
EMPTY_REWARD = -0.05    # White cells


def build() -> Maze:
    """Construct and return the base maze."""
    rewards = {}
    for r in range(ROWS):
        for c in range(COLS):
            if (r, c) in WALLS:
                continue
            if (r, c) in GREEN:
                rewards[(r, c)] = 1.0
            elif (r, c) in BROWN:
                rewards[(r, c)] = -1.0
            else:
                rewards[(r, c)] = EMPTY_REWARD
    return Maze(ROWS, COLS, WALLS, rewards, name="Base 6×6 Maze")

import matplotlib.pyplot as plt
import numpy as np

def plot_labyrinth(maze):
    """
    Plots the 10x10 Labyrinth maze.
    Walls: Black | Rewards (Green): Green | Penalties (Brown): Brown | Start: Blue
    """
    # Create a numeric grid for plotting
    # 0: Path, 1: Wall, 2: Green Reward, 3: Brown Penalty
    grid = np.zeros((maze.rows, maze.cols))
    
    # Fill grid based on maze data
    for r in range(maze.rows):
        for c in range(maze.cols):
            if (r, c) in maze.walls:
                grid[r, c] = 1
            elif maze.rewards.get((r, c)) == 1.0:
                grid[r, c] = 2
            elif maze.rewards.get((r, c)) == -1.0:
                grid[r, c] = 3
    
    # Define custom colors
    # Path (White), Wall (Black), Green (+1), Brown (-1)
    cmap = plt.matplotlib.colors.ListedColormap(['white', '#333333', '#4CAF50', '#8B4513'])
    
    fig, ax = plt.subplots(figsize=(8, 8))
    ax.imshow(grid, cmap=cmap)

    # Add the Start point marker
    start_r, start_c = (2,3) # Based on your START variable
    ax.plot(start_c, start_r, 'bo', markersize=15, label='Start (5,3)')

    # Formatting the grid lines and ticks
    ax.set_xticks(np.arange(-.5, maze.cols, 1), minor=True)
    ax.set_yticks(np.arange(-.5, maze.rows, 1), minor=True)
    ax.grid(which='minor', color='black', linestyle='-', linewidth=1)
    
    # Labeling axes
    ax.set_xticks(range(maze.cols))
    ax.set_yticks(range(maze.rows))
    ax.set_title(f"{maze.name}", fontsize=15)
    
    plt.legend(loc='upper right')
    plt.show()

# Example usage:
maze_instance = build()
plot_labyrinth(maze_instance)