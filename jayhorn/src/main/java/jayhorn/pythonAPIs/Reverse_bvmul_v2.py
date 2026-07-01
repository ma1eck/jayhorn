from z3 import *

def to_ternary_str(val, mask, width):
    res = []
    for i in range(width - 1, -1, -1):
        m_bit = (mask >> i) & 1
        v_bit = (val >> i) & 1
        res.append(str(v_bit) if m_bit else "?")
    return "".join(res)

def merge_ternary_list(solutions, width):
   
    if not solutions:
        return (0, 0)
    
    
    merged_val = solutions[0][0]
    merged_mask = (1 << width) - 1 
    
    for val, mask in solutions[1:]:
        merged_mask = merged_mask & mask & ~(merged_val ^ val)
        merged_val = merged_val & merged_mask
        
    return merged_val, merged_mask

def refine_bvmul_with_threshold(width, A_v, A_m, B_v, B_m, C_v, C_m, threshold=8):
   
    s = Solver()
    
    A_concrete = BitVec('A_c', width)
    B_concrete = BitVec('B_c', width)
    C_concrete = BitVec('C_c', width)
    
    s.add(C_concrete == A_concrete * B_concrete)
    s.add((A_concrete & A_m) == (A_v & A_m))
    s.add((B_concrete & B_m) == (B_v & B_m))
    s.add((C_concrete & C_m) == (C_v & C_m))
    
    precise_sols = []
    
    while s.check() == sat:

        if len(precise_sols) >= threshold:
            print(f"[Warning] Solutions exceeded threshold ({threshold}). Merging to prevent state explosion...")
            A_merged_v, A_merged_m = merge_ternary_list([sol[0] for sol in precise_sols], width)
            B_merged_v, B_merged_m = merge_ternary_list([sol[1] for sol in precise_sols], width)
            
            return [((A_merged_v, A_merged_m), (B_merged_v, B_merged_m))]
            
        model = s.model()
        a_val = model[A_concrete].as_long()
        b_val = model[B_concrete].as_long()
        
        precise_sols.append(((a_val, (1 << width) - 1), (b_val, (1 << width) - 1)))
        
        s.add(Or(A_concrete != a_val, B_concrete != b_val))
        
    return precise_sols

if __name__ == "__main__":
    width = 4
    A_val, A_mask = 0b0011, 0b1111 # 0011
    B_val, B_mask = 0b0000, 0b1100 # 00??
    C_val, C_mask = 0b0010, 0b1010 # 0?1?
    
    print("--- Test with Threshold = 5 ---")
    sols_precise = refine_bvmul_with_threshold(width, A_val, A_mask, B_val, B_mask, C_val, C_mask, threshold=5)
    for idx, (A_sol, B_sol) in enumerate(sols_precise, 1):
        print(f"Option {idx}: A = {to_ternary_str(A_sol[0], A_sol[1], width)}, B = {to_ternary_str(B_sol[0], B_sol[1], width)}")
        
    print("\n--- Test with Threshold = 1 (Force Merge) ---")
    sols_merged = refine_bvmul_with_threshold(width, A_val, A_mask, B_val, B_mask, C_val, C_mask, threshold=1)
    for idx, (A_sol, B_sol) in enumerate(sols_merged, 1):
        print(f"Merged Output: A = {to_ternary_str(A_sol[0], A_sol[1], width)}, B = {to_ternary_str(B_sol[0], B_sol[1], width)}")
