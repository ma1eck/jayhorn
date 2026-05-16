package jayhorn.AST.PrincessParser;

import ap.parser.*;
import ap.terfor.conjunctions.Quantifier;
import ap.types.Sort;
import ap.types.Sort$;
import jayhorn.AST.Nodes.*;

import java.math.BigInteger;
import java.util.*;

public class Parser {
    static public InvariantTree convertExpr(IExpression expr) {

        // Literals
        if (expr instanceof IBoolLit) {
            boolean val = ((IBoolLit) expr).value();
            return new LiteralNode(val, VarType.BOOLEAN);
        }
        if (expr instanceof IIntLit) {
            BigInteger valBI = ((IIntLit) expr).value().bigIntValue();

            return LiteralNode.createNumericLiteralNode(valBI);
        }
        // Note: Princess handles BitVectors usually via specific sorts and function apps,
        // or wrappers depending on your API usage.

        // Variables (Constants in Princess)
        if (expr instanceof IConstant) { //TODO: what about double!!
            String name = ((IConstant) expr).c().name();
            VarType type = inferType(expr);
            return new VariableNode(name, type);
        }

        // Bound variables (e.g., inside quantifiers)
        if (expr instanceof ISortedVariable) {
            String name = "v_" + ((ISortedVariable) expr).index();
            VarType type = inferType(expr);
            return new VariableNode(name, type);
        }

        //Operations
        return convertOperation(expr);
    }

