from z3 import *
import sys


def infer_addition_result_mask(Avalue_str, Amask_str, Bvalue_str, Bmask_str):
    """
    Infer the most precise masked result of adding two masked bit-vectors.

    A bit is considered "known" if its corresponding mask bit is 1.

    Args:
        Avalue_str (str): Binary string for A's known value bits.
        Amask_str  (str): Binary mask string for A.
        Bvalue_str (str): Binary string for B's known value bits.
        Bmask_str  (str): Binary mask string for B.

    Returns:
        tuple[str, str]:
            (result_value_binary, result_mask_binary)
    """

    # Validate input sizes
    bit_width = len(Avalue_str)

    if not all(len(x) == bit_width for x in [
        Amask_str, Bvalue_str, Bmask_str
    ]):
        raise ValueError("All binary strings must have the same length.")

    # Convert binary strings to integers
    Avalue = int(Avalue_str, 2)
    Amask  = int(Amask_str, 2)
    Bvalue = int(Bvalue_str, 2)
    Bmask  = int(Bmask_str, 2)

    # Optimizer
    opt = Optimize()

    # Unknown result mask/value
    R_mask = BitVec('R_mask', bit_width)
    R_val  = BitVec('R_val', bit_width)

    # Quantified variables
    a = BitVec('a', bit_width)
    b = BitVec('b', bit_width)

    # Ensure unknown bits in R_val are zero
    opt.add((R_val & ~R_mask) == 0)

    # For all matching inputs, result must match masked output
    implication = Implies(
        And(
            (a & Amask) == Avalue,
            (b & Bmask) == Bvalue
        ),
        ((a + b) & R_mask) == R_val
    )

    opt.add(ForAll([a, b], implication))

    # Maximize known result bits
    opt.maximize(R_mask)

    # Solve
    if opt.check() != sat:
        raise ValueError("Constraints are unsatisfiable.")

    model = opt.model()

    result_mask = model[R_mask].as_long()
    result_value = model[R_val].as_long()

    return (
        f"{result_mask:0{bit_width}b}",
        f"{result_value:0{bit_width}b}"
    )



def test():
    example_input = [
        ("1010", "1111", "0000", "0000"),  # A=10 (known), B=0 (unknown)
        ("1010", "1111", "0001", "1111")  # A=10 (known), B=1 (known)
        # ("1010", "1111", "001?", "1110"),  # A=10 (known), B=0 or 1 (partially known)
        # ("????", "0000", "????", "0000"),  # A and B completely unknown
        ]
    for Avalue_str, Amask_str, Bvalue_str, Bmask_str in example_input:
        result_value, result_mask = infer_addition_result_mask(
            Avalue_str, Amask_str, Bvalue_str, Bmask_str
        )
        print(f"A: {Avalue_str} (mask: {Amask_str}), "
              f"B: {Bvalue_str} (mask: {Bmask_str}) -> "
              f"Result: {result_value} (mask: {result_mask})")

if __name__ == "__main__":
    if len(sys.argv) != 5:
        print("Error: Expected 4 arguments")
        sys.exit(1)

    val, mask = infer_addition_result_mask(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4])
    print(f"{val},{mask}")
