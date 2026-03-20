"""
SC4003 Assignment 1 – Agent Decision Making
============================================
Runs Value Iteration and Policy Iteration on:
  • Part 1 : the base 6×6 maze from the assignment
  • Part 2 : three more-complex 10×10 mazes (increased-size, labyrinth, blockages)

Outputs
-------
  • Terminal:  utility grids + policy grids for every maze
  • Files:     convergence plots (single-state and all-states) as .png
"""
from maze_configs import base, increased_size, labyrinth, blockages
from value_iteration import value_iteration
from policy_iteration import policy_iteration
from utils import (print_grid, plot_convergence,
                   plot_all_states_convergence,
                   value_iteration_all_states,
                   policy_iteration_all_states)


def run_maze(maze, tag: str, track_state=None):
    """Run both algorithms on a single maze and save outputs."""
    print(f"\n{'=' * 60}")
    print(f"  {maze.name}")
    print(f"  States: {len(maze.states)}  |  Walls: {len(maze.walls)}")
    print(f"{'=' * 60}")

    # Pick a default tracked state if none given
    if track_state is None:
        track_state = maze.states[0]

    # ---- Value Iteration ------------------------------------------------
    print(f"\n  [Value Iteration]")
    U_vi, pi_vi, hist_vi = value_iteration(maze, track_state=track_state)
    print(f"  Converged in {len(hist_vi)} iterations.")
    print_grid(maze, utilities=U_vi)
    print_grid(maze, policy=pi_vi)

    # ---- Policy Iteration -----------------------------------------------
    print(f"\n  [Policy Iteration]")
    U_pi, pi_pi, hist_pi = policy_iteration(maze, track_state=track_state)
    print(f"  Converged in {len(hist_pi)} iterations.")
    print_grid(maze, utilities=U_pi)
    print_grid(maze, policy=pi_pi)

    # ---- Single-state convergence plot ----------------------------------
    plot_convergence(hist_vi, hist_pi,
                     state_label=str(track_state),
                     filename=f"results/{tag}_convergence.png")

    # ---- All-states convergence plot (report-style) ---------------------
    _, _, all_vi = value_iteration_all_states(maze)
    _, _, all_pi = policy_iteration_all_states(maze)
    plot_all_states_convergence(maze, all_vi, all_pi,
                                filename=f"results/{tag}_all_states.png")


def main():
    # ================================================================
    # Part 1 – Base 6×6 maze (from the assignment)
    # ================================================================
    base_maze = base.build()
    run_maze(base_maze, tag="part1_base", track_state=base.START)

    # ================================================================
    # Part 2 – More-complex mazes
    # ================================================================
    inc_maze = increased_size.build()
    run_maze(inc_maze, tag="part2_increased", track_state=increased_size.START)

    lab_maze = labyrinth.build()
    run_maze(lab_maze, tag="part2_labyrinth", track_state=labyrinth.START)

    blk_maze = blockages.build()
    run_maze(blk_maze, tag="part2_blockages", track_state=blockages.START)

    print(f"\n{'=' * 60}")
    print("  All done. Check the .png files for convergence plots.")
    print(f"{'=' * 60}")


if __name__ == "__main__":
    main()