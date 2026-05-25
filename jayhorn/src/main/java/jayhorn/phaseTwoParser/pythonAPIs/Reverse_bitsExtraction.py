# Reverse Extraction
from z3 import *

# Helper to format value and mask into a '0', '1', '?' ternary string
def to_ternary_str(val_int, msk_int, width):
    val_bin = format(val_int, f'0{width}b')
    msk_bin = format(msk_int, f'0{width}b')
    return "".join(v if m == '1' else '?' for v, m in zip(val_bin, msk_bin))

def update_slice(A_val, A_msk, B_val, B_msk, high, low, N_A):
    """
    Replaces the slice [high:low] of A with B, keeping the rest of A intact.
    """
    val_parts = []
    msk_parts = []
    
    # 1. Upper Part: A[N_A-1 : high+1]
    if high < N_A - 1:
        val_parts.append(Extract(N_A - 1, high + 1, A_val))
        msk_parts.append(Extract(N_A - 1, high + 1, A_msk))
        
    # 2. Middle Part: B (replaces A[high:low])
    val_parts.append(B_val)
    msk_parts.append(B_msk)
    
    # 3. Lower Part: A[low-1 : 0]
    if low > 0:
        val_parts.append(Extract(low - 1, 0, A_val))
        msk_parts.append(Extract(low - 1, 0, A_msk))
        
    # Concatenate the parts back together
    A_val_new = Concat(*val_parts) if len(val_parts) > 1 else val_parts[0]
    A_msk_new = Concat(*msk_parts) if len(msk_parts) > 1 else msk_parts[0]
    
    return A_val_new, A_msk_new

# --- Example Scenario ---

# Dimensions
N_A = 8
high = 5
low = 2
N_B = high - low + 1 # 4 bits

# 1. Original A: "1 ? 0 ? ? 1 0 ?" (0x82, 0xA6)
val_A_orig = BitVecVal(0x82, N_A)
msk_A_orig = BitVecVal(0xA6, N_A)

# 2. Extract B from A (Indices 5 down to 2)
# Original slice in A is: "? ? 1 0" (at indices 5, 4, 3, 2)
val_B_extracted = Extract(high, low, val_A_orig)
msk_B_extracted = Extract(high, low, msk_A_orig)

# 3. Suppose we refine B (e.g., through some solver constraint or logic)
# Let's say we refined "? ? 1 0" into "0 1 1 0" (Mask is now all 1s, first ? is 0, second ? is 1)
refined_val_B = BitVecVal(0x6, N_B) # bin: 0110
refined_msk_B = BitVecVal(0xF, N_B) # bin: 1111 (fully defined)

# 4. Refine A by replacing its old slice with the refined B
val_A_refined, msk_A_refined = update_slice(
    val_A_orig, msk_A_orig, 
    refined_val_B, refined_msk_B, 
    high, low, N_A
)

# 5. Evaluate and display results using Z3 Solver
s = Solver()
if s.check() == sat:
    # Evaluate expressions to concrete Python integers
    m = s.model()
    
    orig_A_str = to_ternary_str(m.evaluate(val_A_orig).as_long(), m.evaluate(msk_A_orig).as_long(), N_A)
    ext_B_str  = to_ternary_str(m.evaluate(val_B_extracted).as_long(), m.evaluate(msk_B_extracted).as_long(), N_B)
    ref_B_str  = to_ternary_str(m.evaluate(refined_val_B).as_long(), m.evaluate(refined_msk_B).as_long(), N_B)
    ref_A_str  = to_ternary_str(m.evaluate(val_A_refined).as_long(), m.evaluate(msk_A_refined).as_long(), N_A)
    
    print(f"Original A:       {orig_A_str}")
    print(f"Extracted B:          {ext_B_str}")
    print(f"Refined B:            {ref_B_str}")
    print(f"Refined A:        {ref_A_str}")
