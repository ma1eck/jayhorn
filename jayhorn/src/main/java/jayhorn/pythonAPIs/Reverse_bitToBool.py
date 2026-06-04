#reverse bitToBool

def refine_bit_to_bool_direct(A_v, A_m, index, bool_refined):
    # Create a bitmask for the target index (e.g., 1 << index)
    bit_selector = 1 << index
    
    # 1. Update mask: set the bit at 'index' to 1 (making it known)
    A_m_r = A_m | bit_selector
    
    # 2. Update value based on the refined boolean
    if bool_refined: # True -> set bit to 1
        A_v_r = A_v | bit_selector
    else:            # False -> set bit to 0
        A_v_r = A_v & ~bit_selector
        
    return A_v_r, A_m_r

if __name__ == "__main__":
    if len(sys.argv) != 5:
        print("Error: expected A_v A_m index bool_refined")
        sys.exit(1)
    A_v    = long(sys.argv[1])
    A_m    = long(sys.argv[2])
    index  = int(sys.argv[3])
    bool_refined = sys.argv[4].lower() == "true"

    result = refine_bit_to_bool_direct(A_v, A_m, index, bool_refined)
    print(f"{result[0]},{result[1]}")