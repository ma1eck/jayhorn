def to_ternary(val, msk, width):
    val_bin = format(val, f'0{width}b')
    msk_bin = format(msk, f'0{width}b')
    return "".join(v if m == '1' else '?' for v, m in zip(val_bin, msk_bin))

# --- Example Run ---
width = 4

# Original A is "0 0 ? ?"  -> value = 0x0, mask = 0xC
# Original B is "0 0 ? ?"  -> value = 0x0, mask = 0xC
# (They can each be 0, 1, 2, or 3)
A_v, A_m = 0x0, 0xC
B_v, B_m = 0x0, 0xC

# Get refined values
A_v_r, A_m_r, B_v_r, B_m_r = refine_ule_bvs(
    width, A_v, A_m, B_v, B_m, True
)

print(f"Original A:       {to_ternary(A_v, A_m, width)}")
print(f"Original B:       {to_ternary(B_v, B_m, width)}")
print("-" * 30)
print(f"Refined A (Out):  {to_ternary(A_v_r, A_m_r, width)}")
print(f"Refined B (Out):  {to_ternary(B_v_r, B_m_r, width)}")