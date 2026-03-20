"""
Shared constants and configuration for the MDP maze solver.
Adopted from the report's config-driven approach.
"""

# Discount factor (given in the assignment)
DISCOUNT_FACTOR = 0.99

# Value Iteration convergence parameters (AIMA textbook formula)
# tolerance = epsilon * (1 - gamma) / gamma
EPSILON = 0.1
TOLERANCE = EPSILON * (1 - DISCOUNT_FACTOR) / DISCOUNT_FACTOR  # ≈ 0.00101

# Actions and their (row, col) deltas
ACTIONS = {
    'N': (-1, 0),   # Up
    'S': (1, 0),     # Down
    'E': (0, 1),     # Right
    'W': (0, -1),    # Left
}

# Stochastic transition model probabilities
PROB_INTENDED = 0.8
PROB_PERPENDICULAR = 0.1

# Perpendicular directions for each action
PERPENDICULAR = {
    'N': ['E', 'W'],
    'S': ['E', 'W'],
    'E': ['N', 'S'],
    'W': ['N', 'S'],
}