import sys
from z3 import *

def to_ternary(val, mask, width):
    bits = []
    for i in reversed(range(width)):
        if (mask >> i) & 1:
            bits.append(str((val >> i) & 1))
        else:
            bits.append("?")
    return "".join(bits)

def refine_lshr_general(width, A_v, A_m, B_v, B_m, C_v, C_m):
    # Helper to get bit at index or None if unknown
    def get_bit(v, m, i):
        return (v >> i) & 1 if (m >> i) & 1 else None

    # 1. Collect all shift amounts k consistent with current ternary B
    # Note: k > width is usually treated as shift by width (result 0)
    possible_k = []
    for k in range(width + 1):
        match = True
        for i in range(width):
            bit_k = (k >> i) & 1
            b_bit = get_bit(B_v, B_m, i)
            if b_bit is not None and b_bit != bit_k:
                match = False
                break
        if match:
            possible_k.append(k)

    # 2. Filter possible_k by checking if A and C are compatible for that shift
    valid_k = []
    for k in possible_k:
        consistent = True
        for i in range(width):
            # For C[i] = A[i + k]
            val_a = get_bit(A_v, A_m, i + k) if (i + k < width) else 0
            val_c = get_bit(C_v, C_m, i)

            # If both are known and don't match, this k is impossible
            if val_a is not None and val_c is not None and val_a != val_c:
                consistent = False
                break
        if consistent:
            valid_k.append(k)

    if not valid_k:
        return None  # Contradiction: no valid shift fits the data

    # 3. Refine B: Intersection of all remaining valid k values
    ref_B_v, ref_B_m = B_v, B_m
    for i in range(width):
        possible_bits = set((k >> i) & 1 for k in valid_k)
        if len(possible_bits) == 1:
            bit = possible_bits.pop()
            ref_B_m |= (1 << i)
            if bit: ref_B_v |= (1 << i)
            else: ref_B_v &= ~(1 << i)

    # 4. Refine A: Bit j must be v if for EVERY valid k, A[j] is forced to v
    ref_A_v, ref_A_m = A_v, A_m
    for j in range(width):
        if not (A_m >> j) & 1:
            forced_vals = set()
            for k in valid_k:
                # If we shift by k, then A[j] maps to C[j-k]
                if j - k >= 0:
                    c_val = get_bit(C_v, C_m, j - k)
                    if c_val is not None: forced_vals.add(c_val)
                    else: forced_vals.update([0, 1])
                else:
                    # A[j] is shifted out, it could be 0 or 1
                    forced_vals.update([0, 1])

            if len(forced_vals) == 1:
                v = forced_vals.pop()
                ref_A_m |= (1 << j)
                if v: ref_A_v |= (1 << j)
                else: ref_A_v &= ~(1 << j)

    # 5. Refine C: Bit i must be v if for EVERY valid k, C[i] is forced to v
    ref_C_v, ref_C_m = C_v, C_m
    for i in range(width):
        if not (C_m >> i) & 1:
            forced_vals = set()
            for k in valid_k:
                a_val = get_bit(A_v, A_m, i + k) if (i + k < width) else 0
                if a_val is not None: forced_vals.add(a_val)
                else: forced_vals.update([0, 1])
            
            if len(forced_vals) == 1:
                v = forced_vals.pop()
                ref_C_m |= (1 << i)
                if v: ref_C_v |= (1 << i)
                else: ref_C_v &= ~(1 << i)

    return ref_A_v, ref_A_m, ref_B_v, ref_B_m, ref_C_v, ref_C_m

if __name__ == "__main__":
    if len(sys.argv) != 8:
        print("Error: expected width A_v A_m B_v B_m C_v_r C_m_r")
        sys.exit(1)

    width  = int(sys.argv[1])
    A_v    = int(sys.argv[2], 2)
    A_m    = int(sys.argv[3], 2)
    B_v    = int(sys.argv[4], 2)
    B_m    = int(sys.argv[5], 2)
    C_v    = int(sys.argv[6], 2)
    C_m    = int(sys.argv[7], 2)

    result = refine_lshr_general(width, A_v, A_m, B_v, B_m, C_v, C_m)

    if result is None:
        print("Error: Contradiction found, no valid shift fits the data.")
        sys.exit(1)

    A_v_r, A_m_r, B_v_r, B_m_r, C_v_r, C_m_r = result
    fmt = f'0{width}b'
    print(f"{format(A_v_r,fmt)},{format(A_m_r,fmt)},{format(B_v_r,fmt)},{format(B_m_r,fmt)}")