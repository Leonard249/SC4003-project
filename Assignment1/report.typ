// ============================================================
//  SC4003 / CE/CZ4046 Intelligent Agents – Assignment 1
//  Agent Decision Making: Value Iteration & Policy Iteration
// ============================================================

#set document(title: "SC4003 Assignment 1 – Agent Decision Making")
#set page(margin: 2cm, numbering: "1")
#set text(font: "New Computer Modern", size: 11pt)
#set heading(numbering: "1.1")
#set par(justify: true)

// ---------- Title Page ----------
#align(center)[
  #v(2cm)
  #text(size: 14pt, weight: "bold")[SCHOOL OF COMPUTER SCIENCE AND ENGINEERING]
  #linebreak()
  #text(size: 14pt, weight: "bold")[NANYANG TECHNOLOGICAL UNIVERSITY]
  #v(2cm)
  #text(size: 20pt, weight: "bold")[Assignment 1: Agent Decision Making]
  #v(0.5cm)
  #text(size: 14pt)[SC4003 / CE/CZ4046 – Intelligent Agents]
  #v(2cm)
  #text(size: 12pt)[
    _Module 3: Agent Decision Making_ \
    Reference: "Artificial Intelligence: A Modern Approach" \
    S. Russell and P. Norvig, Chapters 16 & 17
  ]
  #v(3cm)
]

#pagebreak()

// ---------- Table of Contents ----------
#outline(indent: 1.5em, depth: 3)
#pagebreak()

// ============================================================
= Introduction
// ============================================================

This report presents the implementation and results for Assignment 1 of SC4003 (Intelligent Agents), covering Markov Decision Processes (MDPs) solved via Value Iteration and Policy Iteration.

The assignment considers a 6#sym.times 6 stochastic maze environment with the following properties:

- *Transition model*: the intended action succeeds with probability 0.8; with probability 0.1 the agent moves at a right angle to the intended direction (0.1 each side). Hitting a wall or boundary causes the agent to remain in its current cell.
- *Rewards*: green cells yield $+1$, brown cells yield $-1$, and white (empty) cells yield $-0.05$. There are no terminal states.
- *Discount factor*: $gamma = 0.99$.

The goal is to compute the optimal policy $pi^*$ and the utility $U(s)$ for every non-wall state using both Value Iteration and Policy Iteration.

// ============================================================
= Part 1: Base 6#sym.times 6 Maze
// ============================================================

== Maze Layout

The maze is a 6#sym.times 6 grid with 5 wall cells and 31 non-wall (traversable) states. The layout is shown below, with row 0 at the top:

#figure(
  image("images/maze_layout.png", width: 35%),
  caption: [Base 6×6 maze from the assignment. Green = +1, Brown = −1, White = −0.05.],
)

#figure(
  table(
    columns: 7,
    align: center,
    stroke: 0.5pt,
    [], [*Col 0*], [*Col 1*], [*Col 2*], [*Col 3*], [*Col 4*], [*Col 5*],
    [*Row 0*], text(fill: rgb("#2e7d32"), weight: "bold")[+1], text(fill: gray)[Wall], text(fill: rgb("#2e7d32"), weight: "bold")[+1], [-0.05], [-0.05], text(fill: rgb("#2e7d32"), weight: "bold")[+1],
    [*Row 1*], [-0.05], text(fill: rgb("#8B4513"), weight: "bold")[-1], [-0.05], text(fill: rgb("#2e7d32"), weight: "bold")[+1], text(fill: gray)[Wall], text(fill: rgb("#8B4513"), weight: "bold")[-1],
    [*Row 2*], [-0.05], [-0.05], text(fill: rgb("#8B4513"), weight: "bold")[-1], [-0.05], text(fill: rgb("#2e7d32"), weight: "bold")[+1], [-0.05],
    [*Row 3*], [-0.05], [-0.05], [Start], text(fill: rgb("#8B4513"), weight: "bold")[-1], [-0.05], text(fill: rgb("#2e7d32"), weight: "bold")[+1],
    [*Row 4*], [-0.05], text(fill: gray)[Wall], text(fill: gray)[Wall], text(fill: gray)[Wall], text(fill: rgb("#8B4513"), weight: "bold")[-1], [-0.05],
    [*Row 5*], [-0.05], [-0.05], [-0.05], [-0.05], [-0.05], [-0.05],
  ),
  caption: [Base 6×6 maze in tabular form. Green cells = +1, Brown cells = −1, White cells = −0.05 living cost. Start position at (3, 2).],
)

