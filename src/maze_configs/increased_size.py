"""
Increased-size maze – 10×10 grid with scattered rewards and penalties.
Tests how increasing state count affects convergence.
"""
from maze import Maze

ROWS = 10
COLS = 10

WALLS = {
    (1, 3), (2, 0), (2, 2), (2, 5),
    (3, 1), (3, 9),
    (4, 4), (5, 6),
    (6, 5), (6, 6), (6, 8),
    (7, 1), (7, 4),
    (9, 5),
}

GREEN = {
    (0, 0), (0, 9), (1, 1), (1, 6), (1, 7), (1, 8),
    (3, 0), (3, 5), (5, 8), (5, 9), (8, 9),
}

BROWN = {
    (0, 3), (0, 4), (1, 0), (2, 8),
    (4, 5), (4, 6), (4, 7),
    (5, 3), (6, 0),
    (7, 2), (7, 7), (8, 1),
}

START = (4, 3)
EMPTY_REWARD = -0.05


def build() -> Maze:
    """Construct and return the increased-size maze."""
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
    return Maze(ROWS, COLS, WALLS, rewards, name="Increased-Size 10×10 Maze")