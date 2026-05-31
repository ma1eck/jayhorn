
# Reverse integers EQ

from z3 import *

def refine_integer_equality(A_min, A_max, B_min, B_max, eq_refined: bool):
    """
    Refines the lower and upper bounds of two mathematical integers A and B
    based on whether they are equal (eq_refined = True) or not (eq_refined = False).
    """
    s = Optimize()

    # Refined bounds we want to find
    A_min_r, A_max_r = Int('A_min_r'), Int('A_max_r')
    B_min_r, B_max_r = Int('B_min_r'), Int('B_max_r')

    # 1. Monotonicity Constraints (Ranges can only shrink, never grow)
    s.add(A_min_r >= A_min), s.add(A_max_r <= A_max)
    s.add(B_min_r >= B_min), s.add(B_max_r <= B_max)
    s.add(A_min_r <= A_max_r) # Valid range check
    s.add(B_min_r <= B_max_r)

    # 2. Model the equality relationship using symbolic integers
    concrete_A, concrete_B = Int('concrete_A'), Int('concrete_B')
    
    # Concrete values must fall within the refined bounds
    s.add(concrete_A >= A_min_r, concrete_A <= A_max_r)
    s.add(concrete_B >= B_min_r, concrete_B <= B_max_r)

    if eq_refined:
        s.add(concrete_A == concrete_B)
    else:
        s.add(concrete_A != concrete_B)

    # 3. Optimization: Maximize the lower bounds and Minimize the upper bounds
    # (This shrinks the intervals to be as tight/precise as possible)
    s.maximize(A_min_r)
    s.minimize(A_max_r)
    s.maximize(B_min_r)
    s.minimize(B_max_r)

    if s.check() == sat:
        m = s.model()
        return (m[A_min_r].as_long(), m[A_max_r].as_long(),
                m[B_min_r].as_long(), m[B_max_r].as_long())
    else:
        raise Exception("Contradiction: Integer ranges cannot satisfy this equality state.")

# --- Example Run ---
# A is in [1, 10]
# B is in [5, 15]
# We learn A == B is True
# A_min, A_max, B_min, B_max = 1, 10, 5, 15
# print(refine_integer_equality(A_min, A_max, B_min, B_max, eq_refined=True))
# # Output: (5, 10, 5, 10) -> Both are refined to [5, 10]




if __name__ == "__main__":
    if len(sys.argv) != 7:
        print("Error: expected width A_v A_m B_v B_m eq_refined")
        sys.exit(1)

    A_min   = long(sys.argv[1])
    A_max   = long(sys.argv[2])
    B_min   = long(sys.argv[3])
    B_max   = long(sys.argv[4])
    eq_refined = sys.argv[5].lower() == "true"

    result = refine_integer_equality(A_min, A_max, B_min, B_max, eq_refined)
    print(f"{result[0]},{result[1]},{result[2]},{result[3]}")