== Value Iteration

=== Description of Implementation

Value Iteration applies the Bellman update rule iteratively until convergence. For each state $s$, the utility is updated as:

$ U_(i+1)(s) = R(s) + gamma dot max_a sum_(s') P(s' | s, a) dot U_i (s') $

Starting from $U_0(s) = 0$ for all states, the algorithm repeats until the maximum change across all states satisfies $delta < epsilon(1 - gamma) / gamma$, where $epsilon = 0.1$ (the AIMA textbook convergence criterion). With $gamma = 0.99$ this yields a tolerance of approximately $0.00101$.

The stochastic transition model is implemented via a `transition_probs` method that, for each action, computes the probability distribution over successor states by combining the 0.8 intended-direction probability with the 0.1 perpendicular-direction probabilities. When multiple outcomes map to the same cell (e.g., bouncing off a wall), the probabilities are correctly accumulated.

=== Optimal Policy (Value Iteration)

The algorithm converged in *688 iterations*. The resulting optimal policy is:

#figure(
  table(
    columns: 7,
    align: center,
    stroke: 0.5pt,
    [], [*Col 0*], [*Col 1*], [*Col 2*], [*Col 3*], [*Col 4*], [*Col 5*],
    [*Row 0*], [↑], [▓], [←], [←], [←], [↑],
    [*Row 1*], [↑], [←], [←], [←], [▓], [↑],
    [*Row 2*], [↑], [←], [←], [↑], [←], [←],
    [*Row 3*], [↑], [←], [←], [↑], [↑], [↑],
    [*Row 4*], [↑], [▓], [▓], [▓], [↑], [↑],
    [*Row 5*], [↑], [←], [←], [←], [↑], [↑],
  ),
  caption: [Optimal policy from Value Iteration. ▓ = wall.],
)

The policy shows two clear "attractor" regions. States in the upper-left quadrant are directed toward the +1 reward at (0,0), which has the highest utility due to the adjacent wall limiting stochastic drift. States in the lower-right flow upward toward the +1 rewards at (0,5) and (3,5). The bottom row (Row 5) routes agents leftward and upward, navigating around the wall barrier at Row 4.

=== Utilities of All States (Value Iteration)

#figure(
  table(
    columns: 7,
    align: center,
    stroke: 0.5pt,
    [], [*Col 0*], [*Col 1*], [*Col 2*], [*Col 3*], [*Col 4*], [*Col 5*],
    [*Row 0*], [99.90], [▓], [94.92], [93.74], [92.51], [93.18],
    [*Row 1*], [98.28], [95.77], [94.42], [94.27], [▓], [90.77],
    [*Row 2*], [96.82], [95.45], [93.16], [93.03], [92.96], [91.64],
    [*Row 3*], [95.41], [94.30], [93.07], [90.97], [91.66], [91.74],
    [*Row 4*], [94.16], [▓], [▓], [▓], [89.39], [90.40],
    [*Row 5*], [92.77], [91.55], [90.35], [89.16], [88.40], [89.12],
  ),
  caption: [Converged utility values from Value Iteration ($gamma = 0.99$).],
)

