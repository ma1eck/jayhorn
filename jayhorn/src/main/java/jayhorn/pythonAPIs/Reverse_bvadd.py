
#Reverse Addition

from z3 import *

def refine_operands_with_subtraction(
    width, 
    # Original states
    A_value, A_mask, 
    B_value, B_mask,
    # Refined state of C
    C_value_refined, C_mask_refined
):
    """
    Given original A and B, and a newly refined C (where C = A + B),
    we use subtraction (bvsub) to find the refined A and B.
    """
    s = Optimize()

    # 1. Define output variables we want to find
    A_value_refined = BitVec('A_value_refined', width)
    A_mask_refined  = BitVec('A_mask_refined', width)
    
    B_value_refined = BitVec('B_value_refined', width)
    B_mask_refined  = BitVec('B_mask_refined', width)

    # 2. Monotonicity Constraint (We cannot "lose" information we already had)
    # The refined masks must keep all 1s from the original masks
    s.add((A_mask & ~A_mask_refined) == 0)
    s.add((B_mask & ~B_mask_refined) == 0)
    # The known bits in the original must match the refined bits
    s.add((A_value_refined & A_mask) == (A_value & A_mask))
    s.add((B_value_refined & B_mask) == (B_value & B_mask))

    # 3. Apply the subtraction (bvsub) relationship
    # We create concrete bit-vectors to represent possible valid values
    concrete_A = BitVec('concrete_A', width)
    concrete_B = BitVec('concrete_B', width)
    concrete_C = BitVec('concrete_C', width)

    # Here is where subtraction (bvsub) is explicitly used:
    # We force Z3 to evaluate the subtraction relationship
    s.add(concrete_A == concrete_C - concrete_B) # This is bvsub

    # Link the concrete values to our refined ternary masks
    s.add((concrete_A & A_mask_refined) == (A_value_refined & A_mask_refined))
    s.add((concrete_B & B_mask_refined) == (B_value_refined & B_mask_refined))
    s.add((concrete_C & C_mask_refined) == (C_value_refined & C_mask_refined))

    # 4. We want the MOST refined answer (maximize 1s in the masks)
    s.maximize(Sum([ZeroExt(width, Extract(i, i, A_mask_refined)) for i in range(width)]))
    s.maximize(Sum([ZeroExt(width, Extract(i, i, B_mask_refined)) for i in range(width)]))

    if s.check() == sat:
        m = s.model()
        return (
            m[A_value_refined].as_int(), m[A_mask_refined].as_int(),
            m[B_value_refined].as_int(), m[B_mask_refined].as_int()
        )
    else:
        raise Exception("Contradictory constraints. No solution exists.")

# --- Verification Helper ---
def to_ternary(val, msk, width):
    val_bin = format(val, f'0{width}b')
    msk_bin = format(msk, f'0{width}b')
    return "".join(v if m == '1' else '?' for v, m in zip(val_bin, msk_bin))

# # --- Example Run ---
# width = 4

# # Original A is "0 0 ? ?"  -> value = 0x0, mask = 0xC
# # Original B is "0 0 ? ?"  -> value = 0x0, mask = 0xC
# # (They can each be 0, 1, 2, or 3)
# A_v, A_m = 0x0, 0xC
# B_v, B_m = 0x0, 0xC

# # We learn C is refined to exactly 5 ("0 1 0 1") -> value = 0x5, mask = 0xF
# C_v_r, C_m_r = 0x5, 0xF

# # Get refined values
# A_v_r, A_m_r, B_v_r, B_m_r = refine_operands_with_subtraction(
#     width, A_v, A_m, B_v, B_m, C_v_r, C_m_r
# )

# print(f"Original A:       {to_ternary(A_v, A_m, width)}")
# print(f"Original B:       {to_ternary(B_v, B_m, width)}")
# print(f"Refined C:        {to_ternary(C_v_r, C_m_r, width)}")
# print("-" * 30)
# print(f"Refined A (Out):  {to_ternary(A_v_r, A_m_r, width)}")
# print(f"Refined B (Out):  {to_ternary(B_v_r, B_m_r, width)}")




if __name__ == "__main__":
    if len(sys.argv) != 8:
        print("Error: expected width A_v A_m B_v B_m C_v_r C_m_r")
        sys.exit(1)

    width  = int(sys.argv[1])
    A_v    = int(sys.argv[2])
    A_m    = int(sys.argv[3])
    B_v    = int(sys.argv[4])
    B_m    = int(sys.argv[5])
    C_v_r  = int(sys.argv[6])
    C_m_r  = int(sys.argv[7])

    result = refine_operands_with_subtraction(width, A_v, A_m, B_v, B_m, C_v_r, C_m_r)
    A_v_r, A_m_r, B_v_r, B_m_r = result
    fmt = f'0{width}b'
    print(f"{format(A_v_r,fmt)},{format(A_m_r,fmt)},{format(B_v_r,fmt)},{format(B_m_r,fmt)}")