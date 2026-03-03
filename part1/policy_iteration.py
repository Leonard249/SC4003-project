"""
Policy iteration algorithm for the maze.
"""
import numpy as np
from typing import Dict, Tuple, List
from maze import Maze

def _policy_evaluation(maze: Maze, policy: Dict[Tuple[int, int], str],
                       gamma: float) -> Dict[Tuple[int, int], float]:
    """
    Solve the linear system for the current policy:
        U(s) = R(s) + gamma * sum_{s'} T(s, π(s), s') * U(s')
    Returns a dict mapping state -> utility.
    """
    n = len(maze.states)
    A = np.zeros((n, n))
    b = np.zeros(n)

    for i, s in enumerate(maze.states):
        b[i] = maze.rewards[s]
        probs = maze.transition_probs(s, policy[s])
        for ns, p in probs.items():
            j = maze.state_index[ns]
            A[i, j] -= gamma * p
        A[i, i] += 1.0

    # Solve the linear system
    U_vec = np.linalg.solve(A, b)
    return {s: U_vec[i] for i, s in enumerate(maze.states)}

def policy_iteration(maze: Maze, gamma: float = 0.99,
                     track_state: Tuple[int, int] = None):
    """
    Perform policy iteration.

    Returns:
        U: dict mapping state -> utility
        policy: dict mapping state -> optimal action
        history: list of utility values of track_state after each policy evaluation
    """
    # Initial policy: always go North (any fixed action works)
    policy = {s: 'N' for s in maze.states}
    if track_state is None:
        track_state = (maze.rows-1, 0) if (maze.rows-1, 0) in maze.states else maze.states[0]

    history = []
    unchanged = False

    while not unchanged:
        # Policy evaluation
        U = _policy_evaluation(maze, policy, gamma)
        history.append(U[track_state])

        # Policy improvement
        unchanged = True
        for s in maze.states:
            old_action = policy[s]
            # Find best action under current utilities
            best_action = max(maze.ACTIONS, key=lambda a: maze.expected_value(s, a, U))
            if best_action != old_action:
                policy[s] = best_action
                unchanged = False

    return U, policy, history