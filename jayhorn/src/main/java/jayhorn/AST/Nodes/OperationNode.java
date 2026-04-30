package jayhorn.AST.Nodes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class OperationNode extends InvariantTree {
    private static final long serialVersionUID = 1L;

    private final OpType opType;
    private final List<InvariantTree> children;
    private final List<Integer> params; // For operations like bvExtract(high, low)

    public OperationNode(OpType opType, List<InvariantTree> children) {
        this(opType, children, new ArrayList<>());
    }

    public OperationNode(OpType opType, List<InvariantTree> children, List<Integer> params) {
        this.opType = opType;
        this.children = children;
        this.params = params;
    }

    public OpType getOpType() { return opType; }
    public List<InvariantTree> getChildren() { return children; }
    public List<Integer> getParams() { return params; }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(opType.name());

        if (params != null && !params.isEmpty()) {
            sb.append("[");
            for (int i = 0; i < params.size(); i++) {
                sb.append(params.get(i));
                if (i < params.size() - 1) sb.append(", ");
            }
            sb.append("]");
        }

        sb.append("(");
        for (int i = 0; i < children.size(); i++) {
            sb.append(children.get(i));
            if (i < children.size() - 1) sb.append(", ");
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    protected String toPrettyString(int indent) {
        StringBuilder sb = new StringBuilder();

        sb.append(opType.name());
        sb.append("(");

        // Parameters
        if (params != null && !params.isEmpty()) {
            sb.append("[");
            sb.append(String.join(", ", params.stream()
                    .map(Object::toString)
                    .toArray(String[]::new)));
            sb.append("]");
        }

        // No children
        if (children.isEmpty()) {
            return sb.append("()").toString();
        }

//        // Single child - inline
//        if (children.size() == 1) {
//            return sb.append("(")
//                    .append(children.get(0).toPrettyString(0))
//                    .append(")")
//                    .toString();
//        }

        // Multiple children - multiline
//        sb.append("(\n");
//        for (int i = 0; i < children.size(); i++) {
//            sb.append(getIndent(indent + 1))
//                    .append(children.get(i).toPrettyString(indent + 1));
//            if (i < children.size() - 1) {
//                sb.append(",");
//            }
//            if (i != children.size()-1)
//                sb.append("\n");
//        }

        for (int i=0; i<children.size(); i++) {
            InvariantTree child = children.get(i);
            sb.append("\n")
                    .append(getIndent(indent + 1))
                    .append(child.toPrettyString(indent + 1));
            if (i < children.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("\n").append(getIndent(indent));

        sb.append(getIndent(indent)).append(")");

        return sb.toString();
    }

    public static OperationNode mkOr(InvariantTree ... args){
        ArrayList<InvariantTree> childes = new ArrayList<>(Arrays.asList(args));
        return new OperationNode(OpType.OR, childes);
    }
    public static OperationNode mkAdd(InvariantTree ... args){
        ArrayList<InvariantTree> childes = new ArrayList<>(Arrays.asList(args));
        return new OperationNode(OpType.ADD, childes);
    }
    public static OperationNode mkNot(InvariantTree arg){
        ArrayList<InvariantTree> child = new ArrayList<>(Collections.singletonList(arg));
        return new OperationNode(OpType.NOT, child);
    }
    public static OperationNode mkEq(InvariantTree left, InvariantTree right){
        ArrayList<InvariantTree> childes = new ArrayList<>(Arrays.asList(left, right));
        return new OperationNode(OpType.EQ, childes);
    }
    public static OperationNode mkFPExponent(InvariantTree arg){
        ArrayList<InvariantTree> child = new ArrayList<>(Collections.singletonList(arg));
        return new OperationNode(OpType.FP_EXPONENT, child);
    }
    public static OperationNode mkFPMantissa(InvariantTree arg){
        ArrayList<InvariantTree> child = new ArrayList<>(Collections.singletonList(arg));
        return new OperationNode(OpType.FP_MANTISSA, child);
    }
    public static OperationNode mkBvule(InvariantTree left, InvariantTree right) {
        ArrayList<InvariantTree> childes = new ArrayList<>(Arrays.asList(left, right));
        return new OperationNode(OpType.BVULE, childes);
    }
    public static OperationNode mkExtract(int left, int right, InvariantTree arg){
        ArrayList<InvariantTree> child = new ArrayList<>(Collections.singletonList(arg));
        ArrayList<Integer> params = new ArrayList<>(Arrays.asList(left, right));
        return new OperationNode(OpType.BVEXTRACT, child, params);
    }
    public static OperationNode mkBVAdd(InvariantTree left, InvariantTree right){
        ArrayList<InvariantTree> childes = new ArrayList<>(Arrays.asList(left, right));
        return new OperationNode(OpType.BVADD, childes);
    }
    public static OperationNode mkConcat(InvariantTree ... args){
        ArrayList<InvariantTree> childes = new ArrayList<>(Arrays.asList(args));
        return new OperationNode(OpType.BVCONCAT, childes);
    }

    // --- Logic ---
    public static OperationNode mkAnd(InvariantTree ... args){
        ArrayList<InvariantTree> childes = new ArrayList<>(Arrays.asList(args));
        return new OperationNode(OpType.AND, childes);
    }

    public static OperationNode mkIte(InvariantTree cond, InvariantTree thenTree, InvariantTree elseTree){
        ArrayList<InvariantTree> childes = new ArrayList<>(Arrays.asList(cond, thenTree, elseTree));
        return new OperationNode(OpType.ITE, childes);
    }

    // --- Relational ---
    public static OperationNode mkLe(InvariantTree left, InvariantTree right){
        ArrayList<InvariantTree> childes = new ArrayList<>(Arrays.asList(left, right));
        return new OperationNode(OpType.LE, childes);
    }

    public static OperationNode mkLt(InvariantTree left, InvariantTree right){
        ArrayList<InvariantTree> childes = new ArrayList<>(Arrays.asList(left, right));
        return new OperationNode(OpType.LT, childes);
    }

    public static OperationNode mkGe(InvariantTree left, InvariantTree right){
        ArrayList<InvariantTree> childes = new ArrayList<>(Arrays.asList(left, right));
        return new OperationNode(OpType.GE, childes);
    }

    public static OperationNode mkGt(InvariantTree left, InvariantTree right){
        ArrayList<InvariantTree> childes = new ArrayList<>(Arrays.asList(left, right));
        return new OperationNode(OpType.GT, childes);
    }

    // --- Arithmetic ---
    public static OperationNode mkMul(InvariantTree ... args){
        ArrayList<InvariantTree> childes = new ArrayList<>(Arrays.asList(args));
        return new OperationNode(OpType.MUL, childes);
    }

    // --- Bit-Vector ---
    public static OperationNode mkBit2Bool(InvariantTree arg){
        ArrayList<InvariantTree> child = new ArrayList<>(Collections.singletonList(arg));
        return new OperationNode(OpType.BIT2BOOL, child);
    }

    public static OperationNode mkBvuge(InvariantTree left, InvariantTree right) {
        ArrayList<InvariantTree> childes = new ArrayList<>(Arrays.asList(left, right));
        return new OperationNode(OpType.BVUGE, childes);
    }

    public static OperationNode mkBvult(InvariantTree left, InvariantTree right) {
        ArrayList<InvariantTree> childes = new ArrayList<>(Arrays.asList(left, right));
        return new OperationNode(OpType.BVULT, childes);
    }

    public static OperationNode mkBvugt(InvariantTree left, InvariantTree right) {
        ArrayList<InvariantTree> childes = new ArrayList<>(Arrays.asList(left, right));
        return new OperationNode(OpType.BVUGT, childes);
    }

    public static OperationNode mkBvneg(InvariantTree arg){
        ArrayList<InvariantTree> child = new ArrayList<>(Collections.singletonList(arg));
        return new OperationNode(OpType.BVNEG, child);
    }

    public static OperationNode mkBvsub(InvariantTree left, InvariantTree right) {
        ArrayList<InvariantTree> childes = new ArrayList<>(Arrays.asList(left, right));
        return new OperationNode(OpType.BVSUB, childes);
    }

    public static OperationNode mkBvlshr(InvariantTree left, InvariantTree right) {
        ArrayList<InvariantTree> childes = new ArrayList<>(Arrays.asList(left, right));
        return new OperationNode(OpType.BVLSHR, childes);
    }

    public static OperationNode mkBvshl(InvariantTree left, InvariantTree right) {
        ArrayList<InvariantTree> childes = new ArrayList<>(Arrays.asList(left, right));
        return new OperationNode(OpType.BVSHL, childes);
    }

    public static OperationNode mkBvudiv(InvariantTree left, InvariantTree right) {
        ArrayList<InvariantTree> childes = new ArrayList<>(Arrays.asList(left, right));
        return new OperationNode(OpType.BVUDIV, childes);
    }

    public static OperationNode mkBvmul(InvariantTree left, InvariantTree right) {
        ArrayList<InvariantTree> childes = new ArrayList<>(Arrays.asList(left, right));
        return new OperationNode(OpType.BVMUL, childes);
    }

    public static OperationNode mkZeroExtend(int extension, InvariantTree arg){
        ArrayList<InvariantTree> child = new ArrayList<>(Collections.singletonList(arg));
        ArrayList<Integer> params = new ArrayList<>(Collections.singletonList(extension));
        return new OperationNode(OpType.ZERO_EXTEND, child, params);
    }

    // --- Floating Point ---
    public static OperationNode mkFPSign(InvariantTree arg){
        ArrayList<InvariantTree> child = new ArrayList<>(Collections.singletonList(arg));
        return new OperationNode(OpType.FP_SIGN, child);
    }

    public static OperationNode mkEFPSign(InvariantTree arg){
        ArrayList<InvariantTree> child = new ArrayList<>(Collections.singletonList(arg));
        return new OperationNode(OpType.EFP_SIGN, child);
    }

    public static OperationNode mkEFPExponent(InvariantTree arg){
        ArrayList<InvariantTree> child = new ArrayList<>(Collections.singletonList(arg));
        return new OperationNode(OpType.EFP_EXPONENT, child);
    }

    public static OperationNode mkEFPMantissa(InvariantTree arg){
        ArrayList<InvariantTree> child = new ArrayList<>(Collections.singletonList(arg));
        return new OperationNode(OpType.EFP_MANTISSA, child);
    }

    // --- Casts ---
    public static OperationNode mkModCast(InvariantTree arg){
        ArrayList<InvariantTree> child = new ArrayList<>(Collections.singletonList(arg));
        return new OperationNode(OpType.MOD_CAST, child);
    }

    public static OperationNode mkIntCast(InvariantTree arg){
        ArrayList<InvariantTree> child = new ArrayList<>(Collections.singletonList(arg));
        return new OperationNode(OpType.INT_CAST, child);
    }

    // --- Quantifiers ---
    public static OperationNode mkExists(InvariantTree ... args){
        ArrayList<InvariantTree> childes = new ArrayList<>(Arrays.asList(args));
        return new OperationNode(OpType.EXISTS, childes);
    }

    public static OperationNode mkForall(InvariantTree ... args){
        ArrayList<InvariantTree> childes = new ArrayList<>(Arrays.asList(args));
        return new OperationNode(OpType.FORALL, childes);
    }

    // --- Floating Point Types ---
    public static OperationNode mkFloatingPoint(InvariantTree ... args){
        ArrayList<InvariantTree> childes = new ArrayList<>(Arrays.asList(args));
        return new OperationNode(OpType.FLOATING_POINT, childes);
    }

    public static OperationNode mkDoubleFloatingPoint(InvariantTree ... args){
        ArrayList<InvariantTree> childes = new ArrayList<>(Arrays.asList(args));
        return new OperationNode(OpType.DOUBLE_FLOATING_POINT, childes);
    }

    public static OperationNode mkExtendedFloatingPoint(InvariantTree ... args){
        ArrayList<InvariantTree> childes = new ArrayList<>(Arrays.asList(args));
        return new OperationNode(OpType.EXTENDED_FLOATING_POINT, childes);
    }

    public static OperationNode mkExtendedDoubleFloatingPoint(InvariantTree ... args){
        ArrayList<InvariantTree> childes = new ArrayList<>(Arrays.asList(args));
        return new OperationNode(OpType.EXTENDED_DOUBLE_FLOATING_POINT, childes);
    }

}
