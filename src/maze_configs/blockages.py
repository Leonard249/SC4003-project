"""
Blockages maze – 10×10 grid with horizontal wall barriers and small gates.
Rewards are concentrated on the top side; the agent starts at the bottom.
Tests whether the algorithms can navigate through narrow openings.
"""
from maze import Maze

ROWS = 10
COLS = 10

# Three horizontal wall barriers, each with 1-2 gaps ("gates")
WALLS = {
    # Barrier 1 (row 2): gate at col 1 and col 7
    (2, 0), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6), (2, 8), (2, 9),
    # Barrier 2 (row 5): gate at col 2 and col 6
    (5, 0), (5, 1), (5, 3), (5, 4), (5, 5), (5, 7), (5, 8), (5, 9),
    # Barrier 3 (row 8): gate at col 4
    (8, 0), (8, 1), (8, 2), (8, 3), (8, 5), (8, 6), (8, 7), (8, 8), (8, 9),
}

GREEN = {
    (0, 1), (0, 4), (0, 8),   # Rewards at the top
    (1, 6),
}

BROWN = {
    (0, 2), (0, 5),            # Penalties near rewards
    (1, 0), (1, 8),
    (3, 3), (6, 5),
}

START = (9, 4)
EMPTY_REWARD = -0.05


def build() -> Maze:
    """Construct and return the blockages maze."""
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
    return Maze(ROWS, COLS, WALLS, rewards, name="Blockages 10×10 Maze")