from maze import Maze

ROWS = 10
COLS = 10

WALLS = {
    (1, 1), (1, 8),
    (3, 3), (3, 6),
    (4, 1), (4, 8),
    (6, 3), (6, 6),
    (8, 1), (8, 8),
}

GREEN = {
    (0, 4), (0, 5),
}

BROWN = {
    (1, 2), (1, 4), (1, 5), (1, 7),
    (2, 1), (2, 8),
    (3, 4), (3, 5),
    (4, 0), (4, 3), (4, 6), (4, 9),
    (5, 1), (5, 8),
    (6, 4), (6, 5),
    (7, 1), (7, 4), (7, 5), (7, 8),
    (8, 2), (8, 7),
}

START = (9, 4)  # cols 4 and 5 are both marked S; use col 4 as primary start
EMPTY_REWARD = -0.05


def build() -> Maze:
    """Construct and return the minefield maze."""
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
    return Maze(ROWS, COLS, WALLS, rewards, name="Minefield 10×10 Maze")