    static private InvariantTree convertOperation(IExpression expr) {
        List<InvariantTree> children = new ArrayList<>();

        // Extract children uniformly based on IExpression arity
        for (int i = 0; i < expr.length(); i++) {
            children.add(convertExpr(expr.apply(i)));
        }

        // Logic operations
        if (expr instanceof INot) return new OperationNode(OpType.NOT, children);
        if (expr instanceof IBinFormula) {
            scala.Enumeration.Value junctor = ((IBinFormula) expr).j();
            if (junctor.equals(IBinJunctor.Or())) return new OperationNode(OpType.OR, children);
            if (junctor.equals(IBinJunctor.And())) return new OperationNode(OpType.AND, children);
        }

        // Relational operations
        if (expr instanceof IEquation) {
            return new OperationNode(OpType.EQ, children);
        }

        // Princess normalizes inequalities to (expr >= 0).
        if (expr instanceof IIntFormula) {
            scala.Enumeration.Value rel = ((IIntFormula) expr).rel();

            if (rel.equals(IIntRelation.GeqZero())) {
                LiteralNode zero = LiteralNode.getIntLiteral(0);
                children.add(zero);
                return new OperationNode(OpType.GE, children);
            }
            if (rel.equals(IIntRelation.EqZero())) {
                LiteralNode zero = LiteralNode.getIntLiteral(0);
                children.add(zero);
                return new OperationNode(OpType.EQ, children);
            }
        }

        if (expr instanceof ITimes) {
            ITimes times = (ITimes) expr;
            int coeff = times.coeff().intValue(); // Get the IdealInt coefficient
            InvariantTree coeffNode = new LiteralNode(coeff, VarType.INTEGER);
            InvariantTree subtermNode = convertExpr(times.subterm());
            // Represent as multiplication: coeff * subterm
            return new OperationNode(OpType.MUL, Arrays.asList(coeffNode, subtermNode));
        }

        if (expr instanceof IPlus) {return new OperationNode(OpType.ADD, children);}
        if (expr instanceof ITermITE) {return new OperationNode(OpType.ITE, children);}


        // Function Applications (BitVectors, Datatypes, custom operations)
        if (expr instanceof IFunApp) {
            IFunApp funApp = (IFunApp) expr;
            String funcName = funApp.fun().name();

            // Operations mapped directly from name
            switch (funcName) {
                // Bit-vector operations
                case "bv_add": return new OperationNode(OpType.BVADD, children);
                case "bv_sub": return new OperationNode(OpType.BVSUB, children);
                case "bv_mul": return new OperationNode(OpType.BVMUL, children);
                case "bv_concat": return new OperationNode(OpType.BVCONCAT, children);
                case "bv_ule": return new OperationNode(OpType.BVULE, children);
                case "bv_uge": return new OperationNode(OpType.BVUGE, children);
                case "bv_ult": return new OperationNode(OpType.BVULT, children);
                case "bv_ugt": return new OperationNode(OpType.BVUGT, children);
                case "bv_lshr": return new OperationNode(OpType.BVLSHR, children);
                case "bv_shl": return new OperationNode(OpType.BVSHL, children);
                case "bv_udiv": return new OperationNode(OpType.BVUDIV, children);
                case "bv_neg": return new OperationNode(OpType.BVNEG, children);
                case "bv_extract": return new OperationNode(OpType.BVEXTRACT, children);
                case "zero_extend": return new OperationNode(OpType.ZERO_EXTEND, children);

                // Floating point accessors
                case "sign": return new OperationNode(OpType.FP_SIGN, children);
                case "exponent": return new OperationNode(OpType.FP_EXPONENT, children);
                case "mantissa": return new OperationNode(OpType.FP_MANTISSA, children);
                case "esign": return new OperationNode(OpType.EFP_SIGN, children);
                case "eexponent": return new OperationNode(OpType.EFP_EXPONENT, children);
                case "emantissa": return new OperationNode(OpType.EFP_MANTISSA, children);

                case "FloatingPoint": return new OperationNode(OpType.FLOATING_POINT, children);
                case "DoubleFloatingPoint": return new OperationNode(OpType.DOUBLE_FLOATING_POINT, children);
                case "ExtendedFloatingPoint": return new OperationNode(OpType.EXTENDED_FLOATING_POINT, children);
                case "ExtendedDoubleFloatingPoint": return new OperationNode(OpType.EXTENDED_DOUBLE_FLOATING_POINT, children);

                case "mod_cast": return new OperationNode(OpType.MOD_CAST, children);
                case "int_cast": return new OperationNode(OpType.INT_CAST, children);
            }
            if (funcName.startsWith("extract")) {
                String[] parts = funcName.split("_");
                if (parts.length == 3) {
                    int high = Integer.parseInt(parts[1]);
                    int low = Integer.parseInt(parts[2]);
                    return new OperationNode(OpType.BVEXTRACT, children, Arrays.asList(high, low));
                }
            }

            if (funcName.startsWith("bit2bool")) {
                String[] parts = funcName.split("_");
                if (parts.length >= 2) {
                    int bitIndex = Integer.parseInt(parts[1]);
                    return new OperationNode(OpType.BIT2BOOL, children, Arrays.asList(bitIndex));
                }
            }
        }

        if (expr instanceof ISortedQuantified) {
            ISortedQuantified quantified = (ISortedQuantified) expr;

            // 1. Get the subformula and convert it recursively
            InvariantTree subFormulaNode = convertExpr(quantified.subformula());

            // 2. Identify if it's EXISTS or FORALL
//            boolean isExists = quantified.quan().equals(ap.parser.Quantifier.EX());
            boolean isExists = quantified.quan() instanceof Quantifier.EX$;
            OpType qType = isExists ? OpType.EXISTS : OpType.FORALL;

            // 3. (Optional) If your AST needs the bound variable's type:
            VarType boundVarType = inferType(quantified);

            // Return the AST node (Adjust depending on how JayHorn expects quantifiers.
            // Often it's just an OperationNode, or a specific QuantifiedNode).
            return new OperationNode(qType, Arrays.asList(subFormulaNode));
        }


        throw new UnsupportedOperationException("Unsupported operation: " + expr);
    }

    static private VarType inferType(IExpression expr) {
        if (expr instanceof IFormula) return VarType.BOOLEAN;

        if (expr instanceof ITerm) {
            Sort sort = Sort$.MODULE$.sortOf(((ITerm) expr));

            if (sort.equals(Sort.Integer$.MODULE$)) return VarType.INTEGER;

            // Map custom Princess sorts (if you have defined them)
            String sortName = sort.name();
            if (sortName.contains("BitVec") || sortName.matches("^bv\\[\\d+]$")) return VarType.BITVECTOR;
            if (sortName.equals("FloatingPoint")) return VarType.FLOAT;
            if (sortName.equals("DoubleFloatingPoint")) return VarType.DOUBLE;
            if (sortName.equals("ExtendedFloatingPoint")) return VarType.EFLOAT;
            if (sortName.equals("ExtendedDoubleFloatingPoint")) return VarType.EDOUBLE;
            if (sortName.equals("any")) return VarType.ANY;
        }

        throw new IllegalArgumentException("Unknown type for: " + expr);
    }
}