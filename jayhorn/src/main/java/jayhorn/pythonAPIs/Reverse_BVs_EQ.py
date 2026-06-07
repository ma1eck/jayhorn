# Reverse_BVs_EQ.py
import sys
from z3 import *

def refine_equality_bvs(width, A_v, A_m, B_v, B_m, eq_refined: bool):
    s = Optimize()

    A_v_r, A_m_r = BitVec('A_v_r', width), BitVec('A_m_r', width)
    B_v_r, B_m_r = BitVec('B_v_r', width), BitVec('B_m_r', width)

    s.add((A_m & ~A_m_r) == 0), s.add((B_m & ~B_m_r) == 0)
    s.add((A_v_r & A_m) == (A_v & A_m)), s.add((B_v_r & B_m) == (B_v & B_m))

    concrete_A, concrete_B = BitVec('concrete_A', width), BitVec('concrete_B', width)

    if eq_refined:
        s.add(concrete_A == concrete_B)
    else:
        s.add(concrete_A != concrete_B)

    s.add((concrete_A & A_m_r) == (A_v_r & A_m_r))
    s.add((concrete_B & B_m_r) == (B_v_r & B_m_r))

    s.maximize(Sum([ZeroExt(width, Extract(i, i, A_m_r)) for i in range(width)]))
    s.maximize(Sum([ZeroExt(width, Extract(i, i, B_m_r)) for i in range(width)]))

    if s.check() == sat:
        m = s.model()
        fmt = f'0{width}b'
        return (format(m[A_v_r].as_long(), fmt), format(m[A_m_r].as_long(), fmt),
                format(m[B_v_r].as_long(), fmt), format(m[B_m_r].as_long(), fmt))
    else:
        raise Exception("Contradiction: Operands cannot satisfy this equality state.")

def to_ternary(val, msk, width):
    val_bin = format(val, f'0{width}b')
    msk_bin = format(msk, f'0{width}b')
    return "".join(v if m == '1' else '?' for v, m in zip(val_bin, msk_bin))

def example():
    # --- Example Run ---
    width = 4

    # Original A is "0 0 ? ?"  -> value = 0x0, mask = 0xC
    # Original B is "0 0 ? ?"  -> value = 0x0, mask = 0xC
    # (They can each be 0, 1, 2, or 3)
    A_v, A_m = 0x0, 0xC
    B_v, B_m = 0x0, 0xC

    # Get refined values
    A_v_r, A_m_r, B_v_r, B_m_r = refine_equality_bvs(
        width, A_v, A_m, B_v, B_m, True
    )

    print(f"Original A:       {to_ternary(A_v, A_m, width)}")
    print(f"Original B:       {to_ternary(B_v, B_m, width)}")
    print("-" * 30)
    print(f"Refined A (Out):  {to_ternary(A_v_r, A_m_r, width)}")
    print(f"Refined B (Out):  {to_ternary(B_v_r, B_m_r, width)}")


if __name__ == "__main__":
    if len(sys.argv) != 7:
        print("Error: expected width A_v A_m B_v B_m eq_refined")
        sys.exit(1)

    width = int(sys.argv[1])
    A_v = int(sys.argv[2], 2)
    A_m = int(sys.argv[3], 2)
    B_v = int(sys.argv[4], 2)
    B_m = int(sys.argv[5], 2)
    eq_refined = sys.argv[6].lower() == "true"

    result = refine_equality_bvs(width, A_v, A_m, B_v, B_m, eq_refined)
    print(f"{result[0]},{result[1]},{result[2]},{result[3]}")