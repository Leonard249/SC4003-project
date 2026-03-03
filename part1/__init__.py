# part1/__init__.py

# Expose specific classes/functions to the package level
from maze import Maze
from value_iteration import value_iteration_function

# Optional: Define what is available for "from part1 import *"
__all__ = ["Maze", "value_iteration_function"]

# Optional: Add package metadata
__version__ = "1.0.0"
