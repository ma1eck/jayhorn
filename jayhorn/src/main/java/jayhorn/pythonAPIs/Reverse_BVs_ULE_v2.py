# Reverse_BVs_EQ.py
import sys
from z3 import *

def refine_ule_bvs(
        width,
        A_value, A_mask,
        B_value, B_mask,
        ule_refined: bool
):
    """
    Backward refinement for:
        A <= B  (unsigned)  if ule_refined=True
        A >  B  (unsigned)  if ule_refined=False

    value/mask convention:
        mask bit = 1  -> known
        mask bit = 0  -> unknown
    """

    # --------------------------------------------
    # Helper: possible bit values
    # --------------------------------------------
    def possible_bits(val, mask, i):
        if (mask >> i) & 1:
            return {(val >> i) & 1}
        return {0, 1}

    # comparison states
    EQ, LT, GT = 0, 1, 2

    # --------------------------------------------
    # Phase 1: Forward comparison propagation
    # --------------------------------------------
    states_possible = [set() for _ in range(width + 1)]
    states_possible[0] = {EQ}

    for k in range(width):
        i = width - 1 - k  # process from MSB to LSB

        Ai = possible_bits(A_value, A_mask, i)
        Bi = possible_bits(B_value, B_mask, i)

        next_states = set()

        for state in states_possible[k]:
            for a in Ai:
                for b in Bi:

                    if state == EQ:
                        if a < b:
                            next_states.add(LT)
                        elif a > b:
                            next_states.add(GT)
                        else:
                            next_states.add(EQ)

                    elif state == LT:
                        next_states.add(LT)

                    elif state == GT:
                        next_states.add(GT)

        states_possible[k + 1] = next_states

    # --------------------------------------------
    # Phase 2: Backward filtering
    # --------------------------------------------
    valid_states = [set() for _ in range(width + 1)]

    if ule_refined:
        valid_states[width] = states_possible[width] & {EQ, LT}
    else:
        valid_states[width] = states_possible[width] & {GT}

    for k in reversed(range(width)):
        i = width - 1 - k

        Ai = possible_bits(A_value, A_mask, i)
        Bi = possible_bits(B_value, B_mask, i)

        for state in states_possible[k]:
            for a in Ai:
                for b in Bi:

                    if state == EQ:
                        if a < b:
                            ns = LT
                        elif a > b:
                            ns = GT
                        else:
                            ns = EQ
                    elif state == LT:
                        ns = LT
                    else:
                        ns = GT

                    if ns in valid_states[k + 1]:
                        valid_states[k].add(state)

    # --------------------------------------------
    # Phase 3: Bit refinement
    # --------------------------------------------
    new_A_value = A_value
    new_A_mask  = A_mask
    new_B_value = B_value
    new_B_mask  = B_mask

    for k in range(width):
        i = width - 1 - k

        Ai = possible_bits(A_value, A_mask, i)
        Bi = possible_bits(B_value, B_mask, i)

        possible_A = set()
        possible_B = set()

        for state in valid_states[k]:
            for a in Ai:
                for b in Bi:

                    if state == EQ:
                        if a < b:
                            ns = LT
                        elif a > b:
                            ns = GT
                        else:
                            ns = EQ
                    elif state == LT:
                        ns = LT
                    else:
                        ns = GT

                    if ns in valid_states[k + 1]:
                        possible_A.add(a)
                        possible_B.add(b)

        if len(possible_A) == 1:
            bit = next(iter(possible_A))
            new_A_mask |= (1 << i)
            if bit:
                new_A_value |= (1 << i)
            else:
                new_A_value &= ~(1 << i)

        if len(possible_B) == 1:
            bit = next(iter(possible_B))
            new_B_mask |= (1 << i)
            if bit:
                new_B_value |= (1 << i)
            else:
                new_B_value &= ~(1 << i)

    fmt = f'0{width}b'
    return (format(new_A_value, fmt), format(new_A_mask, fmt),
            format(new_B_value, fmt), format(new_B_mask, fmt))


def to_ternary(val, msk, width):
    val_bin = format(val, f'0{width}b')
    msk_bin = format(msk, f'0{width}b')
    return "".join(v if m == '1' else '?' for v, m in zip(val_bin, msk_bin))

def example():
    # --- Example Run ---
    width = 4

    # Original A is "1 0 1 0"  -> value = 0xa, mask = 0xF
    # Original B is "? ? ? ?"  -> value = 0x0, mask = 0x0
    # (They can each be 0, 1, 2, or 3)

    # A_v, A_m = 0xA, 0xF
    # B_v, B_m = 0x0, 0x0

    A_v, A_m = "00", "11"
    B_v, B_m = "00", "00"
    A_v, A_m = int(A_v, 2), int(A_m, 2)
    B_v, B_m = int(B_v, 2), int(B_m, 2)

    # Get refined values
    A_v_r, A_m_r, B_v_r, B_m_r = refine_ule_bvs(
        width, A_v, A_m, B_v, B_m, False
    )
    print(f"{A_v_r}, {A_m_r}, {B_v_r}, {B_m_r}")

    # print(f"Original A:       {to_ternary(A_v, A_m, width)}")
    # print(f"Original B:       {to_ternary(B_v, B_m, width)}")
    # print("-" * 30)
    # print(f"Refined A (Out):  {to_ternary(A_v_r, A_m_r, width)}")
    # print(f"Refined B (Out):  {to_ternary(B_v_r, B_m_r, width)}")

if __name__ == "__main__":
    # example()
    if len(sys.argv) != 7:
        print("Error: expected width A_v A_m B_v B_m eq_refined")
        sys.exit(1)

    width    = int(sys.argv[1])
    A_v      = int(sys.argv[2],2)
    A_m      = int(sys.argv[3],2)
    B_v      = int(sys.argv[4],2)
    B_m      = int(sys.argv[5],2)
    eq_refined = sys.argv[6].lower() == "true"

    result = refine_ule_bvs(width, A_v, A_m, B_v, B_m, eq_refined)
    print(f"{result[0]},{result[1]},{result[2]},{result[3]}")




