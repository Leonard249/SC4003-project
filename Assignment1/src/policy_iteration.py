import random
import numpy as np
from typing import Dict, Tuple, List
from maze import Maze
from config import DISCOUNT_FACTOR


def _policy_evaluation(maze: Maze,
                       policy: Dict[Tuple[int, int], str],
                       gamma: float) -> Dict[Tuple[int, int], float]:
    n = len(maze.states)
    A = np.zeros((n, n))
    b = np.zeros(n)

    for i, s in enumerate(maze.states):
        b[i] = maze.rewards[s]
        probs = maze.transition_probs(s, policy[s])
        for ns, p in probs.items():
            j = maze.state_index[ns]
            A[i, j] -= gamma * p
        A[i, i] += 1.0          # the "I" in (I − γ T_π)

    U_vec = np.linalg.solve(A, b)
    return {s: float(U_vec[i]) for i, s in enumerate(maze.states)}


# Full policy iteration loop
def policy_iteration(maze: Maze, gamma: float = DISCOUNT_FACTOR,
                     track_state: Tuple[int, int] = None):
    """
    Perform policy iteration on *maze*.

    Parameters
    ----------
    maze        : Maze instance.
    gamma       : Discount factor (default from config).
    track_state : If given, record its utility after every evaluation.

    Returns
    -------
    U       : dict  – state -> converged utility
    policy  : dict  – state -> optimal action character
    history : list  – utility of *track_state* at each evaluation step
    """
    # Random init policy (adopted from the report)
    policy: Dict[Tuple[int, int], str] = {
        s: random.choice(maze.ACTION_LIST) for s in maze.states
    }

    if track_state is None:
        track_state = maze.states[0]

    history: List[float] = []
    unchanged = False

    while not unchanged:
        # eval: exact solve
        U = _policy_evaluation(maze, policy, gamma)
        history.append(U[track_state])

        # improvement: greedy policy update
        unchanged = True
        for s in maze.states:
            old_action = policy[s]
            best_action = max(maze.ACTION_LIST,
                              key=lambda a: maze.expected_value(s, a, U))
            if best_action != old_action:
                policy[s] = best_action
                unchanged = False

    return U, policy, history