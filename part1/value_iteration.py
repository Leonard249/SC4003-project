from typing import Dict, Tuple, List
from maze import Maze

def value_iteration(maze: Maze, gamma: float = 0.99, epsilon: float = 1e-4,
                    track_state: Tuple[int, int] = None):
    """
    Perform value iteration.

    Returns:
        U: dict mapping state -> utility
        policy: dict mapping state -> optimal action
        history: list of utility values of track_state at each iteration
    """
    # Init utilities to 0
    U = {s: 0.0 for s in maze.states}
    if track_state is None:
        # default to bottom-left corner if exists, else first state
        track_state = (maze.rows-1, 0) if (maze.rows-1, 0) in maze.states else maze.states[0]

    history = []

    while True:
        U_new = {}
        delta = 0.0
        for s in maze.states:
            # Bellman update: U(s) = R(s) + gamma * max_a expected value
            best = max(maze.expected_value(s, a, U) for a in maze.ACTIONS)
            U_new[s] = maze.rewards[s] + gamma * best
            delta = max(delta, abs(U_new[s] - U[s]))

        U = U_new
        history.append(U[track_state])

        if delta < epsilon:
            break

    # Derive optimal policy from final utilities
    policy = {}
    for s in maze.states:
        best_action = max(maze.ACTIONS, key=lambda a: maze.expected_value(s, a, U))
        policy[s] = best_action

    return U, policy, history