
#reverse Concatenation

from z3 import *

# Helper to format value and mask into a '0', '1', '?' ternary string
def to_ternary_str(val_int, msk_int, width):
    val_bin = format(val_int, f'0{width}b')
    msk_bin = format(msk_int, f'0{width}b')
    
    result = []
    for v, m in zip(val_bin, msk_bin):
        if m == '1':
            result.append(v)  # Mask is 1: Bit is known (0 or 1)
        else:
            result.append('?') # Mask is 0: Bit is unknown ('?')
    return "".join(result)

# 1. Define widths of the operands
n_A = 4  # Operand A width
n_B = 4  # Operand B width
N = n_A + n_B  # Total width of X (8 bits)

# 2. Define concrete inputs for the concatenated vector X
# Let's say we want X to represent the ternary value: "0 1 ? 1 ? 0 0 1"
# - Value vector:  0 1 0 1 0 0 0 1 -> bin: 01010001 -> hex: 0x51
# - Mask vector:   1 1 0 1 0 1 1 1 -> bin: 11010111 -> hex: 0xD7
concrete_val_X = 0x51
concrete_msk_X = 0xD7

print(f"Concatenated input X: {to_ternary_str(concrete_val_X, concrete_msk_X, N)}")
print("-" * 40)

# 3. Define Z3 variables
val_X = BitVec('val_X', N)
msk_X = BitVec('msk_X', N)

# Declare symbolic variables for the recovered operands
val_A = BitVec('val_A', n_A)
msk_A = BitVec('msk_A', n_A)
val_B = BitVec('val_B', n_B)
msk_B = BitVec('msk_B', n_B)

# 4. Set up the solver and constraints
s = Solver()

# Constrain X to our concrete input values
s.add(val_X == concrete_val_X)
s.add(msk_X == concrete_msk_X)

# Define the extraction logic (reversing the concatenation)
# A is the upper part, B is the lower part
s.add(val_A == Extract(N - 1, n_B, val_X))
s.add(msk_A == Extract(N - 1, n_B, msk_X))

s.add(val_B == Extract(n_B - 1, 0, val_X))
s.add(msk_B == Extract(n_B - 1, 0, msk_X))

# 5. Solve and print the outputs
if s.check() == sat:
    model = s.model()
    
    # Extract integer values from the model
    res_val_A = model[val_A].as_long()
    res_msk_A = model[msk_A].as_long()
    
    res_val_B = model[val_B].as_long()
    res_msk_B = model[msk_B].as_long()
    
    # Format and display the outputs
    str_A = to_ternary_str(res_val_A, res_msk_A, n_A)
    str_B = to_ternary_str(res_val_B, res_msk_B, n_B)
    
    print(f"Recovered Operand A (Upper {n_A} bits):")
    print(f"  Ternary: {str_A}")
    print(f"  Value (binary): {format(res_val_A, f'0{n_A}b')}")
    print(f"  Mask  (binary): {format(res_msk_A, f'0{n_A}b')}")
    print()
    print(f"Recovered Operand B (Lower {n_B} bits):")
    print(f"  Ternary: {str_B}")
    print(f"  Value (binary): {format(res_val_B, f'0{n_B}b')}")
    print(f"  Mask  (binary): {format(res_msk_B, f'0{n_B}b')}")
else:
    print("No solution found.")
