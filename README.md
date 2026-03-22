# SC4003 Assignment 1 – Agent Decision Making

Value Iteration and Policy Iteration on stochastic maze environments.

## Prerequisites

- **Python 3.13+**
- **uv** – a fast Python package and project manager

---

## Setup

### 1. Install uv

**macOS / Linux**

```bash
curl -LsSf https://astral.sh/uv/install.sh | sh
```

**Windows (PowerShell)**

```powershell
powershell -ExecutionPolicy ByPass -c "irm https://astral.sh/uv/install.ps1 | iex"
```

After installation, restart your terminal or run the command printed by the installer to add `uv` to your `PATH`.

---

### 2. Initialise the project

Navigate to the project directory and initialise it:

```bash
cd sc4003-project
uv init
```

> **Note:** If you cloned or unzipped this project and a `pyproject.toml` is already present, `uv init` will detect it and skip re-initialisation. You can proceed directly to the next step.

---

### 3. Sync dependencies

Install all required packages (`numpy`, `matplotlib`) from `pyproject.toml`:

```bash
uv sync
```

This creates a virtual environment in `.venv/` and installs all dependencies into it automatically.

---

### 4. Run the project

```bash
uv run main.py
```

---

> Make sure the `results/` directory exists before running. Create it with:
>
> ```bash
> mkdir results
> ```

---
