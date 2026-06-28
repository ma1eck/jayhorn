import sys
from z3 import *

def refine_mul_backward(width, A_value, A_mask, B_value, B_mask, C_value, C_mask):
    """
    Corrected backward refinement for C = A * B (mod 2^width).
    Dynamically refines bits of A and B from LSB to MSB to prevent over-approximation.
    """
    # Work with mutable bit representations: 
    # None represents an unknown bit ('?'), 0 or 1 represent known bits.
    A_bits = [((A_value >> i) & 1) if ((A_mask >> i) & 1) else None for i in range(width)]
    B_bits = [((B_value >> i) & 1) if ((B_mask >> i) & 1) else None for i in range(width)]
    C_bits = [((C_value >> i) & 1) if ((C_mask >> i) & 1) else None for i in range(width)]

    # carry_states[i] will store the set of possible incoming carries to bit position i.
    # Each carry state is represented as a tuple: (carry_value)
    carry_states = [set() for _ in range(width + 1)]
    carry_states[0] = {0}

    for i in range(width):
        # 1. Determine possible values for the current bits at position i
        possible_Ai = {0, 1} if A_bits[i] is None else {A_bits[i]}
        possible_Bi = {0, 1} if B_bits[i] is None else {B_bits[i]}
        possible_Ci = {0, 1} if C_bits[i] is None else {C_bits[i]}

        # We will collect valid choices of (a_i, b_i) and the next carries they generate
        valid_Ai = set()
        valid_Bi = set()
        next_carries = set()

        # Helper to compute all possible values of the cross-sum:
        # Sum_{j=1}^{i-1} (A_j * B_{i-j})
        # This only relies on already-processed (and potentially refined) lower bits.
        possible_mid_sums = {0}
        for j in range(1, i):
            val_Aj = {0, 1} if A_bits[j] is None else {A_bits[j]}
            val_Bi_j = {0, 1} if B_bits[j] is None else {B_bits[j]}
            
            temp = set()
            for x in val_Aj:
                for y in val_Bi_j:
                    for s in possible_mid_sums:
                        temp.add(s + x * y)
            possible_mid_sums = temp

        # Check all combinations of current bits and incoming carries
        for cin in carry_states[i]:
            for ai in possible_Ai:
                for bi in possible_Bi:
                    # Get boundary values for index 0
                    a0_vals = {0, 1} if A_bits[0] is None else {A_bits[0]}
                    b0_vals = {0, 1} if B_bits[0] is None else {B_bits[0]}

                    for a0 in a0_vals:
                        for b0 in b0_vals:
                            for mid_sum in possible_mid_sums:
                                # Calculate total sum at bit position i
                                if i == 0:
                                    total_sum = (ai * bi) + cin
                                else:
                                    total_sum = (a0 * bi) + (ai * b0) + mid_sum + cin

                                sum_bit = total_sum & 1
                                cout = total_sum >> 1

                                if sum_bit in possible_Ci:
                                    valid_Ai.add(ai)
                                    valid_Bi.add(bi)
                                    next_carries.add(cout)

        # 2. Refine our knowledge of the current bits based on valid combinations
        if len(valid_Ai) == 1:
            A_bits[i] = list(valid_Ai)[0]
        if len(valid_Bi) == 1:
            B_bits[i] = list(valid_Bi)[0]

        # 3. Save the filtered carries for the next bit position
        if not next_carries:
            # No valid transition means the inputs are mathematically contradictory
            break
        carry_states[i + 1] = next_carries

    # Convert back to (value, mask) representation
    ref_A_val, ref_A_mask = 0, 0
    ref_B_val, ref_B_mask = 0, 0
    for i in range(width):
        if A_bits[i] is not None:
            ref_A_mask |= (1 << i)
            if A_bits[i] == 1:
                ref_A_val |= (1 << i)
        if B_bits[i] is not None:
            ref_B_mask |= (1 << i)
            if B_bits[i] == 1:
                ref_B_val |= (1 << i)

    return ref_A_val, ref_A_mask, ref_B_val, ref_B_mask


def to_ternary(val, mask, width):
    bits = []
    for i in reversed(range(width)):
        if (mask >> i) & 1:
            bits.append(str((val >> i) & 1))
        else:
            bits.append("?")
    return "".join(bits)


def test():
    width = 4

    # A = ????
    A_v, A_m = 0b0000, 0b0000

    # B = ???? (3)
    #B_v, B_m = 0b0011, 0b1111
    B_v, B_m = 0b0000, 0b0000


    # C refined to 0011 (3)
    C_v, C_m = 0b0011, 0b1111

    A_v_r, A_m_r, B_v_r, B_m_r = refine_mul_backward(
        width,
        A_v, A_m,
        B_v, B_m,
        C_v, C_m
    )

    print("Refined A:", to_ternary(A_v_r, A_m_r, width))
    print("Refined B:", to_ternary(B_v_r, B_m_r, width))

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

    result = refine_mul_backward(width, A_v, A_m, B_v, B_m, C_v, C_m)

    if result is None:
        print("Error: Contradiction found, no valid shift fits the data.")
        sys.exit(1)

    A_v_r, A_m_r, B_v_r, B_m_r = result
    fmt = f'0{width}b'
    print(f"{format(A_v_r,fmt)},{format(A_m_r,fmt)},{format(B_v_r,fmt)},{format(B_m_r,fmt)}")