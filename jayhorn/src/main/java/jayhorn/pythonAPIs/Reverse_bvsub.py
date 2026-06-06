def refine_sub_backward(
    width,
    A_value, A_mask,
    B_value, B_mask,
    C_value, C_mask
):
    """
    Backward refinement for: C = A - B (mod 2^width)
    """

    # Helper: same as before
    def possible_bits(val, mask, i):
        if (mask >> i) & 1:
            return {(val >> i) & 1}
        return {0, 1}

    # 1. Forward Pass: Collect possible borrows
    borrows_possible = [set() for _ in range(width + 1)]
    borrows_possible[0] = {0} # No incoming borrow at LSB

    for i in range(width):
        Ai = possible_bits(A_value, A_mask, i)
        Bi = possible_bits(B_value, B_mask, i)
        Ci = possible_bits(C_value, C_mask, i)
        
        next_borrows = set()
        for bin_ in borrows_possible[i]:
            for a in Ai:
                for b in Bi:
                    # SUBTRACTION LOGIC
                    # A_i - B_i - Borr_in = C_i - 2 * Borr_out
                    diff = a - b - bin_
                    res = diff % 2
                    bout = 1 if diff < 0 else 0

                    if res in Ci:
                        next_borrows.add(bout)
        borrows_possible[i + 1] = next_borrows

    # 2. Backward Pass: Filter valid borrows
    valid_borrows = [set() for _ in range(width + 1)]
    valid_borrows[width] = borrows_possible[width].copy()

    for i in reversed(range(width)):
        Ai = possible_bits(A_value, A_mask, i)
        Bi = possible_bits(B_value, B_mask, i)
        Ci = possible_bits(C_value, C_mask, i)

        for bin_ in borrows_possible[i]:
            for a in Ai:
                for b in Bi:
                    diff = a - b - bin_
                    res = diff % 2
                    bout = 1 if diff < 0 else 0

                    if res in Ci and bout in valid_borrows[i + 1]:
                        valid_borrows[i].add(bin_)

    # 3. Refinement Pass: Update bits
    new_A_v, new_A_m = A_value, A_mask
    new_B_v, new_B_m = B_value, B_mask

    for i in range(width):
        Ai = possible_bits(A_value, A_mask, i)
        Bi = possible_bits(B_value, B_mask, i)
        Ci = possible_bits(C_value, C_mask, i)

        possible_A = set()
        possible_B = set()

        for bin_ in valid_borrows[i]:
            for a in Ai:
                for b in Bi:
                    diff = a - b - bin_
                    res = diff % 2
                    bout = 1 if diff < 0 else 0

                    if res in Ci and bout in valid_borrows[i + 1]:
                        possible_A.add(a)
                        possible_B.add(b)

        # Update masks/values
        if len(possible_A) == 1:
            bit = next(iter(possible_A))
            new_A_m |= (1 << i)
            new_A_v = (new_A_v & ~(1 << i)) | (bit << i)

        if len(possible_B) == 1:
            bit = next(iter(possible_B))
            new_B_m |= (1 << i)
            new_B_v = (new_B_v & ~(1 << i)) | (bit << i)

    return new_A_v, new_A_m, new_B_v, new_B_m

def to_ternary(val, mask, width):
    bits = []
    for i in reversed(range(width)):
        if (mask >> i) & 1:
            bits.append(str((val >> i) & 1))
        else:
            bits.append("?")
    return "".join(bits)

width = 4

# Example: A - B = C
# Let's say: A = 1?1?, B = ?0?0, Result C = 0111 (7)
# This setup forces the refinement to solve for the missing bits.

# A = 1?1? (1010 in binary for the known bits)
A_v, A_m = 0b1010, 0b1010

# B = ?0?0 (0000 in binary for the known bits)
B_v, B_m = 0b0000, 0b0101

# C = 0111 (7)
C_v, C_m = 0b0111, 0b1111

A_v_r, A_m_r, B_v_r, B_m_r = refine_sub_backward(
    width,
    A_v, A_m,
    B_v, B_m,
    C_v, C_m
)

print("Refined A:", to_ternary(A_v_r, A_m_r, width))
print("Refined B:", to_ternary(B_v_r, B_m_r, width))