The highest utility is at (0,0) with $U = 99.90$, which is the green +1 cell in the top-left corner. This cell benefits from the adjacent wall at (0,1) which limits stochastic drift away from the reward. Utilities generally decrease with distance from high-reward states. Notably, (3,3) has a lower utility of 90.97 due to its $-1$ penalty, creating a local depression in the utility landscape.

=== Convergence Plots (Value Iteration)

#figure(
  image("images/part1_base_all_states.png", width: 100%),
  caption: [Utility estimates for all 31 states as a function of iteration number. Value Iteration (left) and Policy Iteration (right).],
)

The left panel shows that all 31 state utilities begin at zero and gradually converge over approximately 688 iterations. States near high-reward cells converge to higher values, while states further from rewards or near penalties converge to lower values. The characteristic S-curve shape reflects the gradual propagation of reward information through the Bellman updates.

#figure(
  image("images/part1_base_convergence.png", width: 100%),
  caption: [Convergence of the Start state (3, 2) for Value Iteration (left) and Policy Iteration (right).],
)

The tracked start state (3,2) converges smoothly from 0 toward its final value of approximately 93 under Value Iteration.

== Policy Iteration

=== Description of Implementation

Policy Iteration alternates between two steps:

+ *Policy Evaluation*: Given a fixed policy $pi$, solve the system of linear equations $ U^pi (s) = R(s) + gamma sum_(s') P(s' | s, pi(s)) dot U^pi (s') $ for all states simultaneously. This is done exactly via `numpy.linalg.solve` by constructing the matrix equation $(I - gamma T_pi) U = R$.

+ *Policy Improvement*: For each state, find the action that maximises the expected utility under the newly computed utilities: $ pi_(i+1)(s) = arg max_a sum_(s') P(s' | s, a) dot U^(pi_i) (s') $ If no state's policy changes, the algorithm has converged to the optimal policy.

The initial policy assigns a random action to every state.

=== Optimal Policy (Policy Iteration)

The algorithm converged in *6–8 iterations* (varies with the random initial policy). The resulting optimal policy is identical to that produced by Value Iteration:

#figure(
  table(
    columns: 7,
    align: center,
    stroke: 0.5pt,
    [], [*Col 0*], [*Col 1*], [*Col 2*], [*Col 3*], [*Col 4*], [*Col 5*],
    [*Row 0*], [↑], [▓], [←], [←], [←], [↑],
    [*Row 1*], [↑], [←], [←], [←], [▓], [↑],
    [*Row 2*], [↑], [←], [←], [↑], [←], [←],
    [*Row 3*], [↑], [←], [←], [↑], [↑], [↑],
    [*Row 4*], [↑], [▓], [▓], [▓], [↑], [↑],
    [*Row 5*], [↑], [←], [←], [←], [↑], [↑],
  ),
  caption: [Optimal policy from Policy Iteration (identical to Value Iteration).],
)

=== Utilities of All States (Policy Iteration)

#figure(
  table(
    columns: 7,
    align: center,
    stroke: 0.5pt,
    [], [*Col 0*], [*Col 1*], [*Col 2*], [*Col 3*], [*Col 4*], [*Col 5*],
    [*Row 0*], [100.00], [▓], [95.02], [93.84], [92.61], [93.28],
    [*Row 1*], [98.38], [95.87], [94.52], [94.37], [▓], [90.87],
    [*Row 2*], [96.92], [95.55], [93.26], [93.13], [93.06], [91.74],
    [*Row 3*], [95.51], [94.40], [93.17], [91.07], [91.76], [91.84],
    [*Row 4*], [94.26], [▓], [▓], [▓], [89.49], [90.50],
    [*Row 5*], [92.87], [91.65], [90.45], [89.26], [88.50], [89.22],
  ),
  caption: [Converged utility values from Policy Iteration ($gamma = 0.99$).],
)

