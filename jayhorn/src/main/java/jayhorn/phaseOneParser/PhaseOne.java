package jayhorn.phaseOneParser;

import jayhorn.AST.ASTHelper;
import jayhorn.AST.Nodes.InvariantTree;
import jayhorn.AST.Nodes.VarType;
import jayhorn.AST.Nodes.VariableNode;
import jayhorn.phaseOneParser.LiteralValues.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PhaseOne { // todo: add lots of if for safe casting
    public static ParentedInvariantTree parse(InvariantTree tree){
        ArrayList<VariableNode> variableNodes =  ASTHelper.getVariableNodes(tree);
        HashMap<String, StateValue> varStates = new HashMap<>();
        for (VariableNode var: variableNodes) {
            String name = var.getName();
            VarType type = var.getType();
            if (varStates.containsKey(name)) continue;
            StateValue state = ParentedInvariantTree.getVariableStateByType(type);
            varStates.put(name, state);
        }
        ParentedInvariantTree initialTree  = ASTHelper.toParentedInvariantTree(tree, varStates);
        ParentedInvariantTree afterPhase1  = phase1(initialTree);

        return afterPhase1;
    }

    private static ParentedInvariantTree phase1(ParentedInvariantTree tree){
        if (tree.getNodeType() == ParentedInvariantTree.NodeType.VARIABLE ||
                tree.getNodeType() == ParentedInvariantTree.NodeType.LITERAL){
            return tree;
        }
        switch (tree.getOpType()){
            case OR:
                handleOr(tree);
                break;
            case NOT:
                handleNot(tree);
                break;
            case EQ:
                handleEq(tree);
                break;
            case BVULE:
                handleBVULE(tree);
                break;
            case BVADD:
                handleBVAdd(tree);
                break;
            case BVCONCAT:
                handleConcat(tree);
                break;
            case BVEXTRACT:
                handleExtract(tree);
                break;
            case FP_EXPONENT:
                handleExponent(tree);
                break;
            case FP_MANTISSA:
                handleMantissa(tree);
                break;
            default:
        }
        return tree;

    }

    private static void handleBVULE(ParentedInvariantTree tree) {
        ParentedInvariantTree leftChild = tree.getChildren().get(0);
        ParentedInvariantTree rightChild = tree.getChildren().get(1);
        phase1(leftChild); phase1(rightChild);
        BVLiteralValue leftBLV = ((BVLiteralValue) leftChild.getStateValue());
        BVLiteralValue rightBLV = ((BVLiteralValue) rightChild.getStateValue());
        String leftValue = leftBLV.getValueStr(); String leftMask = leftBLV.getMaskStr();
        String rightValue = rightBLV.getValueStr(); String rightMask = rightBLV.getMaskStr();

        GBool result = bvuleGBitVector(leftValue, leftMask, rightValue, rightMask);
        ((BoolLiteralValue) tree.getStateValue()).setState(result);
    }

    private static void handleMantissa(ParentedInvariantTree tree) {
        ParentedInvariantTree child = tree.getChildren().get(0);
        phase1(child);
        FloatingPointLiteralValue fpv = ((FloatingPointLiteralValue) child.getStateValue());
        BVLiteralValue mantissa = fpv.getMantissa();
        tree.setStateValue(mantissa);
    }

    private static void handleExponent(ParentedInvariantTree tree) {
        ParentedInvariantTree child = tree.getChildren().get(0);
        phase1(child);
        FloatingPointLiteralValue fpv = ((FloatingPointLiteralValue) child.getStateValue());
        BVLiteralValue exponent = fpv.getExponent();
        tree.setStateValue(exponent);
    }

    private static void handleBVAdd(ParentedInvariantTree tree) {
        ParentedInvariantTree leftChild = tree.getChildren().get(0);
        ParentedInvariantTree rightChild = tree.getChildren().get(1);
        phase1(leftChild); phase1(rightChild);
        BVLiteralValue leftBLV = ((BVLiteralValue) leftChild.getStateValue());
        BVLiteralValue rightBLV = ((BVLiteralValue) rightChild.getStateValue());
        String leftValue = leftBLV.getValueStr(); String leftMask = leftBLV.getMaskStr();
        String rightValue = rightBLV.getValueStr(); String rightMask = rightBLV.getMaskStr();

        String[] addResult = addGBitVector(leftValue, leftMask, rightValue, rightMask);
        String resultValue = addResult[0]; String resultMask = addResult[1];
        BVLiteralValue result = BVLiteralValue.mkBVLiteralValue(resultValue, resultMask);
        tree.setStateValue(result);
    }

    private static void handleExtract(ParentedInvariantTree tree) { // assuming that the child only contains a bv type, and indexes are store in parameters
        ParentedInvariantTree child = tree.getChildren().get(0);
        int start = tree.getParams().get(1);
        int end = tree.getParams().get(0); // todo: handel the scenario where these are stored as child
        phase1(child);
        ArrayList<BoolLiteralValue> result ;
        if (child.getType() == VarType.BITVECTOR){
            ArrayList<BoolLiteralValue> childBV = ((BVLiteralValue) child.getStateValue()).state;

            result =  new ArrayList<>(childBV.subList(start, end+1));
            ((BVLiteralValue) tree.getStateValue()).state = result;
        }
    }

    private static void handleConcat(ParentedInvariantTree tree) { // concat(a, b, c) = abc where a has higher indexes
        List<ParentedInvariantTree> children = tree.getChildren();
        ArrayList<BoolLiteralValue> result = new ArrayList<>();
        for (int i = children.size()-1; i >= 0; i--) {
            ParentedInvariantTree child = children.get(i);
            phase1(child);
            if (child.getType() == VarType.BITVECTOR){
                ArrayList<BoolLiteralValue> childBV = ((BVLiteralValue) child.getStateValue()).state;
                result.addAll(childBV);
            }
        }
        ((BVLiteralValue) tree.getStateValue()).state = result;
    }

    private static void handleOr(ParentedInvariantTree tree){
        List<ParentedInvariantTree> children = tree.getChildren();
        boolean allFalse = true;
        boolean isThereTrue = false;
        for (ParentedInvariantTree child: children) {
            phase1(child);
            if (((BoolLiteralValue) child.getStateValue()).state != GBool.FALSE) allFalse = false;
            if (((BoolLiteralValue) child.getStateValue()).state == GBool.TRUE) isThereTrue = true;
        }
        BoolLiteralValue blv = ((BoolLiteralValue) tree.getStateValue());
        if (allFalse) {
            blv.setState(false);
        } else if (isThereTrue){
            blv.setState(true);
        }else {
            blv.setState(GBool.UNKNOWN);
        }

    }
    private static void handleNot(ParentedInvariantTree tree){
        ParentedInvariantTree child = tree.getChildren().get(0);
        phase1(child);
        BoolLiteralValue blv = ((BoolLiteralValue) tree.getStateValue());
        if (((BoolLiteralValue) child.getStateValue()).state == GBool.FALSE){
            blv.setState(true);
        } else if (((BoolLiteralValue) child.getStateValue()).state == GBool.TRUE) {
            blv.setState(false);
        } else {
            blv.setState(GBool.UNKNOWN);
        }
    }
    private static void handleEq(ParentedInvariantTree tree){
        ParentedInvariantTree leftChild = tree.getChildren().get(0);
        ParentedInvariantTree rightChild = tree.getChildren().get(1);
        phase1(leftChild); phase1(rightChild);
        GBool eqResult = GBool.UNKNOWN;
        switch (leftChild.getType()){
            case BOOLEAN:
                eqResult = handleEqBool(leftChild, rightChild);
                break;
            case INTEGER:
                eqResult = handleEqInt(leftChild, rightChild);
                break;
            case BITVECTOR:
                eqResult = handleEqBV(leftChild, rightChild);
                break;
            default:
                break;

        }

        BoolLiteralValue blv = ((BoolLiteralValue) tree.getStateValue());
        blv.setState(eqResult);
    }

    private static GBool handleEqBV(ParentedInvariantTree leftChild, ParentedInvariantTree rightChild) {
        if (rightChild.getType() != VarType.BITVECTOR) return GBool.FALSE;
        ArrayList<BoolLiteralValue> leftBV = ((BVLiteralValue) leftChild.getStateValue()).state;
        ArrayList<BoolLiteralValue> rightBV = ((BVLiteralValue) rightChild.getStateValue()).state;
        int minLength = Math.min(leftBV.size(), rightBV.size());
        GBool result = GBool.TRUE;
        for (int i = 0; i < minLength; i++) {
            GBool lBit = leftBV.get(i).state;
            GBool rBit = rightBV.get(i).state;
            if (lBit == GBool.UNKNOWN || rBit == GBool.UNKNOWN) result = GBool.UNKNOWN;
            else if (lBit != rBit) result = GBool.FALSE;
        }
        return result;
    }

    private static GBool handleEqInt(ParentedInvariantTree leftChild, ParentedInvariantTree rightChild) {
        if (rightChild.getType() != VarType.INTEGER) return GBool.FALSE;
        ValidIntegerRange leftRanges = ((IntLiteralValue) leftChild.getStateValue()).state;
        ValidIntegerRange rightRanges = ((IntLiteralValue) rightChild.getStateValue()).state;
        if (!leftRanges.hasOverlap(rightRanges)) return GBool.FALSE;
        if (! (leftRanges.isSingleValue() & rightRanges.isSingleValue())) return GBool.UNKNOWN;
        Integer lInt = leftRanges.getSingleValue();
        Integer rInt = rightRanges.getSingleValue();
        return (lInt.equals(rInt)) ? GBool.TRUE : GBool.FALSE;
    }

    private static GBool handleEqBool(ParentedInvariantTree leftChild, ParentedInvariantTree rightChild) {
        if (rightChild.getType() != VarType.BOOLEAN) return GBool.FALSE;
        GBool leftBLV = ((BoolLiteralValue) leftChild.getStateValue()).state;
        GBool rightBLV = ((BoolLiteralValue) rightChild.getStateValue()).state;
        if (leftBLV == GBool.UNKNOWN || rightBLV == GBool.UNKNOWN) return GBool.UNKNOWN;
        if (leftBLV == rightBLV) return GBool.TRUE;
        return GBool.FALSE;
    }

    public static String[] addGBitVector(String aValStr, String aMaskStr, String bValStr, String bMaskStr) {
        int bitWidth = aValStr.length();
        BigInteger bitMask = BigInteger.ONE.shiftLeft(bitWidth).subtract(BigInteger.ONE);

        BigInteger aVal = new BigInteger(aValStr, 2);
        BigInteger aMask = new BigInteger(aMaskStr, 2);
        BigInteger bVal = new BigInteger(bValStr, 2);
        BigInteger bMask = new BigInteger(bMaskStr, 2);

        // u: bits that are unknown in A and B are 1.
        BigInteger u = aMask.not().or(bMask.not()).and(bitMask);

        BigInteger sumMin = aVal.and(aMask).add(bVal.and(bMask));
        BigInteger sumMax = sumMin.add(u);

        BigInteger rMask = sumMin.xor(sumMax).not().and(u.not()).and(bitMask);
        BigInteger rVal = sumMin.and(rMask).and(bitMask);

        String rValOut = String.format("%" + bitWidth + "s", rVal.toString(2)).replace(' ', '0');
        String rMaskOut = String.format("%" + bitWidth + "s", rMask.toString(2)).replace(' ', '0');

        return new String[]{rValOut, rMaskOut};
    }

    public static GBool bvuleGBitVector(String aValStr, String aMaskStr, String bValStr, String bMaskStr) {
        int bitWidth = aValStr.length();
        BigInteger bitMask = BigInteger.ONE.shiftLeft(bitWidth).subtract(BigInteger.ONE);

        BigInteger aVal = new BigInteger(aValStr, 2);
        BigInteger aMask = new BigInteger(aMaskStr, 2);
        BigInteger bVal = new BigInteger(bValStr, 2);
        BigInteger bMask = new BigInteger(bMaskStr, 2);

        BigInteger aMin = aVal.and(aMask);
        BigInteger bMin = bVal.and(bMask);

        BigInteger aMax = aMin.or(aMask.not().and(bitMask));
        BigInteger bMax = bMin.or(bMask.not().and(bitMask));

        // Evaluate bounds
        if (aMax.compareTo(bMin) <= 0) {
            // A_max <= B_min
            return GBool.TRUE;
        } else if (aMin.compareTo(bMax) > 0) {
            // A_min > B_max
            return GBool.FALSE;
        } else {
            // Bounds overlap
            return GBool.UNKNOWN;
        }
    }

    public static void main(String[] args) {
        // Example: A = 101000
        String aValStr  = "011000";
        String aMaskStr = "111111";

        // Example: B = ??????
        String bValStr  = "100000";
        String bMaskStr = "110000";

        GBool result = bvuleGBitVector(aValStr, aMaskStr, bValStr, bMaskStr);

        System.out.println(result);
    }




}
