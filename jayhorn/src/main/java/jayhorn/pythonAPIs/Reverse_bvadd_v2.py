import sys
from z3 import *

def refine_add_backward(
    width,
    A_value, A_mask,
    B_value, B_mask,
    C_value, C_mask
):
    """
    Backward refinement for:
        C = A + B   (mod 2^width)

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

    # --------------------------------------------
    # Phase 1: Forward carry propagation
    # --------------------------------------------
    carry_possible = [set() for _ in range(width + 1)]
    carry_possible[0] = {0}  # no incoming carry at LSB

    for i in range(width):
        Ai = possible_bits(A_value, A_mask, i)
        Bi = possible_bits(B_value, B_mask, i)
        Ci = possible_bits(C_value, C_mask, i)

        next_carries = set()

        for cin in carry_possible[i]:
            for a in Ai:
                for b in Bi:
                    s = a + b + cin
                    sum_bit = s & 1
                    cout = (s >> 1) & 1

                    if sum_bit in Ci:
                        next_carries.add(cout)

        carry_possible[i + 1] = next_carries

    # --------------------------------------------
    # Phase 2: Backward carry filtering
    # --------------------------------------------
    valid_carry = [set() for _ in range(width + 1)]
    valid_carry[width] = carry_possible[width].copy()

    for i in reversed(range(width)):
        Ai = possible_bits(A_value, A_mask, i)
        Bi = possible_bits(B_value, B_mask, i)
        Ci = possible_bits(C_value, C_mask, i)

        for cin in carry_possible[i]:
            for a in Ai:
                for b in Bi:
                    s = a + b + cin
                    sum_bit = s & 1
                    cout = (s >> 1) & 1

                    if sum_bit in Ci and cout in valid_carry[i + 1]:
                        valid_carry[i].add(cin)

    # --------------------------------------------
    # Phase 3: Bit refinement
    # --------------------------------------------
    new_A_value = A_value
    new_A_mask  = A_mask
    new_B_value = B_value
    new_B_mask  = B_mask

    for i in range(width):
        Ai = possible_bits(A_value, A_mask, i)
        Bi = possible_bits(B_value, B_mask, i)
        Ci = possible_bits(C_value, C_mask, i)

        possible_A = set()
        possible_B = set()

        for cin in valid_carry[i]:
            for a in Ai:
                for b in Bi:
                    s = a + b + cin
                    sum_bit = s & 1
                    cout = (s >> 1) & 1

                    if sum_bit in Ci and cout in valid_carry[i + 1]:
                        possible_A.add(a)
                        possible_B.add(b)

        # Refine A bit
        if len(possible_A) == 1:
            bit = next(iter(possible_A))
            new_A_mask |= (1 << i)
            if bit:
                new_A_value |= (1 << i)
            else:
                new_A_value &= ~(1 << i)

        # Refine B bit
        if len(possible_B) == 1:
            bit = next(iter(possible_B))
            new_B_mask |= (1 << i)
            if bit:
                new_B_value |= (1 << i)
            else:
                new_B_value &= ~(1 << i)

    return new_A_value, new_A_mask, new_B_value, new_B_mask
    
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

    # A_v_s = "001000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
    # A_m_s = "111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111"
    # B_v_s = "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
    # B_m_s = "100000000000000000000000000000000000000000000000000000111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111"

    # C_v_s = "111100000011001110011000000000100000000000110001000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
    # C_m_s = "111111111111111111111111111111111111111111111111111110000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"



    # A = 0011
    A_v, A_m = 0b0011, 0b1111

    # B = 00??
    B_v, B_m = 0b0000, 0b1100

    # C = ?1??
    C_v, C_m = 0b0100, 0b0100

    width  = 4
    # A_v    = int(A_v_s, 2)
    # A_m    = int(A_m_s, 2)
    # B_v    = int(B_v_s, 2)
    # B_m    = int(B_m_s, 2)
    # C_v_r  = int(C_v_s, 2)
    # C_m_r  = int(C_m_s, 2)

    A_v_r, A_m_r, B_v_r, B_m_r = refine_add_backward(
        width,
        A_v, A_m,
        B_v, B_m,
        C_v, C_m
    )

    print("Refined A:", to_ternary(A_v_r, A_m_r, width))
    print("Refined B:", to_ternary(B_v_r, B_m_r, width))


if __name__ == "__main__":
    # test()
    if len(sys.argv) != 8:
        print("Error: expected width A_v A_m B_v B_m C_v_r C_m_r")
        sys.exit(1)

    width  = int(sys.argv[1])
    A_v    = int(sys.argv[2], 2)
    A_m    = int(sys.argv[3], 2)
    B_v    = int(sys.argv[4], 2)
    B_m    = int(sys.argv[5], 2)
    C_v_r  = int(sys.argv[6], 2)
    C_m_r  = int(sys.argv[7], 2)

    result = refine_add_backward(width, A_v, A_m, B_v, B_m, C_v_r, C_m_r)
    A_v_r, A_m_r, B_v_r, B_m_r = result
    fmt = f'0{width}b'
    print(f"{format(A_v_r,fmt)},{format(A_m_r,fmt)},{format(B_v_r,fmt)},{format(B_m_r,fmt)}")