The utilities from Policy Iteration are the exact solution of the Bellman equations (obtained via linear algebra), and are marginally higher than the Value Iteration approximation. The maximum difference is approximately 0.1, which is within the convergence tolerance $epsilon = 0.1$. The state (0,0) reaches the exact utility of $100.00 = 1.0 / (1 - 0.99)$, the theoretical maximum for a +1 reward state under an infinite-horizon discounted MDP with $gamma = 0.99$.

=== Convergence Analysis (Policy Iteration)

The right panel of the all-states plot (Figure 5) shows Policy Iteration converging in far fewer iterations (6–8) compared to Value Iteration's 688. This is because each Policy Iteration step performs an exact policy evaluation via linear system solving, obtaining the precise utility values for the current policy in a single step. In contrast, Value Iteration makes incremental Bellman updates and requires many sweeps to propagate value information across the grid.

The Policy Iteration convergence for the start state (3,2) in Figure 6 shows the utility jumping rapidly from its initial random-policy value (~65) to the converged value (~93) in just a few iterations.

// ============================================================
= Part 2: More Complex Maze Environments
// ============================================================

To investigate how the number of states and the complexity of the environment affect convergence, three additional 10#sym.times 10 maze environments were designed, each testing a different complexity dimension.

== Maze 1: Increased-Size (10#sym.times 10, 86 states, 14 walls)

This maze scales up the number of states to 86 (with 14 walls) while keeping the wall structure relatively open. Rewards (+1) and penalties (−1) are scattered throughout the grid, creating a more complex utility landscape than the base maze.

#figure(
  table(
    columns: 2,
    stroke: none,
    align: center,
    [*VI iterations*: 688], [*PI iterations*: ~11–13],
  ),
  caption: [Convergence comparison for the Increased-Size maze.],
)

#figure(
  image("images/part2_increased_all_states.png", width: 100%),
  caption: [All-states convergence for the Increased-Size 10×10 maze. Value Iteration (left), Policy Iteration (right).],
)

#figure(
  image("images/part2_increased_convergence.png", width: 100%),
  caption: [Tracked-state (4, 3) convergence for the Increased-Size 10×10 maze.],
)

Both algorithms produce identical optimal policies. Value Iteration requires the same number of iterations (688) as the base maze because convergence speed is governed by the discount factor $gamma$ and the AIMA tolerance formula, not directly by the number of states. Policy Iteration converges in approximately 11–13 iterations — more than the base maze's 6–8, reflecting the larger policy space with 86 states and more diverse action choices.

== Maze 2: Labyrinth (10#sym.times 10, 67 states, 33 walls)

This maze has a high wall density forming narrow corridors. Despite having fewer traversable states (67) than the increased-size maze (86), the corridor structure forces the agent through longer, more constrained paths.

#figure(
  table(
    columns: 2,
    stroke: none,
    align: center,
    [*VI iterations*: 688], [*PI iterations*: ~7–8],
  ),
  caption: [Convergence comparison for the Labyrinth maze.],
)

#figure(
  image("images/part2_labyrinth_all_states.png", width: 100%),
  caption: [All-states convergence for the Labyrinth 10×10 maze. Value Iteration (left), Policy Iteration (right).],
)

#figure(
  image("images/part2_labyrinth_convergence.png", width: 100%),
  caption: [Tracked-state (5, 3) convergence for the Labyrinth 10×10 maze.],
)

Interestingly, Policy Iteration converges in only 7–8 iterations — fewer than the open increased-size maze — because the narrow corridors reduce the number of meaningful policy choices per state (many cells have only one or two viable directions). Value Iteration still requires 688 iterations, dominated by the discount factor.

== Maze 3: Blockages (10#sym.times 10, 75 states, 25 walls)

This maze features three horizontal wall barriers, each with one or two narrow gates. Rewards are concentrated at the top, and the agent starts at the bottom row (9, 4), forcing it to navigate through all three barriers to reach the rewards.

#figure(
  table(
    columns: 2,
    stroke: none,
    align: center,
    [*VI iterations*: 662], [*PI iterations*: ~6–8],
  ),
  caption: [Convergence comparison for the Blockages maze.],
)

