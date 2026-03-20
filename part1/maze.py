from typing import Dict, List, Tuple, Set

class Maze:
    # Directions and their (row, col) deltas
    ACTIONS = ['N', 'S', 'E', 'W']
    DELTAS = {'N': (-1, 0), 'S': (1, 0), 'E': (0, 1), 'W': (0, -1)}

    # Perpendicular directions for each action
    _PERP = {
        'N': ['E', 'W'],
        'S': ['E', 'W'],
        'E': ['N', 'S'],
        'W': ['N', 'S']
    }

    def __init__(self, rows: int, cols: int, walls: Set[Tuple[int, int]],
                 rewards: Dict[Tuple[int, int], float]):
        self.rows = rows
        self.cols = cols
        self.walls = walls
        self.rewards = rewards

        # List of all non-wall states
        self.states: List[Tuple[int, int]] = []
        for r in range(rows):
            for c in range(cols):
                if (r, c) not in walls:
                    self.states.append((r, c))

        # Mapping state -> index for matrix building
        self.state_index = {s: i for i, s in enumerate(self.states)}

    def is_valid(self, r: int, c: int) -> bool:
        return 0 <= r < self.rows and 0 <= c < self.cols and (r, c) not in self.walls

    def next_state(self, state: Tuple[int, int], action: str) -> Tuple[int, int]:
        dr, dc = self.DELTAS[action]
        r, c = state
        nr, nc = r + dr, c + dc
        if self.is_valid(nr, nc):
            return (nr, nc)
        return state  # bump into wall/boundary

    def transition_probs(self, state: Tuple[int, int], action: str) -> Dict[Tuple[int, int], float]:

        probs = {}
        # intended direction
        s_intended = self.next_state(state, action)
        probs[s_intended] = probs.get(s_intended, 0.0) + 0.8

        # perpendicular directions
        for perp in self._PERP[action]:
            s_perp = self.next_state(state, perp)
            probs[s_perp] = probs.get(s_perp, 0.0) + 0.1

        return probs

    def expected_value(self, state: Tuple[int, int], action: str,
                       utility: Dict[Tuple[int, int], float]) -> float:
        probs = self.transition_probs(state, action)
        return sum(p * utility[s] for s, p in probs.items())