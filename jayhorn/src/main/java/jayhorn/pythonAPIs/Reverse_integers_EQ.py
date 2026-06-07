import sys
from z3 import *

INF = float('inf')
BIG = 10**18

def parse_bound(s):
    s = s.strip().lower()
    if s == 'inf': return INF
    if s == '-inf': return -INF
    return int(s)

def format_bound(v):
    if v >= BIG: return 'inf'
    if v <= -BIG: return '-inf'
    return str(v)

def refine_integer_equality(A_min, A_max, B_min, B_max, eq_refined: bool):
    s = Optimize()
    A_min_r, A_max_r = Int('A_min_r'), Int('A_max_r')
    B_min_r, B_max_r = Int('B_min_r'), Int('B_max_r')

    # Monotonicity — finite bounds only
    if A_min != -INF: s.add(A_min_r >= int(A_min))
    if A_max !=  INF: s.add(A_max_r <= int(A_max))
    if B_min != -INF: s.add(B_min_r >= int(B_min))
    if B_max !=  INF: s.add(B_max_r <= int(B_max))

    # Sentinel anchors for fully-unbounded sides (prevents Z3 divergence)
    if A_min == -INF: s.add(A_min_r >= -BIG)
    if A_max ==  INF: s.add(A_max_r <=  BIG)
    if B_min == -INF: s.add(B_min_r >= -BIG)
    if B_max ==  INF: s.add(B_max_r <=  BIG)

    s.add(A_min_r <= A_max_r)
    s.add(B_min_r <= B_max_r)

    concrete_A, concrete_B = Int('concrete_A'), Int('concrete_B')
    s.add(concrete_A >= A_min_r, concrete_A <= A_max_r)
    s.add(concrete_B >= B_min_r, concrete_B <= B_max_r)

    if eq_refined:
        s.add(concrete_A == concrete_B)
    else:
        s.add(concrete_A != concrete_B)

    s.maximize(A_min_r)
    s.minimize(A_max_r)
    s.maximize(B_min_r)
    s.minimize(B_max_r)

    if s.check() != sat:
        raise Exception("Contradiction: Integer ranges cannot satisfy this equality state.")

    m = s.model()
    return (m[A_min_r].as_long(), m[A_max_r].as_long(),
            m[B_min_r].as_long(), m[B_max_r].as_long())


def example():
    A_min = -2147483648
    A_max = 2147483647
    B_min = 1
    B_max = 1
    eq_refined = False

    result = refine_integer_equality(A_min, A_max, B_min, B_max, eq_refined)
    print(f"A_min_result: {result[0]}")
    print(f"A_max_result: {result[1]}")
    print(f"B_min_result: {result[2]}")
    print(f"B_max_result: {result[3]}")
    return


if __name__ == "__main__":
    # example()
    if len(sys.argv) != 6:
        print("Error: expected A_min A_max B_min B_max eq_refined")
        sys.exit(1)

    A_min = parse_bound(sys.argv[1])
    A_max = parse_bound(sys.argv[2])
    B_min = parse_bound(sys.argv[3])
    B_max = parse_bound(sys.argv[4])
    eq_refined = sys.argv[5].lower() == "true"

    r = refine_integer_equality(A_min, A_max, B_min, B_max, eq_refined)
    print(",".join(format_bound(v) for v in r))
