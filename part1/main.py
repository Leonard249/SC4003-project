"""
Main script for Part 1 of the assignment.
Runs value iteration and policy iteration on the given 3x4 maze.
"""
from maze import Maze
from value_iteration import *
from policy_iteration import policy_iteration
from utils import print_grid, plot_convergence

def main():
    # Define the maze (same as Figure 17.1 in AIMA)
    rows, cols = 3, 4
    walls = {(1, 1)}  # row 1, col 1 (0‑based) is a wall

    # Rewards: green +1 at (0,3), brown -1 at (1,3), white -0.05 elsewhere
    rewards = {}
    for r in range(rows):
        for c in range(cols):
            if (r, c) in walls:
                continue
            if (r, c) == (0, 3):
                rewards[(r, c)] = 1.0
            elif (r, c) == (1, 3):
                rewards[(r, c)] = -1.0
            else:
                rewards[(r, c)] = -0.05

    maze = Maze(rows, cols, walls, rewards)

    # Tracked state for convergence plot (bottom‑left corner)
    track = (rows-1, 0)

    print("Running Value Iteration...")
    U_vi, pi_vi, hist_vi = value_iteration(maze, track_state=track)
    print("Value Iteration completed.")
    print_grid(maze, utilities=U_vi)
    print_grid(maze, policy=pi_vi)

    print("\nRunning Policy Iteration...")
    U_pi, pi_pi, hist_pi = policy_iteration(maze, track_state=track)
    print("Policy Iteration completed.")
    print_grid(maze, utilities=U_pi)
    print_grid(maze, policy=pi_pi)

    # Plot convergence
    plot_convergence(hist_vi, hist_pi, state_label=str(track))

if __name__ == "__main__":
    main()