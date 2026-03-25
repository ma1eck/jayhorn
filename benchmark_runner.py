import os
import subprocess
import csv
import re
import time
from concurrent.futures import ProcessPoolExecutor, as_completed

# --- CONFIGURATION ---
BASE_DIRS = [r"examples2\JAVA-SVCOM", r"examples2\C-SVCOM"]
NATIVE_LIB = r"C:\am21\Float_Z3_jayhorn\jayhorn\jayhorn\native_lib"
JAYHORN_JAR = r"C:\am21\Float_Z3_jayhorn\jayhorn\jayhorn\build\libs\jayhorn.jar"
CSV_FILE_PATH = 'benchmark_results.csv'
SOLVER = "spacer"

TIMEOUT_SECONDS =  8*60
MAX_WORKERS = 4

LOOP_BASED = "loop-based"
LOOP_FREE = "loop-free"
ENCODINGS = [LOOP_BASED, LOOP_FREE]

CEX_DIR_NAME = "counter examples or models"

GET_CEX = False

SKIP_TIMEOUTS = False

NUMBER_OF_REPETITION = 1
AVERAGING = NUMBER_OF_REPETITION > 1

selected_benchmarks = []

def run_benchmark(task_info):
    base_dir, folder_name, rounding_enc, norm_enc = task_info
    folder_path = os.path.join(base_dir, folder_name)

    classes_dir = os.path.join(folder_path, "classes")
    src_dir = os.path.join(folder_path, "src")

    output_filename = f"output_R_{rounding_enc}_N_{norm_enc}.txt"
    output_file_path = os.path.join(folder_path, output_filename)

    if not (os.path.isdir(classes_dir) and os.path.isdir(src_dir)):
        return None
    if (len(selected_benchmarks) != 0 and not folder_name in selected_benchmarks):
        return None

    cmd = [
        "java",
        f"-Djava.library.path={NATIVE_LIB}",
        "-jar", JAYHORN_JAR,
        "-j", classes_dir,
        "-src", src_dir,
        "-rounding-encoding", rounding_enc,
        "-normalization-encoding", norm_enc,
        "-solver", SOLVER,
        "-heap-mode", "bounded"
    ]

    if GET_CEX:
        cex_path = os.path.join(folder_path, CEX_DIR_NAME,
                                f"rounding {rounding_enc} normalization {norm_enc}.txt")
        cmd += ["-solution", "-full-cex", "-print-horn", "-cex-path", cex_path]

    env = os.environ.copy()
    env["PATH"] = NATIVE_LIB + ";" + env["PATH"]

    total_times = []
    solver_times = []
    result = "UNKNOWN"
    stdout = ""

    for i in range(NUMBER_OF_REPETITION):

        start_wall_clock = time.time()

        try:
            process = subprocess.Popen(
                cmd,
                stdout=subprocess.PIPE,
                stderr=subprocess.STDOUT,
                env=env,
                text=True,
                encoding="utf-8",
                errors="replace"
            )

            try:
                run_stdout, _ = process.communicate(timeout=TIMEOUT_SECONDS)
                end_wall_clock = time.time()

                total_time_ms = (end_wall_clock - start_wall_clock) * 1000
                total_times.append(total_time_ms)

                solver_time_ms = None

                for line in run_stdout.splitlines():

                    if "Spacer takes" in line:
                        match = re.search(r'Spacer takes\s+([\d.]+)\s*(\S+)', line)
                        if match:
                            val = float(match.group(1))
                            unit = match.group(2).lower()

                            # if unit == "ms":
                            solver_time_ms = val
                            # elif unit in ["s", "sec", "secs"]:
                                # solver_time_ms = val * 1000

                    clean_line = line.strip()
                    if clean_line in ("SAFE", "UNSAFE"):
                        result = clean_line

                if solver_time_ms is not None:
                    solver_times.append(solver_time_ms)

                stdout += f"\n\n=== RUN {i+1} ===\n"
                stdout += run_stdout

            except subprocess.TimeoutExpired:
                process.kill()
                process.wait()

                result = "TIMEOUT"
                total_times.append(TIMEOUT_SECONDS * 1000)

                stdout += f"\n\n=== RUN {i+1} TIMEOUT ===\n"

        except Exception as e:
            result = "ERROR"
            stdout += f"\n\n=== ERROR ===\n{str(e)}"

    avg_total = round(sum(total_times) / len(total_times), 2) if total_times else ""
    avg_solver = round(sum(solver_times) / len(solver_times), 2) if solver_times else ""

    try:
        with open(output_file_path, "w", encoding="utf-8", errors="replace") as f:
            f.write(stdout)
    except Exception as e:
        print(f"Warning: Failed to write {output_filename} for {folder_name}: {e}")

    return [folder_name, rounding_enc, norm_enc, avg_total, result, avg_solver]


def main():
    tasks = []
    for b_dir in BASE_DIRS:
        if not os.path.exists(b_dir):
            print(f"Warning: Directory not found: {b_dir}")
            continue
        
        for folder in os.listdir(b_dir):
            if GET_CEX and os.path.isdir(os.path.join(b_dir, folder)):
                cex_dir = os.path.join(b_dir, folder, CEX_DIR_NAME)
                os.makedirs(cex_dir, exist_ok=True)

            # Create 4 tasks for each benchmark covering all combinations
            for rounding_enc in ENCODINGS:
                for norm_enc in ENCODINGS:
                    tasks.append((b_dir, folder, rounding_enc, norm_enc))

    print(f"Starting parallel run for {len(tasks)} tasks (4 per benchmark)...")
    print(f"Timeout set to {TIMEOUT_SECONDS /60:.2f} minutes per benchmark task.")
    if AVERAGING:
        print(f"Set to average {NUMBER_OF_REPETITION} repetitions of a benchmark.")

    results_data = []

    with ProcessPoolExecutor(max_workers=MAX_WORKERS) as executor:
        try:
            future_to_benchmark = {
                executor.submit(run_benchmark, t): t for t in tasks
            }

            for future in as_completed(future_to_benchmark):
                res = future.result()
                if res:
                    results_data.append(res)
                    print(f"  Finished: {res[0]} [R: {res[1]}, N: {res[2]}] -> {res[4]} ({res[3]} ms), solver took:{res[5]} ms")

        except KeyboardInterrupt:
            print("\nCtrl+C detected. Terminating workers...")
            executor.shutdown(wait=False, cancel_futures=True)
            raise

    # Added columns to reflect the configurations
    headers = ['Benchmark Name', 'Rounding', 'Normalization', 'Total Time (ms)', 'Result', 'Solver Time (ms)']
    with open(CSV_FILE_PATH, mode='w', newline='', encoding='utf-8') as file:
        writer = csv.writer(file)
        writer.writerow(headers)
        # results_data.sort()
        writer.writerows(results_data)

    print(f"\nAll processing complete. Results written to: {CSV_FILE_PATH}")


if __name__ == "__main__":
    main()
