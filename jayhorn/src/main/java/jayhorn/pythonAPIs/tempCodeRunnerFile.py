(sys.argv) != 8:
    #     print("Error: expected width A_v A_m B_v B_m C_v_r C_m_r")
    #     sys.exit(1)

    # width  = int(sys.argv[1])
    # A_v    = int(sys.argv[2], 2)
    # A_m    = int(sys.argv[3], 2)
    # B_v    = int(sys.argv[4], 2)
    # B_m    = int(sys.argv[5], 2)
    # C_v    = int(sys.argv[6], 2)
    # C_m    = int(sys.argv[7], 2)

    # result = refine_mul_backward(width, A_v, A_m, B_v, B_m, C_v, C_m)

    # if result is None:
    #     print("Error: Contradiction found, no valid shift fits the data.")
    #     sys.exit(1)

    # fmt = f'0{width}b'
    # output_parts = []
    # for A_v_r, A_m_r, B_v_r, B_m_r in result:
    #     output_parts.extend([
    #         format(A_v_r, fmt),
    #         format(A_m_r, fmt),
    #         format(B_v_r, fmt),
    #         format(B_m_r, fmt)
    #     ])
    # print(','.join(output_parts))
