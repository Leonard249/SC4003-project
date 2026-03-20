"""
Value Iteration algorithm for the maze MDP.

Convergence criterion uses the AIMA textbook formula:
    δ < ε(1 − γ) / γ
which guarantees the resulting policy is within ε of optimal.
"""
from typing import Dict, Tuple, List
from maze import Maze
from config import DISCOUNT_FACTOR, TOLERANCE


def value_iteration(maze: Maze, gamma: float = DISCOUNT_FACTOR,
                    track_state: Tuple[int, int] = None):
    """
    Perform value iteration on *maze*.

    Parameters
    ----------
    maze        : Maze instance.
    gamma       : Discount factor (default from config).
    track_state : If given, record its utility after every sweep.

    Returns
    -------
    U       : dict  – state -> converged utility
    policy  : dict  – state -> optimal action character
    history : list  – utility of *track_state* at each iteration
    """
    # Initialise all utilities to 0
    U: Dict[Tuple[int, int], float] = {s: 0.0 for s in maze.states}

    if track_state is None:
        track_state = maze.states[0]

    history: List[float] = []

    while True:
        U_new: Dict[Tuple[int, int], float] = {}
        delta = 0.0

        for s in maze.states:
            # Bellman update: U(s) = R(s) + γ · max_a E[U(s')]
            best = max(maze.expected_value(s, a, U) for a in maze.ACTION_LIST)
            U_new[s] = maze.rewards[s] + gamma * best
            delta = max(delta, abs(U_new[s] - U[s]))

        U = U_new
        history.append(U[track_state])

        # AIMA convergence check: δ < ε(1−γ)/γ
        if delta < TOLERANCE:
            break

    # Extract optimal policy from converged utilities
    policy: Dict[Tuple[int, int], str] = {}
    for s in maze.states:
        policy[s] = max(maze.ACTION_LIST,
                        key=lambda a: maze.expected_value(s, a, U))

    return U, policy, history