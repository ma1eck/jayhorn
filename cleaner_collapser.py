import pandas as pd

input_file = "unbounded_benchmark_results2.csv"
output_file = "unbounded_benchmark_results_cleaned.csv"

# Read the CSV (comma separated since csv.writer was used)
df = pd.read_csv(input_file)

# Clean possible whitespace
df.columns = df.columns.str.strip()
df["Benchmark Name"] = df["Benchmark Name"].str.strip()
df["Rounding"] = df["Rounding"].str.strip()
df["Normalization"] = df["Normalization"].str.strip()

# Sort rows
df = df.sort_values(by=["Benchmark Name", "Rounding", "Normalization"])

# Pivot table so each (rounding, normalization) combination becomes columns
pivot_df = df.pivot(
    index="Benchmark Name",
    columns=["Rounding", "Normalization"],
    values=["Total Time (ms)", "Result", "Solver Time (ms)"]
)

# Flatten multi-level column names
pivot_df.columns = [
    f"{metric}_{rounding}_{norm}"
    for metric, rounding, norm in pivot_df.columns
]

# Convert index back to column
pivot_df = pivot_df.reset_index()

# Save collapsed CSV
pivot_df.to_csv(output_file, index=False)

print(f"Saved collapsed results to {output_file}")