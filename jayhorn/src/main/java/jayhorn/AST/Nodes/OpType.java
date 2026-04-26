package jayhorn.AST.Nodes;

public enum OpType {
    // Logic
    OR, AND, NOT,
    // Relational
    EQ, LE, LT, GE, GT,
    // Bit-Vector
    BIT2BOOL, BVADD, BVEXTRACT, BVCONCAT,
    BVULE, BVUGE, BVULT, BVUGT,
    BVLSHR, BVSHL, BVUDIV, BVMUL,

    FP_SIGN, FP_EXPONENT, FP_MANTISSA,
    EFP_SIGN, EFP_EXPONENT, EFP_MANTISSA

}