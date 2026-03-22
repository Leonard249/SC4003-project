from typing import Dict, List, Tuple, Set
from config import ACTIONS, PERPENDICULAR, PROB_INTENDED, PROB_PERPENDICULAR


class Maze:
    ACTION_LIST = list(ACTIONS.keys())  # ['N', 'S', 'E', 'W']

    def __init__(self, rows: int, cols: int, walls: Set[Tuple[int, int]],
                 rewards: Dict[Tuple[int, int], float],
                 name: str = "Unnamed Maze"):
        self.rows = rows
        self.cols = cols
        self.walls = walls
        self.rewards = rewards
        self.name = name

        # Non-wall states only (walls are absent from all data structures)
        self.states: List[Tuple[int, int]] = [
            (r, c)
            for r in range(rows)
            for c in range(cols)
            if (r, c) not in walls
        ]

        # State -> index mapping (used for building the linear system in PI)
        self.state_index = {s: i for i, s in enumerate(self.states)}

    # Transition helpers
    def is_valid(self, r: int, c: int) -> bool:
        """Check if (r, c) is inside the grid and not a wall."""
        return 0 <= r < self.rows and 0 <= c < self.cols and (r, c) not in self.walls

    def next_state(self, state: Tuple[int, int], action: str) -> Tuple[int, int]:
        """Return the resulting state from attempting *action* in *state*.
        If the move hits a wall or boundary the agent stays put."""
        dr, dc = ACTIONS[action]
        nr, nc = state[0] + dr, state[1] + dc
        return (nr, nc) if self.is_valid(nr, nc) else state

    def transition_probs(self, state: Tuple[int, int],
                         action: str) -> Dict[Tuple[int, int], float]:
        """Return {next_state: probability} for taking *action* in *state*.
        Correctly accumulates probability when multiple outcomes map to the
        same cell (e.g. bouncing off a wall from two perpendicular moves)."""
        probs: Dict[Tuple[int, int], float] = {}

        # Intended direction
        s_intended = self.next_state(state, action)
        probs[s_intended] = probs.get(s_intended, 0.0) + PROB_INTENDED

        # Perpendicular directions
        for perp in PERPENDICULAR[action]:
            s_perp = self.next_state(state, perp)
            probs[s_perp] = probs.get(s_perp, 0.0) + PROB_PERPENDICULAR

        return probs

    def expected_value(self, state: Tuple[int, int], action: str,
                       utility: Dict[Tuple[int, int], float]) -> float:
        return sum(p * utility[s] for s, p in self.transition_probs(state, action).items())