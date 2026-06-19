import math
import struct

# ----------------------------------------------------------------------
# Tunable threshold: maximum free mantissa bits for full enumeration
# ----------------------------------------------------------------------
THRESHOLD = 16              # adjust to taste (e.g., 16 or 20)

# ----------------------------------------------------------------------
# Main public function – unbounded mask → exact interval representation
# ----------------------------------------------------------------------
def bitmask_to_intervals(E_val, E_mask, M_val, M_mask,
                         S_val=0, S_mask=-1):
    """
    Convert bitmask constraints into a list of exact double intervals.
    Heuristics used (in order):
      1. Suffix mask → exact contiguous interval.
      2. Small #free bits → exact enumeration.
      3. Low‑complexity mask → arithmetic progression decomposition.
      4. Otherwise → convex‑hull over‑approximation.
    """
    return bitmask_range_to_intervals(
        E_val, E_mask, M_val, M_mask, S_val, S_mask,
        a=float('-inf'), b=float('inf')
    )


# ----------------------------------------------------------------------
# Public function – mask restricted to [a,b] → exact interval representation
# ----------------------------------------------------------------------
def bitmask_range_to_intervals(E_val, E_mask, M_val, M_mask,
                               S_val, S_mask,
                               a, b):
    """
    Exact double intervals for the mask, constrained to doubles in [a,b].
    This is the workhorse; the unbounded version calls it with ±inf.
    """
    EXP_MAX   = 2047
    MANT_MAX  = (1 << 53) - 1
    intervals = []

    # Allowed signs (0 or 1)
    signs = [s for s in (0,1) if (s & ~S_mask) == (S_val & ~S_mask)]

    # Iterate over the 2048‑element exponent space
    for e in range(EXP_MAX + 1):
        if (e & ~E_mask) != (E_val & ~E_mask):
            continue

        for s in signs:
            # Compute the mantissa integer range that keeps us inside [a,b]
            if e == 0:
                m_low, m_high = mantissa_range_subnormal(s, a, b)
            elif e == EXP_MAX:
                m_low, m_high = mantissa_range_special(s, a, b)
            else:
                m_low, m_high = mantissa_range_normal(s, e, a, b)

            if m_low is None or m_low > m_high:
                continue

            # Obtain exact mantissa intervals for this (s,e)
            m_intervals = mantissa_set_to_intervals(
                M_val, M_mask, m_low, m_high
            )

            # Convert mantissa intervals to double intervals
            for m_start, m_end in m_intervals:
                if e == 0:
                    # Subnormals and zero
                    if m_start == 0:
                        zero = -0.0 if s else 0.0
                        intervals.append((zero, zero))
                        if m_end > 0:
                            intervals.append(
                                (subnormal_value(1, s),
                                 subnormal_value(m_end, s))
                            )
                    else:
                        intervals.append(
                            (subnormal_value(m_start, s),
                             subnormal_value(m_end, s))
                        )
                elif e == EXP_MAX:
                    # Infinities / NaNs
                    if m_start == 0 and m_end == 0:
                        val = -math.inf if s else math.inf
                        intervals.append((val, val))
                    else:
                        intervals.append((float('nan'), float('nan')))
                else:
                    intervals.append(
                        (normal_value(e, m_start, s),
                         normal_value(e, m_end, s))
                    )

    return intervals  #merge_intervals(intervals)


