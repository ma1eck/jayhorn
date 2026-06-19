if len(sys.argv) != 7:
    #     print("Error: expected E_val E_mask M_val M_mask S_val S_mask")
    #     sys.exit(1)

    # E_val = int(sys.argv[1], 2)
    # E_mask = ~int(sys.argv[2], 2) & ((1<<11) - 1) # invert mask to get free bits
    # M_val = int(sys.argv[3], 2)
    # M_mask = ~int(sys.argv[4], 2) & ((1<<53) - 1) # invert mask to get free bits
    # if (sys.argv[5]== "false"):
    #     S_val = 0
    # elif (sys.argv[5] == "true"):
    #     S_val = 1
    # else:
    #     S_val = int(sys.argv[5], 2)
    # if (sys.argv[6] == "false"):
    #     S_mask = 0
    # elif (sys.argv[6] == "true"):
    #     S_mask = 1
    # else:
    #     S_mask = int(sys.argv[6], 2)

    # # print(E_val, E_mask, M_val, M_mask, S_val, S_mask)
    # S_mask = 1 - S_mask # invert mask to get free bits

    # if not is_exact_mantissa_case(M_mask):
    #     is_exact_flag = False
    #     intervals = min_max_for_mask(E_val, E_mask, M_val, M_mask, S_val, S_mask)
    # else:
    #     intervals = bitmask_to_intervals(E_val, E_mask, M_val, M_mask, S_val, S_mask)
    #     is_exact_flag = True
    #     if len(intervals) >= TOO_MANY_INTERVAL:
    #         is_exact_flag = False
    #         intervals = min_max_for_mask(E_val, E_mask, M_val, M_mask, S_val, S_mask)


    # for lo, hi in intervals:
    #     print(f"{lo},{hi}")
    # print(f"The result is {'' if is_exact_flag else 'not '}exact")
