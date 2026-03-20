"""
Utility functions for printing grids and plotting convergence curves.

Merges:
  • Our terminal printing (dict-based)
  • The report's per-state convergence plots (all states on one figure)
"""
from typing import Dict, Tuple, List
import matplotlib
matplotlib.use("Agg")                       # headless backend
import matplotlib.pyplot as plt
from maze import Maze


# ------------------------------------------------------------------
# Terminal output
# ------------------------------------------------------------------
ARROW = {'N': '↑', 'S': '↓', 'E': '→', 'W': '←'}


def print_grid(maze: Maze,
               utilities: Dict[Tuple[int, int], float] = None,
               policy: Dict[Tuple[int, int], str] = None) -> None:
    """Print either utility values or policy arrows for every cell."""
    if utilities is not None:
        print("\n  Utility values:")
        for r in range(maze.rows):
            row_str = "  "
            for c in range(maze.cols):
                if (r, c) in maze.walls:
                    row_str += "  WALL  "
                else:
                    row_str += f"{utilities[(r, c)]:7.2f} "
            print(row_str)

    elif policy is not None:
        print("\n  Optimal policy:")
        for r in range(maze.rows):
            row_str = "  "
            for c in range(maze.cols):
                if (r, c) in maze.walls:
                    row_str += " ▓ "
                else:
                    row_str += f" {ARROW.get(policy[(r, c)], '?')} "
            print(row_str)


# ------------------------------------------------------------------
# Convergence plots
# ------------------------------------------------------------------
def plot_convergence(hist_vi: List[float], hist_pi: List[float],
                     state_label: str = "tracked state",
                     filename: str = "convergence_plot.png") -> None:
    """Side-by-side convergence curves for VI and PI (single tracked state)."""
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(12, 5))

    ax1.plot(hist_vi, linewidth=0.8)
    ax1.set_title(f"Value Iteration – utility of {state_label}")
    ax1.set_xlabel("Iteration")
    ax1.set_ylabel("Utility")

    ax2.plot(hist_pi, marker='o', linewidth=1.2)
    ax2.set_title(f"Policy Iteration – utility of {state_label}")
    ax2.set_xlabel("Iteration")
    ax2.set_ylabel("Utility")

    fig.tight_layout()
    fig.savefig(filename, dpi=150)
    plt.close(fig)
    print(f"  → Convergence plot saved to {filename}")


def plot_all_states_convergence(
        maze: Maze,
        all_histories_vi: Dict[Tuple[int, int], List[float]],
        all_histories_pi: Dict[Tuple[int, int], List[float]],
        filename: str = "all_states_convergence.png") -> None:
    """
    Plot every state's utility curve on a single figure (report-style).
    Two subplots: VI on the left, PI on the right.
    """
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(16, 6))

    for s, h in all_histories_vi.items():
        ax1.plot(h, linewidth=0.6, label=f"{s}")
    ax1.set_title(f"Value Iteration – {maze.name}")
    ax1.set_xlabel("Iteration")
    ax1.set_ylabel("Utility")

    for s, h in all_histories_pi.items():
        ax2.plot(h, linewidth=0.6, label=f"{s}")
    ax2.set_title(f"Policy Iteration – {maze.name}")
    ax2.set_xlabel("Iteration")
    ax2.set_ylabel("Utility")

    # Legend outside the plot area to avoid clutter
    for ax in (ax1, ax2):
        ax.legend(fontsize=5, ncol=3, loc="lower right")

    fig.tight_layout()
    fig.savefig(filename, dpi=150)
    plt.close(fig)
    print(f"  → All-states convergence plot saved to {filename}")


# ------------------------------------------------------------------
# Helpers to record per-state histories
# ------------------------------------------------------------------
def value_iteration_all_states(maze: Maze, gamma: float = 0.99):
    """Run value iteration while recording the utility of *every* state."""
    from config import TOLERANCE

    U = {s: 0.0 for s in maze.states}
    histories: Dict[Tuple[int, int], List[float]] = {s: [] for s in maze.states}

    while True:
        U_new = {}
        delta = 0.0
        for s in maze.states:
            best = max(maze.expected_value(s, a, U) for a in maze.ACTION_LIST)
            U_new[s] = maze.rewards[s] + gamma * best
            delta = max(delta, abs(U_new[s] - U[s]))
        U = U_new
        for s in maze.states:
            histories[s].append(U[s])
        if delta < TOLERANCE:
            break

    policy = {s: max(maze.ACTION_LIST,
                     key=lambda a, _s=s: maze.expected_value(_s, a, U))
              for s in maze.states}
    return U, policy, histories


def policy_iteration_all_states(maze: Maze, gamma: float = 0.99):
    """Run policy iteration while recording the utility of *every* state."""
    import random, numpy as np

    policy = {s: random.choice(maze.ACTION_LIST) for s in maze.states}
    histories: Dict[Tuple[int, int], List[float]] = {s: [] for s in maze.states}
    unchanged = False

    while not unchanged:
        # Exact policy evaluation
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
        U_vec = np.linalg.solve(A, b)
        U = {s: float(U_vec[i]) for i, s in enumerate(maze.states)}

        for s in maze.states:
            histories[s].append(U[s])

        unchanged = True
        for s in maze.states:
            old = policy[s]
            best = max(maze.ACTION_LIST,
                       key=lambda a, _s=s: maze.expected_value(_s, a, U))
            if best != old:
                policy[s] = best
                unchanged = False

    return U, policy, histories