# ----------------------------------------------------------------------
# Core mantissa‑interval generation (all heuristics)
# ----------------------------------------------------------------------
def mantissa_set_to_intervals(M_val, M_mask, min_allowed, max_allowed):
    """
    Return a list of (start, end) mantissa integer intervals that EXACTLY
    cover all m ∈ [min_allowed, max_allowed] satisfying the mask.

    Heuristics applied:
      - Suffix mask → exact single interval.
      - Small free bits → full enumeration + compression.
      - Low complexity → arithmetic progression split.
      - Fallback → convex hull.
    """
    required  = M_val & ~M_mask   # fixed bits
    free_bits = M_mask & ((1 << 53) - 1)

    # Quick bounds outside which no value can exist
    min_m = max(required, min_allowed)
    max_m = min(required | free_bits, max_allowed)
    if min_m > max_m:
        return []

    # ---- Heuristic 1: Suffix mask (free bits form a contiguous block from LSB) ----
    # Condition: M_mask is of the form 2^k - 1, i.e. (M_mask & (M_mask + 1)) == 0
    if (M_mask & (M_mask + 1)) == 0:
        return [(min_m, max_m)]

    # ---- Heuristic 2: Small number of free bits → exact enumeration ----
    n_free = free_bits.bit_count()  # Python ≥3.8
    if n_free <= THRESHOLD:
        values = []
        # Enumerate all combinations of the free bits
        free_positions = [i for i in range(53) if (free_bits >> i) & 1]
        for combo in range(1 << n_free):
            m = required
            for idx, pos in enumerate(free_positions):
                if (combo >> idx) & 1:
                    m |= (1 << pos)
            if min_allowed <= m <= max_allowed:
                values.append(m)
        values.sort()
        return compress_runs(values)

    

    # ---- Heuristic 3: Convex‑hull over‑approximation ----
    return [(min_m, max_m)]


# ----------------------------------------------------------------------
# Helper: compress a sorted list of integers into runs
# ----------------------------------------------------------------------
def compress_runs(values):
    if not values:
        return []
    runs = []
    start = end = values[0]
    for v in values[1:]:
        if v == end + 1:
            end = v
        else:
            runs.append((start, end))
            start = end = v
    runs.append((start, end))
    return runs


# ----------------------------------------------------------------------
# Count runs of fixed bits (zeros in M_mask) within the 53‑bit range
# ----------------------------------------------------------------------
def count_fixed_runs(M_mask):
    # We consider only bits 0..51
    mask = M_mask & ((1 << 53) - 1)
    # Runs of zeros: iterate through bits, count transitions
    count = 0
    in_zero = False
    for i in range(53):
        bit = (mask >> i) & 1
        if bit == 0:
            if not in_zero:
                count += 1
                in_zero = True
        else:
            in_zero = False
    return count




# ----------------------------------------------------------------------
# Helper: true if value is an infinite float (not NaN)
# ----------------------------------------------------------------------
def _is_posinf(x):
    return math.isinf(x) and x > 0

def _is_neginf(x):
    return math.isinf(x) and x < 0


# ----------------------------------------------------------------------
# Normal numbers
# ----------------------------------------------------------------------
def mantissa_range_normal(s, e, a, b):
    """Return (m_min, m_max) of mantissa integers for exponent e so that
       the normalised double (1 + m/2^52)*2^(e-1023) lies in [a, b].
       Returns None if no mantissa can satisfy the bounds."""
    exp_val = 2.0 ** (e - 1023)
    MMAX = (1 << 52) - 1

    # ---- lower bound ----
    if math.isnan(a):
        return None
    if _is_neginf(a):
        m_min = 0
    elif _is_posinf(a):
        return None        # can never be >= +∞
    else:
        # a is finite
        m_float = (a / exp_val - 1.0) * (1 << 52)
        if math.isinf(m_float):
            # can happen if a is extremely large
            return None if m_float > 0 else 0
        m_min = int(math.ceil(m_float))
        if m_min < 0:
            m_min = 0
        if m_min > MMAX:
            return None

    # ---- upper bound ----
    if math.isnan(b):
        return None
    if _is_posinf(b):
        m_max = MMAX
    elif _is_neginf(b):
        return None        # can never be <= -∞
    else:
        m_float = (b / exp_val - 1.0) * (1 << 52)
        if math.isinf(m_float):
            return None if m_float < 0 else MMAX
        m_max = int(math.floor(m_float))
        if m_max < 0:
            return None
        if m_max > MMAX:
            m_max = MMAX

    if m_min > m_max:
        return None
    return (m_min, m_max)


