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
        return (m[A_v_r].as_long(), m[A_m_r].as_long(),
                m[B_v_r].as_long(), m[B_m_r].as_long())
    else:
        raise Exception("Contradiction: Operands cannot satisfy this equality state.")


if __name__ == "__main__":
    if len(sys.argv) != 7:
        print("Error: expected width A_v A_m B_v B_m eq_refined")
        sys.exit(1)

    width    = int(sys.argv[1])
    A_v      = (sys.argv[2])
    A_m      = (sys.argv[3])
    B_v      = (sys.argv[4])
    B_m      = (sys.argv[5])
    eq_refined = sys.argv[6].lower() == "true"

    result = refine_equality_bvs(width, A_v, A_m, B_v, B_m, eq_refined)
    print(f"{result[0]},{result[1]},{result[2]},{result[3]}")
