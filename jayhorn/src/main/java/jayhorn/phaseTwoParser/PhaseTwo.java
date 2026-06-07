package jayhorn.phaseTwoParser;

import jayhorn.AST.Nodes.OpType;
import jayhorn.AST.Nodes.VarType;
import jayhorn.phaseOneParser.LiteralValues.*;
import jayhorn.phaseOneParser.ParentedInvariantTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PhaseTwo {


    private static final List<OpType> logicalOps = Arrays.asList(OpType.AND, OpType.OR);
    public static ParentedInvariantTree parse(ParentedInvariantTree tree){
        initializeBranchStates(tree);
        enforceState(tree, new BoolLiteralValue(true), new ArrayList<>());
        return tree;
    }
    private static void initializeBranchStates(ParentedInvariantTree tree){
        // set a state for each branch in variable nodes

        for (ParentedInvariantTree child: tree.getChildren()) {
            initializeBranchStates(child);
        }

        boolean hasLogicalParent = hasLogicalParent(tree);

        if (hasLogicalParent || logicalOps.contains(tree.getOpType())){ // is a branch of logic ops or is a logic op
            int branchID = tree.getBranchID();
            ArrayList<ParentedInvariantTree> varNodes = tree.getVariableNodes();
            for (ParentedInvariantTree varNode: varNodes) {
                StateValue currentState = varNode.getStateValue();
                varNode.putStateForBranch(branchID, currentState);
            }
        }
    }

    private static boolean hasLogicalParent(ParentedInvariantTree tree) {
        List<ParentedInvariantTree> parents = tree.getParents();

        boolean hasLogicalParent = false;
        for (ParentedInvariantTree parent: parents) {
            if (parent.getNodeType() == ParentedInvariantTree.NodeType.OPERATION
                && logicalOps.contains(parent.getOpType())){
                hasLogicalParent = true;
                break;
            }
        }
        return hasLogicalParent;
    }

    private static void enforceState(ParentedInvariantTree tree, StateValue enforcedState,
                                                      ArrayList<Integer> seenBranches){
        boolean hasLogicalParent  = hasLogicalParent(tree);
        if (hasLogicalParent){
            seenBranches  = (ArrayList<Integer>) seenBranches.clone();
            seenBranches.add(tree.getBranchID());
        }

        if (tree.getNodeType() == ParentedInvariantTree.NodeType.LITERAL){
            // skipping, we can also check if the enforced state is equal to the literal value
//            return tree;
            return;
        }
        if (tree.getNodeType() == ParentedInvariantTree.NodeType.VARIABLE){
            for (int branchID: seenBranches) {
                tree.putStateForBranch(branchID, enforcedState.copy());
            } // todo recheck:
            return;
        }
        if (tree.getNodeType() == ParentedInvariantTree.NodeType.OPERATION) {
            switch (tree.getOpType()) {
                case OR:
                    if (enforcedState instanceof BoolLiteralValue) {
//                      BoolLiteralValue enforcedBool = (BoolLiteralValue) enforcedState;
                        ArrayList<Integer> branchIDs = new ArrayList<>();
                        for (ParentedInvariantTree child : tree.getChildren()) {
                            enforceState(child, enforcedState, seenBranches);
                            branchIDs.add(child.getBranchID());
                        }

                        ArrayList<ParentedInvariantTree> variableNodes = tree.getVariableNodes();
                        for (ParentedInvariantTree varNode: variableNodes) {
                            StateValue currentState = varNode.getStateValue().copy();
                            for (Integer branchID : branchIDs){
                                StateValue stateForBranch = varNode.getStateForBranch(branchID);
                                if (stateForBranch == null){continue;}
                                boolean wasAble =  currentState.union(stateForBranch);
                                if (!wasAble){
                                    System.out.println("no answer here??");
                                }
                            }
                        }

                        // TODO: union states of variables
                    }
                    break;
                case AND:
                    if (enforcedState instanceof BoolLiteralValue) {
//                        BoolLiteralValue enforcedBool = (BoolLiteralValue) enforcedState;
                        for (ParentedInvariantTree child : tree.getChildren()) {
                            enforceState(child, enforcedState, seenBranches);
                        }

                        // TODO: Intersect states of variables
                    }

                    break;
                case NOT:
                    if (enforcedState instanceof BoolLiteralValue) {
                        BoolLiteralValue enforcedBool = (BoolLiteralValue) enforcedState;
                        BoolLiteralValue negatedEnforced = new BoolLiteralValue(enforcedBool.getNegate());
                        for (ParentedInvariantTree child : tree.getChildren()) {
                            enforceState(child, negatedEnforced, seenBranches);
                        }
                    }
                    break;
                case ITE:
                    break;
                case EQ:
                    if (enforcedState instanceof BoolLiteralValue) {
                        BoolLiteralValue enforcedBool = (BoolLiteralValue) enforcedState;
                        boolean enforced = enforcedBool.getValue(); // assuming it's true or false
//                        String enforcedStr = "true";
//                        if (!enforced) enforcedStr = "false";

                        ParentedInvariantTree child1 = tree.getChildren().get(0);
                        ParentedInvariantTree child2 = tree.getChildren().get(1);

                        if (child1.getType() == VarType.BITVECTOR
                                && child2.getType() == VarType.BITVECTOR) {
                            bvBinaryLogicalReversing(tree, enforcedState, seenBranches, "Reverse_BVs_EQ");
                        } else if (child1.getType() == VarType.INTEGER
                                && child2.getType() == VarType.INTEGER) {
                            IntLiteralValue intValue1 = (IntLiteralValue) child1.getStateValue();
                            IntLiteralValue intValue2 = (IntLiteralValue) child2.getStateValue();
                            // assuming each state contains only one interval
                            Integer child1_min = intValue1.getMinValue();
                            Integer child1_max = intValue1.getMaxValue();
                            Integer child2_min = intValue2.getMinValue();
                            Integer child2_max = intValue2.getMaxValue();

//                            List<String> out = PythonBridge.run("Reverse_integers_EQ",
//                                    String.valueOf(child1_min), String.valueOf(child1_max),
//                                    String.valueOf(child2_min), String.valueOf(child2_max),
//                                    String.valueOf(enforcedStr)
//                            );
                            if (child1_min == child1_max){
                                IntLiteralValue enforcedInterval1 = new IntLiteralValue(child1_min, child1_min);
                                IntLiteralValue enforcedInterval2;
                                if (enforced){
                                    enforcedInterval2 = new IntLiteralValue(child1_min, child1_min);
                                }else {
                                    enforcedInterval2 = intValue2.copy();
                                    enforcedInterval2.exclude(child1_min, child1_max);
                                }
                                enforceState(child1, enforcedInterval1, seenBranches);
                                enforceState(child2, enforcedInterval2, seenBranches);
                            }else if (child2_min == child2_max){
                                IntLiteralValue enforcedInterval2 = new IntLiteralValue(child2_min, child2_min);
                                IntLiteralValue enforcedInterval1;
                                if (enforced){
                                    enforcedInterval1 = new IntLiteralValue(child2_min, child2_min);
                                }else {
                                    enforcedInterval1 = intValue1.copy();
                                    enforcedInterval1.exclude(child2_min, child1_max);
                                }
                                enforceState(child1, enforcedInterval1, seenBranches);
                                enforceState(child2, enforcedInterval2, seenBranches);
                            }else {
                                System.out.println("noo");
                            }

//                            IntLiteralValue enforcedInterval1 = new IntLiteralValue(A_min_r, A_max_r);
//                            IntLiteralValue enforcedInterval2 = new IntLiteralValue(B_min_r, B_max_r);

                        }

                    }
                    break;
                case LE:
                    break;
                case LT:
                    break;
                case GE:
                    break;
                case GT:
                    break;
                case MUL:
                    break;
                case ADD:
                    break;
                case BIT2BOOL:
                    if (enforcedState instanceof BoolLiteralValue) {
                        BoolLiteralValue enforcedBool = (BoolLiteralValue) enforcedState;
                        boolean enforced = enforcedBool.getValue(); // assuming it's true or false
                        String enforcedStr = "true";
                        if (!enforced) enforcedStr = "false";
                        ParentedInvariantTree child = tree.getChildren().get(0);
                        List<Integer> params =  tree.getParams();
                        int index = params.get(0); // bit2bool should have its index as a parameter
                        if (child.getType() == VarType.BITVECTOR) {
                            BVLiteralValue bvValue1 = (BVLiteralValue) child.getStateValue();
                            String value1 = bvValue1.getValueStr();
                            String mask1 = bvValue1.getMaskStr();

                            List<String> out = PythonBridge.run("Reverse_bitToBool",
                                    String.valueOf(value1), String.valueOf(mask1),
                                    String.valueOf(index),
                                    String.valueOf(enforcedStr)
                            );
                            String A_v_r = out.get(0);
                            String A_m_r = out.get(1);

                            BVLiteralValue enforcedBV1 = BVLiteralValue.mkBVLiteralValue(A_v_r, A_m_r);
                            enforceState(child, enforcedBV1, seenBranches);
                        }
                    }

                    break;
                case BVADD:
                    if (enforcedState instanceof BVLiteralValue){
                        BVLiteralValue enforcedBV = (BVLiteralValue) enforcedState;
                        String enforcedValue = enforcedBV.getValueStr();
                        String enforcedMask = enforcedBV.getMaskStr();

                        ParentedInvariantTree child1 = tree.getChildren().get(0);
                        ParentedInvariantTree child2 = tree.getChildren().get(1);
                        if (child1.getType() == VarType.BITVECTOR
                                && child2.getType() == VarType.BITVECTOR) {
                            BVLiteralValue bvValue1 = (BVLiteralValue) child1.getStateValue();
                            BVLiteralValue bvValue2 = (BVLiteralValue) child2.getStateValue();
                            String value1 = bvValue1.getValueStr();
                            String value2 = bvValue2.getValueStr();
                            String mask1 = bvValue1.getMaskStr();
                            String mask2 = bvValue2.getMaskStr();

                            List<String> out = PythonBridge.run("Reverse_bvadd",
                                    String.valueOf(value1.length()),
                                    String.valueOf(value1), String.valueOf(mask1),
                                    String.valueOf(value2), String.valueOf(mask2),
                                    String.valueOf(enforcedValue), String.valueOf(enforcedMask)
                            );
                            String A_v_r = out.get(0);
                            String A_m_r = out.get(1);
                            String B_v_r = out.get(2);
                            String B_m_r = out.get(3);

                            BVLiteralValue enforcedBV1 = BVLiteralValue.mkBVLiteralValue(A_v_r, A_m_r);
                            BVLiteralValue enforcedBV2 = BVLiteralValue.mkBVLiteralValue(B_v_r, B_m_r);
                            enforceState(child1, enforcedBV1, seenBranches);
                            enforceState(child2, enforcedBV2, seenBranches);
                        }
                    }
                    break;
                case BVEXTRACT:
                    if (enforcedState instanceof BVLiteralValue){
                        BVLiteralValue enforcedBV = (BVLiteralValue) enforcedState;
                        String enforcedValue = enforcedBV.getValueStr();
                        String enforcedMask = enforcedBV.getMaskStr();

                        List<Integer> params =  tree.getParams();
                        int high = params.get(0), low = params.get(1); // may need to swap

                        ParentedInvariantTree child = tree.getChildren().get(0);
                        if (child.getType() == VarType.BITVECTOR) {
                            BVLiteralValue bvValue1 = (BVLiteralValue) child.getStateValue();
                            String value1 = bvValue1.getValueStr();
                            String mask1 = bvValue1.getMaskStr();
                            List<String> out = PythonBridge.run("Reverse_bitsExtraction",
                                    String.valueOf(value1), String.valueOf(mask1),
                                    String.valueOf(enforcedValue), String.valueOf(enforcedMask),
                                    String.valueOf(high), String.valueOf(low),
                                    String.valueOf(value1.length())
                                    );
                            String A_v_r = out.get(0);
                            String A_m_r = out.get(1);
                            BVLiteralValue enforcedBV1 = BVLiteralValue.mkBVLiteralValue(A_v_r, A_m_r);
                            enforceState(child, enforcedBV1, seenBranches);
                        }
                    }
                    break;
                case BVCONCAT: // assuming it's a binary operation. if not you should clean the tree first
                    // we should find the unknown lengths and determine it
                    if (enforcedState instanceof BVLiteralValue){
                        BVLiteralValue enforcedBV = (BVLiteralValue) enforcedState;
                        String enforcedValue = enforcedBV.getValueStr();
                        String enforcedMask = enforcedBV.getMaskStr();
                        int totalLength = enforcedValue.length();

                        ParentedInvariantTree child1 = tree.getChildren().get(0);
                        ParentedInvariantTree child2 = tree.getChildren().get(1);
                        if (child1.getType() == VarType.BITVECTOR
                                && child2.getType() == VarType.BITVECTOR) {
                            BVLiteralValue bvValue1 = (BVLiteralValue) child1.getStateValue();
                            BVLiteralValue bvValue2 = (BVLiteralValue) child2.getStateValue();
                            String value1 = bvValue1.getValueStr();
                            String value2 = bvValue2.getValueStr();
                            String mask1 = bvValue1.getMaskStr();
                            String mask2 = bvValue2.getMaskStr();
                            int length1 = value1.length();
                            int length2 = value2.length();
                            if (totalLength != length1 + length2){
                                if (bvValue1.isConcrete()) {
                                    length2 = totalLength - length1;
                                }else if (bvValue2.isConcrete()){
                                    length1 = totalLength - length2;
                                }else {
                                    System.out.println("noooooooo");
                                }
                            }

                            List<String> out = PythonBridge.run("Reverse_Concatenation",
                                    String.valueOf(enforcedValue), String.valueOf(enforcedMask),
                                    String.valueOf(length1), String.valueOf(length2)
                            );
                            String A_v_r = out.get(0);
                            String A_m_r = out.get(1);
                            String B_v_r = out.get(2);
                            String B_m_r = out.get(3);

                            BVLiteralValue enforcedBV1 = BVLiteralValue.mkBVLiteralValue(A_v_r, A_m_r);
                            BVLiteralValue enforcedBV2 = BVLiteralValue.mkBVLiteralValue(B_v_r, B_m_r);
                            enforceState(child1, enforcedBV1, seenBranches);
                            enforceState(child2, enforcedBV2, seenBranches);
                        }
                    }
                    break;
                case BVSLE:
                    break;
                case BVULE:
                    bvBinaryLogicalReversing(tree, enforcedState, seenBranches, "Reverse_BVs_ULE");
                    break;
                case BVUGE:
                    break;
                case BVSGE:
                    break;
                case BVULT:
                    break;
                case BVUGT:
                    break;
                case BVNEG:
                    break;
                case BVSUB:
                    break;
                case BVLSHR:
                    break;
                case BVSHL:
                    break;
                case BVUDIV:
                    break;
                case BVMUL:
                    break;
                case ZERO_EXTEND:
                    break;
                case EFP_SIGN:
                case FP_SIGN:
                    if (enforcedState instanceof BoolLiteralValue) {
                        BoolLiteralValue enforcedBool = (BoolLiteralValue) enforcedState;
                        FloatingPointLiteralValue enforcedFP =
                                FloatingPointLiteralValue.createSignOnly(enforcedBool);
                        for (ParentedInvariantTree child : tree.getChildren()) {
                            enforceState(child, enforcedFP, seenBranches);
                        }
                    }
                    break;
                case FP_EXPONENT:
                case EFP_EXPONENT:
                    if (enforcedState instanceof BVLiteralValue) {
                        BVLiteralValue enforcedBV = (BVLiteralValue) enforcedState;
                        FloatingPointLiteralValue enforcedFP =
                                FloatingPointLiteralValue.createExponentOnly(enforcedBV);
                        for (ParentedInvariantTree child : tree.getChildren()) {
                            enforceState(child, enforcedFP, seenBranches);
                        }
                    }
                    break;
                case EFP_MANTISSA:
                case FP_MANTISSA:
                    if (enforcedState instanceof BVLiteralValue) {
                        BVLiteralValue enforcedBV = (BVLiteralValue) enforcedState;
                        FloatingPointLiteralValue enforcedFP =
                                FloatingPointLiteralValue.createMantissaOnly(enforcedBV);
                        for (ParentedInvariantTree child : tree.getChildren()) {
                            enforceState(child, enforcedFP, seenBranches);
                        }
                    }
                    break;
                case MOD_CAST:
                    break;
                case INT_CAST:
                    break;
                case EXISTS:
                    break;
                case FORALL:
                    break;
                case FLOATING_POINT:
                    break;
                case DOUBLE_FLOATING_POINT:
                    break;
                case EXTENDED_FLOATING_POINT:
                    break;
                case EXTENDED_DOUBLE_FLOATING_POINT:
                    break;
            }
        }

    }

    private static void bvBinaryLogicalReversing(ParentedInvariantTree tree, StateValue enforcedState,
                                                 ArrayList<Integer> seenBranches, String pythonFileName) {
        if (enforcedState instanceof BoolLiteralValue) {
            BoolLiteralValue enforcedBool = (BoolLiteralValue) enforcedState;
            boolean enforced = enforcedBool.getValue(); // assuming it's true or false
            String enforcedStr = "true";
            if (!enforced) enforcedStr = "false";

            ParentedInvariantTree child1 = tree.getChildren().get(0);
            ParentedInvariantTree child2 = tree.getChildren().get(1);

            if (child1.getType() == VarType.BITVECTOR
                    && child2.getType() == VarType.BITVECTOR) {
                BVLiteralValue bvValue1 = (BVLiteralValue) child1.getStateValue();
                BVLiteralValue bvValue2 = (BVLiteralValue) child2.getStateValue();
                String value1 = bvValue1.getValueStr();
                String value2 = bvValue2.getValueStr();
                String mask1 = bvValue1.getMaskStr();
                String mask2 = bvValue2.getMaskStr();

                List<String> out = PythonBridge.run(pythonFileName,
                        String.valueOf(value1.length()),
                        String.valueOf(value1), String.valueOf(mask1),
                        String.valueOf(value2), String.valueOf(mask2),
                        String.valueOf(enforcedStr)
                );
                String A_v_r = out.get(0);
                String A_m_r = out.get(1);
                String B_v_r = out.get(2);
                String B_m_r = out.get(3);


                System.out.println(A_v_r + " " + A_m_r +", "+ B_v_r + " " + B_m_r +" "  );
                BVLiteralValue enforcedBV1 = BVLiteralValue.mkBVLiteralValue(A_v_r, A_m_r);
                BVLiteralValue enforcedBV2 = BVLiteralValue.mkBVLiteralValue(B_v_r, B_m_r);
                enforceState(child1, enforcedBV1, seenBranches);
                enforceState(child2, enforcedBV2, seenBranches);
            }
        }
    }


}