# ----------------------------------------------------------------------
# Subnormals and zero
# ----------------------------------------------------------------------
def mantissa_range_subnormal(s, a, b):
    """Subnormals: value = m / 2^52 * 2^-1022,  0 ≤ m ≤ (1<<52)-1."""
    exp_val = 2.0 ** -1022 / (1 << 52)
    MMAX = (1 << 52) - 1

    # ---- lower bound ----
    if math.isnan(a):
        return None
    if _is_neginf(a):
        m_min = 0
    elif _is_posinf(a):
        return None
    else:
        m_float = a / exp_val
        if math.isinf(m_float):
            return None if m_float > 0 else 0
        m_min = int(math.ceil(m_float))
        if m_min < 0:
            m_min = 0
        if m_min > MMAX:
            return None

    # ---- upper bound ----
    if math.isnan(b):
        return None
    if _is_posinf(b):
        m_max = MMAX
    elif _is_neginf(b):
        return None
    else:
        m_float = b / exp_val
        if math.isinf(m_float):
            return None if m_float < 0 else MMAX
        m_max = int(math.floor(m_float))
        if m_max < 0:
            return None
        if m_max > MMAX:
            m_max = MMAX

    if m_min > m_max:
        return None
    return (m_min, m_max)


# ----------------------------------------------------------------------
# Special values (infinities)
# ----------------------------------------------------------------------
def mantissa_range_special(s, a, b):
    """Only mantissa 0 (infinity) can satisfy an interval; NaN never does."""
    val = -math.inf if s else math.inf
    if math.isnan(a) or math.isnan(b):
        return None
    if a <= val <= b:
        return (0, 0)
    return None



# ----------------------------------------------------------------------
# Double value constructors
# ----------------------------------------------------------------------
def normal_value(e, m, s):
    v = (1.0 + m / (1 << 52)) * (2.0 ** (e - 1023))
    return -v if s else v

def subnormal_value(m, s):
    v = (m / (1 << 52)) * (2.0 ** -1022)
    return -v if s else v


# ----------------------------------------------------------------------
# Interval merging (tolerant of NaN)
# ----------------------------------------------------------------------

""" def merge_intervals(intervals):
    if not intervals:
        return []
    nan_intervals = [x for x in intervals if math.isnan(x[0])]
    nums = [x for x in intervals if not math.isnan(x[0])]
    if not nums:
        return nan_intervals

    nums.sort(key=lambda x: x[0])
    merged = [list(nums[0])]
    for curr_min, curr_max in nums[1:]:
        last_min, last_max = merged[-1]
        if curr_min <= last_max or math.isclose(curr_min, last_max):
            merged[-1][1] = max(last_max, curr_max)
        else:
            merged.append([curr_min, curr_max])

    result = [(float(lo), float(hi)) for lo, hi in merged]
    result.extend(nan_intervals)
    return result """


# ----------------------------------------------------------------------
# Exact membership test (O(1))
# ----------------------------------------------------------------------
def mask_contains(x, S_val, S_mask, E_val, E_mask, M_val, M_mask):
    """Return True iff the bit pattern of x satisfies the masks."""
    bits = struct.unpack('Q', struct.pack('d', x))[0]
    s = bits >> 63
    e = (bits >> 52) & 0x7FF
    m = bits & ((1 << 52) - 1)
    return ((s & ~S_mask) == (S_val & ~S_mask) and
            (e & ~E_mask) == (E_val & ~E_mask) and
            (m & ~M_mask) == (M_val & ~M_mask))


# ----------------------------------------------------------------------
# Example usage
# ----------------------------------------------------------------------
def examples():
    # Example 1: All normals with exponent 1023 (bias)
    intervals = bitmask_to_intervals(0, (1<<11)-1, 1<<52, 0)
    print("All positive normals with exponent 1023:")
    for lo, hi in intervals[2050:2100]: print(f"  [{lo:.37g}, {hi:.37g}]")

    # Example 2: Subnormals 0‑3
    intervals = bitmask_to_intervals(0, 0, 0, 3)
    print("\nSubnormals m=0..3:")
    for lo, hi in intervals: print(f"  [{lo:.17g}, {hi:.17g}]")

    # Example 3: Range query – only values in [0.5, 1.0]
    intervals = bitmask_range_to_intervals(
        E_val=1023, E_mask=0, M_val=0, M_mask=(1<<52)-1,
        S_val=0, S_mask=0,
        a=0.5, b=1.0
    )
    print("\nNormals with exponent 1023 inside [0.5, 1.0]:")
    for lo, hi in intervals: print(f"  [{lo:.17g}, {hi:.17g}]")

if __name__ == "__main__":
    examples()