#figure(
  image("images/part2_blockages_all_states.png", width: 100%),
  caption: [All-states convergence for the Blockages 10×10 maze. Value Iteration (left), Policy Iteration (right).],
)

#figure(
  image("images/part2_blockages_convergence.png", width: 100%),
  caption: [Tracked-state (9, 4) convergence for the Blockages 10×10 maze.],
)

Value Iteration converges slightly faster (662 iterations versus 688) because the barrier structure partitions the state space and limits value propagation paths, allowing the convergence threshold to be reached sooner. Policy Iteration converges in 6–8 iterations, demonstrating its robustness to bottleneck-style environments.

== Analysis: Effect of Complexity on Convergence

#figure(
  table(
    columns: 6,
    align: center,
    stroke: 0.5pt,
    [*Maze*], [*Grid*], [*States*], [*Walls*], [*VI Iters*], [*PI Iters*],
    [Base], [6×6], [31], [5], [688], [6–8],
    [Increased-Size], [10×10], [86], [14], [688], [11–13],
    [Labyrinth], [10×10], [67], [33], [688], [7–8],
    [Blockages], [10×10], [75], [25], [662], [6–8],
  ),
  caption: [Summary of convergence across all four maze environments.],
)

*Key observations:*

+ *Value Iteration convergence is dominated by the discount factor*, not the number of states. With $gamma = 0.99$ and the AIMA convergence criterion ($delta < epsilon(1-gamma)/gamma approx 0.001$), VI consistently requires approximately 660–690 iterations regardless of maze size. This is because VI's convergence rate scales as $O(1/(1-gamma))$ — a high discount factor forces many iterations for value information to propagate and for the maximum change $delta$ to fall below the tight tolerance.

+ *Policy Iteration scales much better with complexity.* It converges in 6–13 iterations across all environments. Exact policy evaluation (solving a linear system) gives precise utilities at each step, so only a few improvement steps are needed. The per-iteration cost is higher ($O(n^3)$ for the linear solve), but the total number of iterations remains small.

+ *Wall density has mixed effects.* High wall density (Labyrinth, 33 walls) can actually _reduce_ PI iterations by constraining the action choices at each state. When corridors are narrow, many states have only one or two effective actions, so the greedy improvement step makes fewer changes per iteration, and convergence is reached faster. For VI, wall density has minimal effect since convergence is driven by the discount factor.

+ *Bottleneck structures* (Blockages) slightly speed up VI convergence because the barriers partition the state space and limit how far value information needs to propagate in each sweep, allowing $delta$ to drop below threshold sooner.

+ *Scalability limits.* Both algorithms successfully found correct optimal policies for all tested environments (up to 86 states). For much larger environments (thousands of states), VI's per-iteration cost remains $O(|S| dot |A|)$ while PI's $O(n^3)$ linear solve would become the bottleneck. In practice, approximation methods such as modified PI (using iterative evaluation instead of exact solves) or function approximation would be needed for very large MDPs. However, for the environments tested here (up to 100 states), both methods remain entirely tractable and reliably learn the correct policy.

// ============================================================
= Conclusion
// ============================================================

Both Value Iteration and Policy Iteration successfully compute optimal policies for the stochastic maze MDP. They produce identical policies across all tested environments, validating the correctness of both implementations. Policy Iteration is significantly faster in terms of iteration count (6–13 vs. ~688), while Value Iteration has simpler per-iteration logic. The choice between the two depends on the specific application: PI is preferred when exact linear solves are feasible, while VI is more suitable for very large state spaces where matrix inversion is impractical.

The experiments confirm that the algorithms can handle environments of varying complexity — from simple open grids to constrained labyrinths and bottleneck structures — and still reliably converge to the correct optimal policy.
