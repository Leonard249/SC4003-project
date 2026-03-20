"""
Labyrinth maze – 10×10 grid with many walls forming narrow corridors.
Rewards are placed at the ends of corridors; penalties guard the paths.
Tests how maze complexity (wall density) affects convergence.
"""
from maze import Maze

ROWS = 10
COLS = 10

WALLS = {
    (0, 1), (0, 4), (0, 7),
    (1, 1), (1, 5), (1, 8),
    (2, 3), (2, 5), (2, 8),
    (3, 1), (3, 3), (3, 6), (3, 8),
    (4, 0), (4, 3), (4, 5), (4, 8),
    (5, 2), (5, 5), (5, 7),
    (6, 0), (6, 4), (6, 7),
    (7, 2), (7, 4), (7, 6), (7, 9),
    (8, 1), (8, 6), (8, 8),
    (9, 3), (9, 5), (9, 8),
}

GREEN = {
    (0, 2), (0, 9), (2, 9), (3, 0),
    (5, 4), (6, 9), (8, 5), (9, 9),
}

BROWN = {
    (0, 3), (0, 6), (1, 3),
    (2, 4), (3, 4), (4, 7),
    (5, 6), (6, 3), (7, 8),
    (8, 3), (9, 7),
}

START = (5, 3)
EMPTY_REWARD = -0.05


def build() -> Maze:
    """Construct and return the labyrinth maze."""
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
    return Maze(ROWS, COLS, WALLS, rewards, name="Labyrinth 10×10 Maze")