package jayhorn.phaseOneParser;

import jayhorn.AST.ASTHelper;
import jayhorn.AST.Nodes.InvariantTree;
import jayhorn.AST.Nodes.VarType;
import jayhorn.AST.Nodes.VariableNode;
import jayhorn.phaseOneParser.LiteralValues.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.microsoft.z3.*;

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

    public static String[] addGBitVectorDirect(String aValStr, String aMaskStr, String bValStr, String bMaskStr) {
        int bitWidth = aValStr.length();
        BigInteger bitMask = BigInteger.ONE.shiftLeft(bitWidth).subtract(BigInteger.ONE);

        BigInteger aVal = new BigInteger(aValStr, 2);
        BigInteger aMask = new BigInteger(aMaskStr, 2);
        BigInteger bVal = new BigInteger(bValStr, 2);
        BigInteger bMask = new BigInteger(bMaskStr, 2);

        // u: bits that are unknown in A or B are 1.
        BigInteger u = aMask.not().or(bMask.not()).and(bitMask);

        BigInteger sumMin = aVal.and(aMask).add(bVal.and(bMask));
        BigInteger sumMax = sumMin.add(aMask.not()).add(bMask.not());

        // rMask:
        //  sumMin.xor(sumMax).not() = mark bits that are different in sumMin and sumMax as 0
        //  sumMin.xor(sumMax).not().and(u.not()) = b its that are different in min and max, and bits of u are unknown and marked as 0
        BigInteger rMask = sumMin.xor(sumMax).not().and(u.not()).and(bitMask);
        BigInteger rVal = sumMin.and(rMask).and(bitMask);

        String rValOut = String.format("%" + bitWidth + "s", rVal.toString(2)).replace(' ', '0');
        String rMaskOut = String.format("%" + bitWidth + "s", rMask.toString(2)).replace(' ', '0');

        return new String[]{rValOut, rMaskOut};
    }



        public static String[] addGBitVector(String aValStr, String aMaskStr, String bValStr, String bMaskStr) {
            int bitWidth = aValStr.length();

            BigInteger aVal = new BigInteger(aValStr, 2);
            BigInteger aMask = new BigInteger(aMaskStr, 2);
            BigInteger bVal = new BigInteger(bValStr, 2);
            BigInteger bMask = new BigInteger(bMaskStr, 2);

            Context ctx = new Context();
            // USE SOLVER INSTEAD OF OPTIMIZE
            Solver solver = ctx.mkSolver();

            BitVecExpr R_mask = ctx.mkBVConst("mask_R", bitWidth);
            BitVecExpr R_val = ctx.mkBVConst("val_R", bitWidth);
            BitVecExpr a = ctx.mkBVConst("A", bitWidth);
            BitVecExpr b = ctx.mkBVConst("B", bitWidth);

            BitVecExpr aValExpr = ctx.mkBV(aVal.toString(), bitWidth);
            BitVecExpr aMaskExpr = ctx.mkBV(aMask.toString(), bitWidth);
            BitVecExpr bValExpr = ctx.mkBV(bVal.toString(), bitWidth);
            BitVecExpr bMaskExpr = ctx.mkBV(bMask.toString(), bitWidth);
            BitVecExpr zero = ctx.mkBV(0, bitWidth);

            // Force value bits to be 0 where mask is 0
            solver.add(ctx.mkEq(ctx.mkBVAND(R_val, ctx.mkBVNot(R_mask)), zero));

            // Setup premise and conclusion
            BoolExpr aCond = ctx.mkEq(ctx.mkBVAND(a, aMaskExpr), aValExpr);
            BoolExpr bCond = ctx.mkEq(ctx.mkBVAND(b, bMaskExpr), bValExpr);
            BoolExpr premise = ctx.mkAnd(aCond, bCond);

            BitVecExpr sum = ctx.mkBVAdd(a, b);
            BoolExpr conclusion = ctx.mkEq(ctx.mkBVAND(sum, R_mask), R_val);

            // Create ForAll
            BoolExpr implication = ctx.mkImplies(premise, conclusion);
            Expr[] boundVariables = new Expr[]{a, b};
            BoolExpr forAll = ctx.mkForall(boundVariables, implication, 1, new Pattern[0], new Expr[0], null, null);

            solver.add(forAll);

            String bestMask = null;
            String bestVal = null;

            // ITERATIVE MAXIMIZATION
            while (solver.check() == Status.SATISFIABLE) {
                Model m = solver.getModel();

                BitVecNum rMaskRes = (BitVecNum) m.eval(R_mask, false);
                BitVecNum rValRes = (BitVecNum) m.eval(R_val, false);

                bestMask = padLeft(rMaskRes.getBigInteger().toString(2), bitWidth);
                bestVal = padLeft(rValRes.getBigInteger().toString(2), bitWidth);

                // Add constraint to force the next R_mask to be STRICTLY GREATER (unsigned)
                solver.add(ctx.mkBVUGT(R_mask, rMaskRes));
            }

            if (bestMask != null) {
                return new String[]{bestMask, bestVal};
            } else {
                return null;
            }
        }

        private static String padLeft(String s, int length) {
            if (s.length() >= length) return s;
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length - s.length(); i++) sb.append('0');
            sb.append(s);
            return sb.toString();
        }

    public static String[] addGBitVector2(
            String AvalueStr, String AmaskStr,
            String BvalueStr, String BmaskStr) {

        int bitWidth = AmaskStr.length();
        Context ctx = new Context();
        Optimize opt = ctx.mkOptimize();

        BigInteger Avalue = new BigInteger(AvalueStr, 2);
        BigInteger Amask  = new BigInteger(AmaskStr,  2);
        BigInteger Bvalue = new BigInteger(BvalueStr, 2);
        BigInteger Bmask  = new BigInteger(BmaskStr,  2);

        BitVecExpr R_mask = ctx.mkBVConst("mask_R", bitWidth);
        BitVecExpr R_val  = ctx.mkBVConst("val_R",  bitWidth);
        BitVecExpr a      = ctx.mkBVConst("A",      bitWidth);
        BitVecExpr b      = ctx.mkBVConst("B",      bitWidth);

        BitVecExpr AmaskBV  = bigIntegerToBitVec(ctx, Amask,  bitWidth);
        BitVecExpr AvalueBV = bigIntegerToBitVec(ctx, Avalue, bitWidth);
        BitVecExpr BmaskBV  = bigIntegerToBitVec(ctx, Bmask,  bitWidth);
        BitVecExpr BvalueBV = bigIntegerToBitVec(ctx, Bvalue, bitWidth);

        // (R_val & ~R_mask) == 0
        opt.Add(ctx.mkEq(ctx.mkBVAND(R_val, ctx.mkBVNot(R_mask)),
                ctx.mkBV(0, bitWidth)
        ));

        // ForAll a, b: (a & Amask == Avalue) && (b & Bmask == Bvalue) => (a+b) & R_mask == R_val
        BoolExpr premise = ctx.mkAnd(
                ctx.mkEq(ctx.mkBVAND(a, AmaskBV),  AvalueBV),
                ctx.mkEq(ctx.mkBVAND(b, BmaskBV),  BvalueBV)
        );
        BoolExpr conclusion = ctx.mkEq(
                ctx.mkBVAND(ctx.mkBVAdd(a, b), R_mask),
                R_val
        );
        opt.Add(ctx.mkForall(
                new Expr[]{a, b},
                ctx.mkImplies(premise, conclusion),
                1, null, null, null, null
        ));

        opt.MkMaximize(ctx.mkBV2Int(R_mask, false));

        if (opt.Check() == Status.SATISFIABLE) {
            Model m = opt.getModel();
            BigInteger maskVal = ((BitVecNum) m.eval(R_mask, true)).getBigInteger();
            BigInteger rVal    = ((BitVecNum) m.eval(R_val,  true)).getBigInteger();
            return new String[]{
                    String.format("%0" + bitWidth + "d", new BigInteger(maskVal.toString(2))),
                    String.format("%0" + bitWidth + "d", new BigInteger(rVal.toString(2)))
            };
        }
        return new String[]{"UNSAT", "UNSAT"};
    }



    private static BitVecExpr bigIntegerToBitVec(Context ctx, BigInteger value, int bitWidth) {
        if (bitWidth <= 64 && value.bitLength() <= 63) {
            return ctx.mkBV(value.longValue(), bitWidth);
        } else {
            // For larger bit widths, convert to binary string
            String binaryStr = value.toString(2);
            return ctx.mkBV(binaryStr, bitWidth);
        }
    }

    private static String padLeft2(String s, int length) {
        return String.format("%" + length + "s", s).replace(' ', '0');
    }


    public static String[] addGBitVector3(String AvalueStr, String AmaskStr,
                                               String BvalueStr, String BmaskStr) throws Z3Exception {

        int bitWidth = AvalueStr.length();

        // Validate input length equality
        if (AmaskStr.length() != bitWidth ||
                BvalueStr.length() != bitWidth ||
                BmaskStr.length() != bitWidth) {
            throw new IllegalArgumentException("All inputs must have the same length.");
        }

        // Parse inputs as BigInteger
        java.math.BigInteger Avalue = new java.math.BigInteger(AvalueStr, 2);
        java.math.BigInteger Amask  = new java.math.BigInteger(AmaskStr,  2);
        java.math.BigInteger Bvalue = new java.math.BigInteger(BvalueStr, 2);
        java.math.BigInteger Bmask  = new java.math.BigInteger(BmaskStr,  2);

        // Create Z3 context (using default config)
        Context ctx = new Context();

        try {
            Optimize opt = ctx.mkOptimize();

            // BitVec sorts and constants
            BitVecExpr R_mask = ctx.mkBVConst("R_mask", bitWidth);
            BitVecExpr R_val  = ctx.mkBVConst("R_val",  bitWidth);

            BitVecExpr a = ctx.mkBVConst("a", bitWidth);
            BitVecExpr b = ctx.mkBVConst("b", bitWidth);

            // Convert input constants to BitVecExpr
            BitVecExpr AvalueBV = ctx.mkBV(AvalueStr, bitWidth);
            BitVecExpr AmaskBV  = ctx.mkBV(AmaskStr, bitWidth);
            BitVecExpr BvalueBV = ctx.mkBV(BvalueStr, bitWidth);
            BitVecExpr BmaskBV  = ctx.mkBV(BmaskStr, bitWidth);

            // Constraint: (R_val & ~R_mask) == 0
            BitVecExpr negRmask = ctx.mkBVNot(R_mask);
            BoolExpr maskValZero = ctx.mkEq(ctx.mkBVAND(R_val, negRmask), ctx.mkBV(0, bitWidth));
            opt.Add(maskValZero);

            // ForAll (a, b):
            // if (a & Amask) == Avalue AND (b & Bmask) == Bvalue
            // then ((a + b) & R_mask) == R_val

            BoolExpr inputMatch = ctx.mkAnd(
                    ctx.mkEq(ctx.mkBVAND(a, AmaskBV), AvalueBV),
                    ctx.mkEq(ctx.mkBVAND(b, BmaskBV), BvalueBV)
            );

            BoolExpr resultMatch = ctx.mkEq(
                    ctx.mkBVAND(ctx.mkBVAdd(a, b), R_mask),
                    R_val);

            BoolExpr implication = ctx.mkImplies(inputMatch, resultMatch);

            Quantifier forall = ctx.mkForall(
                    new Expr[]{a, b},
                    implication,
                    1,
                    null,
                    null,
                    null,
                    null);

            opt.Add(forall);

            // Maximize known bits in R_mask
            ArithExpr R_mask_as_int = ctx.mkBV2Int(R_mask, false);  // false = unsigned
            opt.MkMaximize(R_mask_as_int);

            if (opt.Check() != Status.SATISFIABLE) {
                throw new IllegalStateException("Constraints are unsatisfiable.");
            }

            Model model = opt.getModel();

            BitVecNum val = (BitVecNum) model.evaluate(R_val, false);
            BitVecNum mask = (BitVecNum) model.evaluate(R_mask, false);

            // Format with leading zeros up to bitWidth
            String resultValueBin = val.getBigInteger().toString(2);
            String resultMaskBin = mask.getBigInteger().toString(2);

            // Pad with leading zeros if needed
            resultValueBin = String.format("%" + bitWidth + "s", resultValueBin).replace(' ', '0');
            resultMaskBin = String.format("%" + bitWidth + "s", resultMaskBin).replace(' ', '0');

            return new String[]{resultValueBin, resultMaskBin};
        }catch (Exception e){
            System.out.printf(e.toString());
            return null;
        }
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
//        // A = 010??00???101
//
//        // Example: A = 101000
//        String aValStr  = "0100000000101";
//        String aMaskStr = "1110011000111";
//
//        // Example: B = 01??????011???
//        String bValStr  = "01??????011???";
//        String bMaskStr = "11000000111000";

//        String aValStr  = "0000000000101";
//        String aMaskStr = "1011101111111";
//
//        String bValStr  = "0000000001100";
//        String bMaskStr = "1011111111111";
//
//        String[] result = addGBitVector(aValStr, aMaskStr, bValStr, bMaskStr);
//
//        System.out.println(Arrays.toString(result));
        testAadGBitVector();
    }


    public static void testAadGBitVector() {
        int passed = 0;
        int total = 10;

        System.out.println("Running " + total + " test cases...\n");

        passed += runTest(1, "0101", "1111", "0011", "1111", "1111", "1000");

        passed += runTest(2, "0000", "0000", "0000", "0000", "0000", "0000");

        passed += runTest(3, "1010", "1111", "0000", "0000", "0000", "0000");

        passed += runTest(4, "0101", "0111", "0010", "1111", "0111", "0111");

        passed += runTest(5, "0000", "0001", "1111", "1111", "0001", "0001");

        passed += runTest(6, "1000", "1101", "0000", "1111", "1101", "1000");

        passed += runTest(7, "1111", "1111", "0001", "1111", "1111", "0000");

        passed += runTest(8, "0010", "0011", "0001", "0011", "0011", "0011");

        passed += runTest(9, "0111", "0111", "0001", "0111", "0111", "0000");

        passed += runTest(10, "00101010", "11111111", "00010000", "11110000", "10000000", "00000000");

        System.out.println("========================================");
        System.out.println("Tests Passed: " + passed + " / " + total);
    }

    private static int runTest(int testNum, String aVal, String aMask, String bVal, String bMask, String expMask, String expVal) {
        try {
            String[] result = PhaseOne.addGBitVector3(aVal, aMask, bVal, bMask);

            if (result == null) {
                System.err.println("Test " + testNum + " FAILED: Returned null");
                return 0;
            }

            String actMask = result[0];
            String actVal = result[1];

            if (expMask.equals(actMask) && expVal.equals(actVal)) {
                System.out.println("Test " + testNum + " PASSED.");
                return 1;
            } else {
                System.err.println("Test " + testNum + " FAILED!");
                System.err.println("  Expected: Mask=" + expMask + ", Val=" + expVal);
                System.err.println("  Actual:   Mask=" + actMask + ", Val=" + actVal);
                return 0;
            }
        } catch (Exception e) {
            System.err.println("Test " + testNum + " FAILED with Exception: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }



}
