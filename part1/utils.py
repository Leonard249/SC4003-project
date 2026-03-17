"""
Utility functions for printing and plotting.
"""
from typing import Dict, Tuple, List
import matplotlib.pyplot as plt
from maze import Maze

def print_grid(maze: Maze, utilities: Dict[Tuple[int, int], float] = None,
               policy: Dict[Tuple[int, int], str] = None):
    """
    Print the maze with either utilities or policy arrows.
    If both are given, utilities are printed by default; set policy=True to print arrows.
    """
    if utilities is not None:
        print("\nUtility values:")
        for r in range(maze.rows):
            row_str = ""
            for c in range(maze.cols):
                if (r, c) in maze.walls:
                    row_str += "  WALL   "
                else:
                    u = utilities[(r, c)]
                    row_str += f"{u:7.3f} "
            print(row_str)
    elif policy is not None:
        print("\nOptimal policy:")
        for r in range(maze.rows):
            row_str = ""
            for c in range(maze.cols):
                if (r, c) in maze.walls:
                    row_str += " W "
                else:
                    row_str += f" {policy[(r, c)]} "
            print(row_str)

def plot_convergence(history_vi: List[float], history_pi: List[float],
                     state_label: str = "Tracked state"):
    """
    Plot utility estimates as a function of iteration number for both algorithms.
    """
    plt.figure(figsize=(10, 5))
    plt.subplot(1, 2, 1)
    plt.plot(history_vi, marker='.', linestyle='-')
    plt.title(f"Value Iteration: Utility of {state_label}")
    plt.xlabel("Iteration")
    plt.ylabel("Utility")

    plt.subplot(1, 2, 2)
    plt.plot(history_pi, marker='o', linestyle='-')
    plt.title(f"Policy Iteration: Utility of {state_label}")
    plt.xlabel("Iteration")
    plt.ylabel("Utility")

    plt.tight_layout()
    plt.savefig("results/convergence_plot.png", dpi=150)
    print("Convergence plot saved to convergence_plot.png")