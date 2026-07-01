import sys
from z3 import *

def to_ternary(val, mask, width):
    bits = []
    for i in reversed(range(width)):
        if (mask >> i) & 1:
            bits.append(str((val >> i) & 1))
        else:
            bits.append("?")
    return "".join(bits)

def merge_ternary_list(solutions, width):
    if not solutions:
        return (0, 0)
    
    merged_val = solutions[0][0]
    merged_mask = (1 << width) - 1 
    
    for val, mask in solutions[1:]:
        merged_mask = merged_mask & mask & ~(merged_val ^ val)
        merged_val = merged_val & merged_mask
        
    return merged_val, merged_mask

def refine_mul_backward(width, A_value, A_mask, B_value, B_mask, C_value, C_mask, threshold=8):
    s = Solver()
    
    A_concrete = BitVec('A_c', width)
    B_concrete = BitVec('B_c', width)
    C_concrete = BitVec('C_c', width)
    
    s.add(C_concrete == A_concrete * B_concrete)
    s.add((A_concrete & A_mask) == (A_value & A_mask))
    s.add((B_concrete & B_mask) == (B_value & B_mask))
    s.add((C_concrete & C_mask) == (C_value & C_mask))
    
    precise_sols = []
    
    while s.check() == sat:
        if len(precise_sols) >= threshold:
            A_merged_v, A_merged_m = merge_ternary_list([sol[0] for sol in precise_sols], width)
            B_merged_v, B_merged_m = merge_ternary_list([sol[1] for sol in precise_sols], width)
            
            return [(A_merged_v, A_merged_m, B_merged_v, B_merged_m)]
            
        model = s.model()
        a_val = model[A_concrete].as_long()
        b_val = model[B_concrete].as_long()
        
        precise_sols.append(((a_val, (1 << width) - 1), (b_val, (1 << width) - 1)))
        
        s.add(Or(A_concrete != a_val, B_concrete != b_val))
    
    if precise_sols:
        results = []
        for (a_val, a_mask), (b_val, b_mask) in precise_sols:
            results.append((a_val, a_mask, b_val, b_mask))
        return results
    
    return None

def test():
    width = 12

    # A = 0???????????
    A_v, A_m = 0b000000000000, 0b100000000000

    # B = 0???????????
    B_v, B_m = 0b000000000000, 0b100000000000

    # C = 000000000110
    C_v, C_m = 0b000000000110, 0b111111111111

    result = refine_mul_backward(width, A_v, A_m, B_v, B_m, C_v, C_m)
    
    if result is None:
        print("No solution found")
        return
    
    for A_v_r, A_m_r, B_v_r, B_m_r in result:
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
    C_v    = int(sys.argv[6], 2)
    C_m    = int(sys.argv[7], 2)

    result = refine_mul_backward(width, A_v, A_m, B_v, B_m, C_v, C_m)

    if result is None:
        print("Error: Contradiction found, no valid shift fits the data.")
        sys.exit(1)

    fmt = f'0{width}b'
    output_parts = []
    for A_v_r, A_m_r, B_v_r, B_m_r in result:
        output_parts.extend([
            format(A_v_r, fmt),
            format(A_m_r, fmt),
            format(B_v_r, fmt),
            format(B_m_r, fmt)
        ])
    print(','.join(output_parts))
