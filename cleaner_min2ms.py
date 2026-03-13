import pandas as pd

input_file = "unbounded_benchmark_results.csv"
output_file = "unbounded_benchmark_results2.csv"

# Load the CSV
df = pd.read_csv(input_file)

def convert_solver_time(value):
    if isinstance(value, str):
        value = value.strip()
        
        # Case: value in minutes (e.g., "2 min", "1.5min")
        if value.endswith("min"):
            minutes = float(value.replace("min", "").strip())
            ms = minutes * 60 * 1000
            ms = round(ms, 6)
            return ms # minutes → milliseconds
        
        # If it already looks numeric, convert it
        try:
            return float(value)
        except:
            return value
    return value

# Apply conversion
df["Solver Time (ms)"] = df["Solver Time (ms)"].apply(convert_solver_time)

# Save to new CSV
df.to_csv(output_file, index=False)

print(f"Cleaned file saved as {output_file}")
