package jayhorn.AST.SpacerParser;

import com.microsoft.z3.*;
import com.microsoft.z3.enumerations.Z3_decl_kind;
import jayhorn.AST.ASTHelper;
import jayhorn.AST.Nodes.*;

import java.math.BigInteger;
import java.util.*;

public class Parser {
    static public InvariantTree convertExpr(Expr expr) {

        // Literals
        if (expr.isNumeral()){
            if (expr.isIntNum()) {
                return new LiteralNode(((IntNum) expr).getInt(), VarType.INTEGER);
            }
            if (expr.isBV()) {
                BitVecNum bv = (BitVecNum) expr;
                return new LiteralNode(bv.getSExpr(), VarType.BITVECTOR); // always storing as string
            }
            if (expr.isTrue()) {
                return new LiteralNode(true, VarType.BOOLEAN);
            }
            if (expr.isFalse()) {
                return new LiteralNode(false, VarType.BOOLEAN);
            }
        }

        // Variables
//        if (expr.isConst() && expr.getFuncDecl().getDeclKind() == Z3_decl_kind.Z3_OP_UNINTERPRETED) {
        if (expr.isConst() && expr.getArgs().length==0) {
            String name = expr.getFuncDecl().getName().toString();
            VarType type = inferType(expr);
//            symbolTable.put(name, type);
            return new VariableNode(name, type);
        }

        // Datatype
        if (expr.isApp() && expr.getSort() instanceof DatatypeSort) {
            DatatypeSort dtSort = (DatatypeSort) expr.getSort();
            String sortName = dtSort.getName().toString();

            VarType fpType = null;
            if (sortName.equals("FloatingPoint"))
                fpType = VarType.FLOAT;
            else if (sortName.equals("DoubleFloatingPoint"))
                fpType = VarType.DOUBLE;
            else if (sortName.equals("ExtendedFloatingPoint"))
                fpType = VarType.EFLOAT;
            else if (sortName.equals("ExtendedDoubleFloatingPoint"))
                fpType = VarType.EDOUBLE;

            if (fpType != null){
                // Check if it's a constructor application (literal)
                FuncDecl funcDecl = expr.getFuncDecl();
                if (funcDecl.getDeclKind() == Z3_decl_kind.Z3_OP_DT_CONSTRUCTOR) {
                    // Extract sign, exponent, mantissa from constructor arguments
                    Expr[] args = expr.getArgs();
                    if (args.length == 3) {
                        // Store as a map or custom object
                        Map<String, Object> fpValue = new HashMap<>();
                        fpValue.put("sign", extractValue(args[0]));
                        fpValue.put("exponent", extractValue(args[1]));
                        fpValue.put("mantissa", extractValue(args[2]));
                        return new LiteralNode(fpValue, fpType);
                    }
                }
            }
        }


        // Operations
        return convertOperation(expr);
    }

    static private Object extractValue(Expr expr) {
        if (expr.isTrue()) return true;
        if (expr.isFalse()) return false;
        if (expr.isIntNum()) return ((IntNum) expr).getInt();
        if (expr.isBV()){
            BigInteger val = ((BitVecNum) expr).getBigInteger();
            return ASTHelper.shrinkBigInteger(val);
        }
        return expr.toString();
    }

    static private InvariantTree convertOperation(Expr expr) {
        List<InvariantTree> children = new ArrayList<>();
        for (Expr arg : expr.getArgs()) {
            children.add(convertExpr(arg));
        }

        // Logic operations
        if (expr.isOr()) return new OperationNode(OpType.OR, children);
        if (expr.isAnd()) return new OperationNode(OpType.AND, children);
        if (expr.isNot()) return new OperationNode(OpType.NOT, children);

        // Relational operations
        if (expr.isEq()) return new OperationNode(OpType.EQ, children);
        if (expr.isLE()) return new OperationNode(OpType.LE, children);
        if (expr.isLT()) return new OperationNode(OpType.LT, children);
        if (expr.isGE()) return new OperationNode(OpType.GE, children);
        if (expr.isGT()) return new OperationNode(OpType.GT, children);

        // Bit-vector operations
        if (expr.isBVAdd()) return new OperationNode(OpType.BVADD, children);
        if (expr.isBVConcat()) return new OperationNode(OpType.BVCONCAT, children);
        if (expr.isBVULE()) return new OperationNode(OpType.BVULE, children);
        if (expr.isBVUGE()) return new OperationNode(OpType.BVUGE, children);
        if (expr.isBVULT()) return new OperationNode(OpType.BVULT, children);
        if (expr.isBVUGT()) return new OperationNode(OpType.BVUGT, children);
        if (expr.isBVShiftRightLogical()) return new OperationNode(OpType.BVLSHR, children);
        if (expr.isBVShiftLeft()) return new OperationNode(OpType.BVSHL, children);
        if (expr.isBVUDiv()) return new OperationNode(OpType.BVUDIV, children);
        if (expr.isBVMul()) return new OperationNode(OpType.BVMUL, children);

        // Operations with parameters
        if (expr.isBVExtract()) {
            int high = expr.getFuncDecl().getParameters()[0].getInt();
            int low = expr.getFuncDecl().getParameters()[1].getInt();
            return new OperationNode(OpType.BVEXTRACT, children, Arrays.asList(high, low));
        }

        if (expr.getFuncDecl().getName().toString().equals("bit2bool")) {
            int bitIndex = expr.getFuncDecl().getParameters()[0].getInt();
            return new OperationNode(OpType.BIT2BOOL, children, Arrays.asList(bitIndex));
        }

        // Handle datatype accessors (e.g., sign, exponent, mantissa)
        if (expr.isApp()) {
            FuncDecl funcDecl = expr.getFuncDecl();
            if (funcDecl.getDeclKind() == Z3_decl_kind.Z3_OP_DT_UPDATE_FIELD) {
                String accessorName = funcDecl.getName().toString();

                // Convert accessor name to operation type
                OpType opType;
                switch (accessorName) {
                    case "sign":
                        opType = OpType.FP_SIGN;
                        break;
                    case "exponent":
                        opType = OpType.FP_EXPONENT;
                        break;
                    case "mantissa":
                        opType = OpType.FP_MANTISSA;
                        break;
                    case "esign":
                        opType = OpType.EFP_SIGN;
                        break;
                    case "eexponent":
                        opType = OpType.EFP_EXPONENT;
                        break;
                    case "emantissa":
                        opType = OpType.EFP_MANTISSA;
                        break;
                    default:
                        // Generic accessor - use a default or throw exception
                        throw new IllegalArgumentException("Unknown accessor: " + accessorName);
                }

                return new OperationNode(opType, children, null);
            }
        }

        throw new UnsupportedOperationException("Unsupported operation: " + expr);
    }

    static private VarType inferType(Expr expr) {
        if (expr.isBool()) return VarType.BOOLEAN;
        if (expr.isInt()) return VarType.INTEGER;
        if (expr.isBV()) return VarType.BITVECTOR;

        Sort sort = expr.getSort();
        if (sort instanceof DatatypeSort) {
            String sortName = sort.getName().toString();
            if (sortName.equals("FloatingPoint")) return VarType.FLOAT;
            else if (sortName.equals("DoubleFloatingPoint")) return VarType.DOUBLE;
            else if (sortName.equals("ExtendedFloatingPoint")) return VarType.EFLOAT;
            else if (sortName.equals("ExtendedDoubleFloatingPoint")) return VarType.EDOUBLE;
        }

        throw new IllegalArgumentException("Unknown type");
    }
}