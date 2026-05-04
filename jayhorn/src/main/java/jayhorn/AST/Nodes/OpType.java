package jayhorn.AST.Nodes;

public enum OpType {
    // Logic
    OR, AND, NOT,

    ITE,

    // Relational
    EQ, LE, LT, GE, GT,

    MUL, ADD,

    // Bit-Vector
    BIT2BOOL, BVADD, BVEXTRACT, BVCONCAT,
    BVULE, BVUGE, BVULT, BVUGT,
    BVNEG, BVSUB,
    BVLSHR, BVSHL, BVUDIV, BVMUL,
    ZERO_EXTEND,

    FP_SIGN, FP_EXPONENT, FP_MANTISSA,
    EFP_SIGN, EFP_EXPONENT, EFP_MANTISSA,

    MOD_CAST, INT_CAST,

    EXISTS, FORALL,

    FLOATING_POINT, DOUBLE_FLOATING_POINT, EXTENDED_FLOATING_POINT, EXTENDED_DOUBLE_FLOATING_POINT;

    public VarType getResultVarType() {
        switch (this) {

            /* Boolean results */
            case OR:
            case AND:
            case NOT:
            case EQ:
            case LE:
            case LT:
            case GE:
            case GT:
            case BIT2BOOL:
            case BVULE:
            case BVUGE:
            case BVULT:
            case BVUGT:
            case EXISTS:
            case FORALL:
                return VarType.BOOLEAN;

            /* Integer arithmetic */
            case ADD:
            case MUL:
            case MOD_CAST:
            case INT_CAST:
                return VarType.INTEGER;

            /* Bitvector results */
            case BVADD:
            case BVEXTRACT:
            case BVCONCAT:
            case BVNEG:
            case BVSUB:
            case BVLSHR:
            case BVSHL:
            case BVUDIV:
            case BVMUL:
            case ZERO_EXTEND:
                return VarType.BITVECTOR;

            /* Floating-point components */
            case FP_SIGN:
            case EFP_SIGN:
                return VarType.BOOLEAN;
            case FP_EXPONENT:
            case FP_MANTISSA:
            case EFP_EXPONENT:
            case EFP_MANTISSA:
                return VarType.BITVECTOR;


            /* Floating-point values */
            case FLOATING_POINT:
                return VarType.FLOAT;

            case DOUBLE_FLOATING_POINT:
                return VarType.DOUBLE;

            case EXTENDED_FLOATING_POINT:
                return VarType.EFLOAT;

            case EXTENDED_DOUBLE_FLOATING_POINT:
                return VarType.EDOUBLE;

            /* Conditional expression (depends on branches) */
            case ITE:
                return VarType.ANY;

            default:
                return VarType.ANY;
        }
    }

}