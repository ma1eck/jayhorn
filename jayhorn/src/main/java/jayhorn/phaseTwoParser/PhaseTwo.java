package jayhorn.phaseTwoParser;

import jayhorn.AST.Nodes.OpType;
import jayhorn.AST.Nodes.VarType;
import jayhorn.Log;
import jayhorn.phaseOneParser.LiteralValues.*;
import jayhorn.phaseOneParser.ParentedInvariantTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PhaseTwo {


    private static final List<OpType> logicalOps = Arrays.asList(OpType.AND, OpType.OR);
    public static ParentedInvariantTree parse(ParentedInvariantTree tree){
        Log.info("Starting the phase two, backward");
        initializeBranchStates(tree);
        ArrayList<StateValue> enforceStates = new ArrayList<>();
        enforceStates.add(new BoolLiteralValue(true));
        enforceState(tree, enforceStates, new ArrayList<>());
        return tree;
    }
    private static void initializeBranchStates(ParentedInvariantTree tree){
        // set a state for each branch in variable nodes

        for (ParentedInvariantTree child: tree.getChildren()) {
            initializeBranchStates(child);
        }

        boolean hasLogicalParent = tree.hasLogicalParent();

        if (hasLogicalParent
//                || logicalOps.contains(tree.getOpType())
        ){ // is a branch of logic ops or is a logic op
            int branchID = tree.getBranchID();
            ArrayList<ParentedInvariantTree> varNodes = tree.getVariableNodes();
            for (ParentedInvariantTree varNode: varNodes) {
                StateValue currentState = varNode.getStateValue();
                varNode.putStateForBranch(branchID, currentState);
            }
        }
    }


    private static void enforceState(ParentedInvariantTree tree, StateValue enforcedState,
                                                      ArrayList<Integer> seenBranches)
    {

        boolean hasLogicalParent  = tree.hasLogicalParent();
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
            Log.info("Starting to enforce " + enforcedState.toString() + " to " + tree.getOpType() + "operation.");
            switch (tree.getOpType()) {
                case OR:
                    handleOR(tree, enforcedState, seenBranches);
                    break;
                case AND:
                    handleAND(tree, enforcedState, seenBranches);
                    break;
                case NOT:
                    handleNOT(tree, enforcedState, seenBranches);
                    break;
                case ITE:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case EQ:
                    handleEQ(tree, enforcedState, seenBranches);
                    break;
                case LE:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case LT:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case GE:
                    handleGE(tree, enforcedState, seenBranches);
                    break;
                case GT:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case MUL:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case ADD:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case BIT2BOOL:
                    handleBIT2BOOL(tree, enforcedState, seenBranches);
                    break;
                case BVADD:
                    handleBVADD(tree, enforcedState, seenBranches);
                    break;
                case BVEXTRACT:
                    handleBVEXTRACT(tree, enforcedState, seenBranches);
                    break;
                case BVCONCAT: // assuming it's a binary operation. if not you should clean the tree first
                    // we should find the unknown lengths and determine it
                    handleBVCONCAT(tree, enforcedState, seenBranches);
                    break;
                case BVSLE:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case BVULE:
                    handleBVULE(tree, enforcedState, seenBranches);
                    break;
                case BVUGE:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case BVSGE:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case BVULT:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case BVUGT:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case BVNEG:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case BVSUB:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case BVLSHR:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case BVSHL:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case BVUDIV:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case BVMUL:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case ZERO_EXTEND:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case EFP_SIGN:
                case FP_SIGN:
                    handleFP_SIGN(tree, enforcedState, seenBranches);
                    break;
                case FP_EXPONENT:
                case EFP_EXPONENT:
                    handleEXPONENT(tree, enforcedState, seenBranches);
                    break;
                case EFP_MANTISSA:
                case FP_MANTISSA:
                    handleMANTISSA(tree, enforcedState, seenBranches);
                    break;
                case MOD_CAST:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case INT_CAST:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case EXISTS:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case FORALL:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case FLOATING_POINT:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case DOUBLE_FLOATING_POINT:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case EXTENDED_FLOATING_POINT:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case EXTENDED_DOUBLE_FLOATING_POINT:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
            }
        }


    }

    private static void logUnsupportedOperationMessage(OpType opType) {
        Log.error("Phase2. the "+ opType.toString() +"operation is not supported yet.");
    }

    private static void handleMANTISSA(ParentedInvariantTree tree, StateValue enforcedState, ArrayList<Integer> seenBranches) {
        if (enforcedState instanceof BVLiteralValue) {
            BVLiteralValue enforcedBV = (BVLiteralValue) enforcedState;
            FloatingPointLiteralValue enforcedFP =
                    FloatingPointLiteralValue.createMantissaOnly(enforcedBV);
            for (ParentedInvariantTree child : tree.getChildren()) {
                enforceState(child, enforcedFP, seenBranches);
            }
        }
    }
    private static void handleMANTISSA(ParentedInvariantTree tree, ArrayList<StateValue> enforcedStates, ArrayList<Integer> seenBranches) {
        ArrayList<StateValue> newEnforce = new ArrayList<>();
        for (StateValue enforcedState: enforcedStates){
            if (enforcedState instanceof BVLiteralValue) {
                BVLiteralValue enforcedBV = (BVLiteralValue) enforcedState;
                FloatingPointLiteralValue enforcedFP =
                        FloatingPointLiteralValue.createMantissaOnly(enforcedBV);
                newEnforce.add(enforcedFP);
            }
        }
        for (ParentedInvariantTree child : tree.getChildren()) {
            enforceState(child, newEnforce, seenBranches);
        }
    }

    private static void handleEXPONENT(ParentedInvariantTree tree, StateValue enforcedState, ArrayList<Integer> seenBranches) {
        if (enforcedState instanceof BVLiteralValue) {
            BVLiteralValue enforcedBV = (BVLiteralValue) enforcedState;
            FloatingPointLiteralValue enforcedFP =
                    FloatingPointLiteralValue.createExponentOnly(enforcedBV);
            for (ParentedInvariantTree child : tree.getChildren()) {
                enforceState(child, enforcedFP, seenBranches);
            }
        }
    }
    private static void handleEXPONENT(ParentedInvariantTree tree, ArrayList<StateValue> enforcedStates, ArrayList<Integer> seenBranches) {
        ArrayList<StateValue> newEnforce = new ArrayList<>();

        for (StateValue enforcedState: enforcedStates){
            if (enforcedState instanceof BVLiteralValue) {
                BVLiteralValue enforcedBV = (BVLiteralValue) enforcedState;
                FloatingPointLiteralValue enforcedFP =
                        FloatingPointLiteralValue.createExponentOnly(enforcedBV);
                newEnforce.add(enforcedFP);
            }
        }
        for (ParentedInvariantTree child : tree.getChildren()) {
            enforceState(child, newEnforce, seenBranches);
        }
    }

    private static void handleFP_SIGN(ParentedInvariantTree tree, StateValue enforcedState, ArrayList<Integer> seenBranches) {
        if (enforcedState instanceof BoolLiteralValue) {
            BoolLiteralValue enforcedBool = (BoolLiteralValue) enforcedState;
            FloatingPointLiteralValue enforcedFP =
                    FloatingPointLiteralValue.createSignOnly(enforcedBool);
            for (ParentedInvariantTree child : tree.getChildren()) {
                enforceState(child, enforcedFP, seenBranches);
            }
        }
    }
    private static void handleFP_SIGN(ParentedInvariantTree tree, ArrayList<StateValue> enforcedStates, ArrayList<Integer> seenBranches) {
        ArrayList<StateValue> newEnforce = new ArrayList<>();
        for (StateValue enforcedState: enforcedStates){
            if (enforcedState instanceof BoolLiteralValue) {
                BoolLiteralValue enforcedBool = (BoolLiteralValue) enforcedState;
                FloatingPointLiteralValue enforcedFP =
                        FloatingPointLiteralValue.createSignOnly(enforcedBool);
                newEnforce.add(enforcedFP);
            }
        }

        for (ParentedInvariantTree child : tree.getChildren()) {
            enforceState(child, newEnforce, seenBranches);
        }
    }

    private static void handleBVULE(ParentedInvariantTree tree, StateValue enforcedState, ArrayList<Integer> seenBranches) {
        bvBinaryLogicalReversing(tree, enforcedState, seenBranches, "Reverse_BVs_ULE_v2"); // this is old version
    }

    private static void handleBVULE(ParentedInvariantTree tree, ArrayList<StateValue> enforcedStates, ArrayList<Integer> seenBranches) {
        ArrayList<StateValue> newEnforceStates1 = new ArrayList<>();
        ArrayList<StateValue> newEnforceStates2 = new ArrayList<>();
        ParentedInvariantTree child1 = tree.getChildren().get(0);
        ParentedInvariantTree child2 = tree.getChildren().get(1);

        if (child1.getType() == VarType.BITVECTOR
                && child2.getType() == VarType.BITVECTOR) {
            for (StateValue enforcedState: enforcedStates){
                List<StateValue>[] result = getBVULEReversingResults(tree, enforcedState);
//                List<StateValue> result = getBVBinaryLogicalReversingResults(tree, enforcedState, seenBranches, "Reverse_BVs_ULE");
                newEnforceStates1.addAll(result[0]);
                newEnforceStates2.addAll(result[1]);
            }
        }
        enforceState(child1, newEnforceStates1, seenBranches);
        enforceState(child2, newEnforceStates2, seenBranches);
    }

    private static void handleBVCONCAT(ParentedInvariantTree tree, StateValue enforcedState, ArrayList<Integer> seenBranches) {
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
    }
    private static void handleBVCONCAT(ParentedInvariantTree tree, ArrayList<StateValue> enforcedStates, ArrayList<Integer> seenBranches) {
        ArrayList<StateValue> newEnforce1 = new ArrayList<>();
        ArrayList<StateValue> newEnforce2 = new ArrayList<>();

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

            for (StateValue enforcedState: enforcedStates){
                if (enforcedState instanceof BVLiteralValue) {
                    BVLiteralValue enforcedBV = (BVLiteralValue) enforcedState;
                    String enforcedValue = enforcedBV.getValueStr();
                    String enforcedMask = enforcedBV.getMaskStr();
                    int totalLength = enforcedValue.length();
                    int length1 = value1.length();
                    int length2 = value2.length();
                    if (totalLength != length1 + length2) {
                        if (bvValue1.isConcrete()) {
                            length2 = totalLength - length1;
                        } else if (bvValue2.isConcrete()) {
                            length1 = totalLength - length2;
                        } else {
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
                    newEnforce1.add(enforcedBV1);
                    newEnforce2.add(enforcedBV2);
                }
            }
            enforceState(child1, newEnforce1, seenBranches);
            enforceState(child2, newEnforce2, seenBranches);
        }
    }

    private static void handleBVEXTRACT(ParentedInvariantTree tree, StateValue enforcedState, ArrayList<Integer> seenBranches) {
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
    }

    private static void handleBVEXTRACT(ParentedInvariantTree tree, ArrayList<StateValue> enforcedStates, ArrayList<Integer> seenBranches) {
        ArrayList<StateValue> newEnforce = new ArrayList<>();
        List<Integer> params =  tree.getParams();
        int high = params.get(0), low = params.get(1); // may need to swap
        ParentedInvariantTree child = tree.getChildren().get(0);

        if (child.getType() == VarType.BITVECTOR) {
            BVLiteralValue bvValue1 = (BVLiteralValue) child.getStateValue();
            String value1 = bvValue1.getValueStr();
            String mask1 = bvValue1.getMaskStr();

            for (StateValue enforcedState: enforcedStates){
                if (enforcedState instanceof BVLiteralValue){
                    BVLiteralValue enforcedBV = (BVLiteralValue) enforcedState;
                    String enforcedValue = enforcedBV.getValueStr();
                    String enforcedMask = enforcedBV.getMaskStr();

                    List<String> out = PythonBridge.run("Reverse_bitsExtraction",
                            String.valueOf(value1), String.valueOf(mask1),
                            String.valueOf(enforcedValue), String.valueOf(enforcedMask),
                            String.valueOf(high), String.valueOf(low),
                            String.valueOf(value1.length())
                            );
                    String A_v_r = out.get(0);
                    String A_m_r = out.get(1);
                    BVLiteralValue enforcedBV1 = BVLiteralValue.mkBVLiteralValue(A_v_r, A_m_r);
                    newEnforce.add(enforcedBV1);
                }
            }
            enforceState(child, newEnforce, seenBranches);
        }
    }

    private static void handleBVADD(ParentedInvariantTree tree, StateValue enforcedState, ArrayList<Integer> seenBranches) {
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
    }

    private static void handleBVADD(ParentedInvariantTree tree, ArrayList<StateValue> enforcedStates, ArrayList<Integer> seenBranches) {
        ArrayList<StateValue> newEnforce1 = new ArrayList<>();
        ArrayList<StateValue> newEnforce2 = new ArrayList<>();
        
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
        
        
            for (StateValue enforcedState: enforcedStates){
                if (enforcedState instanceof BVLiteralValue) {
                    BVLiteralValue enforcedBV = (BVLiteralValue) enforcedState;
                    String enforcedValue = enforcedBV.getValueStr();
                    String enforcedMask = enforcedBV.getMaskStr();
    
                        List<String> out = PythonBridge.run("Reverse_bvadd_v2",
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
                        newEnforce1.add(enforcedBV1);
                        newEnforce2.add(enforcedBV2);
                }
            }
            enforceState(child1, newEnforce1, seenBranches);
            enforceState(child2, newEnforce2, seenBranches);
        }

    }

    private static void handleADD(ParentedInvariantTree tree, ArrayList<StateValue> enforcedStates, ArrayList<Integer> seenBranches) {
        ArrayList<StateValue> newEnforce1 = new ArrayList<>();
        ArrayList<StateValue> newEnforce2 = new ArrayList<>();

        ParentedInvariantTree child1 = tree.getChildren().get(0);
        ParentedInvariantTree child2 = tree.getChildren().get(1);
        if (child1.getType() == VarType.INTEGER
                && child2.getType() == VarType.INTEGER) {
            IntLiteralValue val1 = (IntLiteralValue) child1.getStateValue();
            IntLiteralValue val2 = (IntLiteralValue) child2.getStateValue();

            Integer min1 = val1.getMinValue();
            Integer max1 = val1.getMaxValue();
            Integer min2 = val2.getMinValue();
            Integer max2 = val2.getMaxValue();


            for (StateValue enforcedState: enforcedStates){
                if (enforcedState instanceof IntLiteralValue) {
                    IntLiteralValue enforcedInt = (IntLiteralValue) enforcedState;
                    Integer minEnforce = enforcedInt.getMinValue();
                    Integer maxEnforce = enforcedInt.getMaxValue();


                    Integer enforced1_Min = minEnforce - max2;
                    Integer enforced1_Max = maxEnforce - min2;
                    Integer enforced2_Min = minEnforce - max1;
                    Integer enforced2_Max = maxEnforce - min1;

                    newEnforce1.add(new IntLiteralValue(enforced1_Min, enforced1_Max));
                    newEnforce2.add(new IntLiteralValue(enforced2_Min, enforced2_Max));
                }
            }
            enforceState(child1, newEnforce1, seenBranches);
            enforceState(child2, newEnforce2, seenBranches);
        }

    }

    private static void handleMUL(ParentedInvariantTree tree, ArrayList<StateValue> enforcedStates, ArrayList<Integer> seenBranches) {
        ArrayList<StateValue> newEnforce1 = new ArrayList<>();
        ArrayList<StateValue> newEnforce2 = new ArrayList<>();

        ParentedInvariantTree child1 = tree.getChildren().get(0);
        ParentedInvariantTree child2 = tree.getChildren().get(1);

        if (child1.getType() == VarType.INTEGER && child2.getType() == VarType.INTEGER) {
            IntLiteralValue val1 = (IntLiteralValue) child1.getStateValue();
            IntLiteralValue val2 = (IntLiteralValue) child2.getStateValue();

            Integer min1 = val1.getMinValue();
            Integer max1 = val1.getMaxValue();
            Integer min2 = val2.getMinValue();
            Integer max2 = val2.getMaxValue();

            for (StateValue enforcedState : enforcedStates) {
                if (enforcedState instanceof IntLiteralValue) {
                    IntLiteralValue enforcedInt = (IntLiteralValue) enforcedState;
                    Integer minE = enforcedInt.getMinValue();
                    Integer maxE = enforcedInt.getMaxValue();

                    // --- Constrain child1: child1 = enforcedRange / child2's range ---
                    // If child2's range spans zero, child1 is unconstrained (skip)
                    if (!(min2 <= 0 && max2 >= 0)) {
                        // Divisor range doesn't include zero: safe to divide all combinations
                        List<Integer> candidates1 = Arrays.asList(
                                divFloor(minE, min2), divFloor(minE, max2),
                                divCeil(maxE, min2),  divCeil(maxE, max2)
                        );
                        Integer new1Min = Collections.min(candidates1);
                        Integer new1Max = Collections.max(candidates1);
                        newEnforce1.add(new IntLiteralValue(new1Min, new1Max));
                    } else {
                        // child2 can be zero: child1 is unconstrained, propagate existing bounds
                        newEnforce1.add(new IntLiteralValue(min1, max1));
                    }

                    // --- Constrain child2: child2 = enforcedRange / child1's range ---
                    if (!(min1 <= 0 && max1 >= 0)) {
                        List<Integer> candidates2 = Arrays.asList(
                                divFloor(minE, min1), divFloor(minE, max1),
                                divCeil(maxE, min1),  divCeil(maxE, max1)
                        );
                        Integer new2Min = Collections.min(candidates2);
                        Integer new2Max = Collections.max(candidates2);
                        newEnforce2.add(new IntLiteralValue(new2Min, new2Max));
                    } else {
                        newEnforce2.add(new IntLiteralValue(min2, max2));
                    }
                }
            }

            enforceState(child1, newEnforce1, seenBranches);
            enforceState(child2, newEnforce2, seenBranches);
        }
    }

    // Floor division (rounds toward negative infinity, unlike Java's truncation toward zero)
    private static int divFloor(int a, int b) {
        return Math.floorDiv(a, b);
    }

    private static int divCeil(int a, int b) {
        return Math.floorDiv(a, b) + (a % b != 0 ? 1 : 0);
    }

    private static void handleBIT2BOOL(ParentedInvariantTree tree, StateValue enforcedState, ArrayList<Integer> seenBranches) {
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
                        String.valueOf(value1.length()),
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
    }

    private static void handleBIT2BOOL(ParentedInvariantTree tree, ArrayList<StateValue> enforcedStates, ArrayList<Integer> seenBranches) {
        ArrayList<StateValue> newEnforce = new ArrayList<>();
        ParentedInvariantTree child = tree.getChildren().get(0);
        List<Integer> params = tree.getParams();
        int index = params.get(0); // bit2bool should have its index as a parameter

        for (StateValue enforcedState: enforcedStates){
            if (enforcedState instanceof BoolLiteralValue) {
                BoolLiteralValue enforcedBool = (BoolLiteralValue) enforcedState;
                boolean enforced = enforcedBool.getValue(); // assuming it's true or false
                String enforcedStr = "true";
                if (!enforced) enforcedStr = "false";
                if (child.getType() == VarType.BITVECTOR) {
                    BVLiteralValue bvValue1 = (BVLiteralValue) child.getStateValue();
                    String value1 = bvValue1.getValueStr();
                    String mask1 = bvValue1.getMaskStr();

                    List<String> out = PythonBridge.run("Reverse_bitToBool",
                            String.valueOf(value1.length()),
                            String.valueOf(value1), String.valueOf(mask1),
                            String.valueOf(index),
                            String.valueOf(enforcedStr)
                    );
                    String A_v_r = out.get(0);
                    String A_m_r = out.get(1);

                    BVLiteralValue enforcedBV1 = BVLiteralValue.mkBVLiteralValue(A_v_r, A_m_r);
                    newEnforce.add(enforcedBV1);
                }
            }
        }
        enforceState(child, newEnforce, seenBranches);

    }

    private static void handleGE(ParentedInvariantTree tree, StateValue enforcedState, ArrayList<Integer> seenBranches) {
        if (enforcedState instanceof BoolLiteralValue) {
            BoolLiteralValue enforcedBool = (BoolLiteralValue) enforcedState;
            boolean enforced = enforcedBool.getValue(); // assuming it's true or false
//                        String enforcedStr = "true";
//                        if (!enforced) enforcedStr = "false";

            ParentedInvariantTree child1 = tree.getChildren().get(0);
            ParentedInvariantTree child2 = tree.getChildren().get(1);

            if (child1.getType() == VarType.INTEGER
                    && child2.getType() == VarType.INTEGER) {
                IntLiteralValue intValue1 = (IntLiteralValue) child1.getStateValue();
                IntLiteralValue intValue2 = (IntLiteralValue) child2.getStateValue();

                Integer child1_min = intValue1.getMinValue();
                Integer child1_max = intValue1.getMaxValue();
                Integer child2_min = intValue2.getMinValue();
                Integer child2_max = intValue2.getMaxValue();

                if (child1_min == child1_max) {
                    IntLiteralValue enforcedInterval1 = new IntLiteralValue(child1_min, child1_min);
                    IntLiteralValue enforcedInterval2 = intValue2.copy();
                    if (enforced) {
                        // child1 >= child2  =>  child2 <= child1_min
                        enforcedInterval2.intersect(Integer.MIN_VALUE, child1_min);
                    } else {
                        // child1 < child2  =>  child2 > child1_min  =>  child2 >= child1_min + 1
                        enforcedInterval2.intersect(child1_min + 1, Integer.MAX_VALUE);
                    }
                    enforceState(child1, enforcedInterval1, seenBranches);
                    enforceState(child2, enforcedInterval2, seenBranches);
                } else if (child2_min == child2_max) {
                    IntLiteralValue enforcedInterval2 = new IntLiteralValue(child2_min, child2_min);
                    IntLiteralValue enforcedInterval1 = intValue1.copy();
                    if (enforced) {
                        // child1 >= child2_min
                        enforcedInterval1.intersect(child2_min, Integer.MAX_VALUE);
                    } else {
                        // child1 < child2_min  =>  child1 <= child2_min - 1
                        enforcedInterval1.intersect(Integer.MIN_VALUE, child2_min - 1);
                    }
                    enforceState(child1, enforcedInterval1, seenBranches);
                    enforceState(child2, enforcedInterval2, seenBranches);
                }else {
                    System.out.println("noo");
                }
            }
        }
    }

    private static void handleGE(ParentedInvariantTree tree, ArrayList<StateValue> enforcedStates, ArrayList<Integer> seenBranches) {
        ArrayList<StateValue> newEnforce1 = new ArrayList<>();
        ArrayList<StateValue> newEnforce2 = new ArrayList<>();
        ParentedInvariantTree child1 = tree.getChildren().get(0);
        ParentedInvariantTree child2 = tree.getChildren().get(1);

        for (StateValue enforcedState: enforcedStates) {
            if (enforcedState instanceof BoolLiteralValue) {
                BoolLiteralValue enforcedBool = (BoolLiteralValue) enforcedState;
                boolean enforced = enforcedBool.getValue(); // assuming it's true or false

                if (child1.getType() == VarType.INTEGER
                        && child2.getType() == VarType.INTEGER) {
                    IntLiteralValue intValue1 = (IntLiteralValue) child1.getStateValue();
                    IntLiteralValue intValue2 = (IntLiteralValue) child2.getStateValue();

                    Integer child1_min = intValue1.getMinValue();
                    Integer child1_max = intValue1.getMaxValue();
                    Integer child2_min = intValue2.getMinValue();
                    Integer child2_max = intValue2.getMaxValue();

                    if (child1_min == child1_max) {
                        IntLiteralValue enforcedInterval1 = new IntLiteralValue(child1_min, child1_min);
                        IntLiteralValue enforcedInterval2 = intValue2.copy();
                        if (enforced) {
                            // child1 >= child2  =>  child2 <= child1_min
                            enforcedInterval2.intersect(Integer.MIN_VALUE, child1_min);
                        } else {
                            // child1 < child2  =>  child2 > child1_min  =>  child2 >= child1_min + 1
                            enforcedInterval2.intersect(child1_min + 1, Integer.MAX_VALUE);
                        }
                        newEnforce1.add(enforcedInterval1);
                        newEnforce2.add(enforcedInterval2);
                    } else if (child2_min == child2_max) {
                        IntLiteralValue enforcedInterval2 = new IntLiteralValue(child2_min, child2_min);
                        IntLiteralValue enforcedInterval1 = intValue1.copy();
                        if (enforced) {
                            // child1 >= child2_min
                            enforcedInterval1.intersect(child2_min, Integer.MAX_VALUE);
                        } else {
                            // child1 < child2_min  =>  child1 <= child2_min - 1
                            enforcedInterval1.intersect(Integer.MIN_VALUE, child2_min - 1);
                        }
                        newEnforce1.add(enforcedInterval1);
                        newEnforce2.add(enforcedInterval2);
                    } else {
                        System.out.println("noo");
                    }
                }
            }
        }
        enforceState(child1, newEnforce1, seenBranches);
        enforceState(child2, newEnforce2, seenBranches);
    }

    private static void handleLE(ParentedInvariantTree tree, ArrayList<StateValue> enforcedStates, ArrayList<Integer> seenBranches) {
        ArrayList<StateValue> newEnforce1 = new ArrayList<>();
        ArrayList<StateValue> newEnforce2 = new ArrayList<>();
        ParentedInvariantTree child1 = tree.getChildren().get(0);
        ParentedInvariantTree child2 = tree.getChildren().get(1);

        for (StateValue enforcedState: enforcedStates) {
            if (enforcedState instanceof BoolLiteralValue) {
                BoolLiteralValue enforcedBool = (BoolLiteralValue) enforcedState;
                boolean enforced = enforcedBool.getValue(); // assuming it's true or false

                if (child1.getType() == VarType.INTEGER
                        && child2.getType() == VarType.INTEGER) {
                    IntLiteralValue intValue1 = (IntLiteralValue) child1.getStateValue();
                    IntLiteralValue intValue2 = (IntLiteralValue) child2.getStateValue();

                    Integer child1_min = intValue1.getMinValue();
                    Integer child1_max = intValue1.getMaxValue();
                    Integer child2_min = intValue2.getMinValue();
                    Integer child2_max = intValue2.getMaxValue();

                    if (child1_min == child1_max) {
                        IntLiteralValue enforcedInterval1 = new IntLiteralValue(child1_min, child1_min);
                        IntLiteralValue enforcedInterval2 = intValue2.copy();
                        if (enforced) {
                            // child1 <= child2  =>  child2 >= child1_min
                            enforcedInterval2.intersect(child1_min, Integer.MAX_VALUE);
                        } else {
                            // !(child1 <= child2)  =>  child2 < child1_min  =>  child2 <= child1_min - 1
                            enforcedInterval2.intersect(Integer.MIN_VALUE, child1_min - 1);
                        }
                        newEnforce1.add(enforcedInterval1);
                        newEnforce2.add(enforcedInterval2);
                    } else if (child2_min == child2_max) {
                        IntLiteralValue enforcedInterval2 = new IntLiteralValue(child2_min, child2_min);
                        IntLiteralValue enforcedInterval1 = intValue1.copy();
                        if (enforced) {
                            // child1 <= child2_min
                            enforcedInterval1.intersect(Integer.MIN_VALUE, child2_min);
                        } else {
                            // child1 > child2_min  =>  child1 >= child2_min + 1
                            enforcedInterval1.intersect(child2_min + 1, Integer.MAX_VALUE);
                        }
                        newEnforce1.add(enforcedInterval1);
                        newEnforce2.add(enforcedInterval2);
                    } else {
                        System.out.println("noo");
                    }
                }
            }
        }
        enforceState(child1, newEnforce1, seenBranches);
        enforceState(child2, newEnforce2, seenBranches);
    }

    private static void handleEQ(ParentedInvariantTree tree, StateValue enforcedState, ArrayList<Integer> seenBranches) {
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
                handleIntegerEQ(seenBranches, child1, child2, enforced);

            }

        }
    }
    private static void handleEQ(ParentedInvariantTree tree, ArrayList<StateValue> enforcedStates, ArrayList<Integer> seenBranches) {
        ArrayList<StateValue> newEnforceStates1 = new ArrayList<>();
        ArrayList<StateValue> newEnforceStates2 = new ArrayList<>();
        ParentedInvariantTree child1 = tree.getChildren().get(0);
        ParentedInvariantTree child2 = tree.getChildren().get(1);

        for (StateValue enforcedState: enforcedStates){
            if (enforcedState instanceof BoolLiteralValue) {
                BoolLiteralValue enforcedBool = (BoolLiteralValue) enforcedState;
                boolean enforced = enforcedBool.getValue(); // assuming it's true or false

                if (child1.getType() == VarType.BITVECTOR
                        && child2.getType() == VarType.BITVECTOR) {
                     List<StateValue> result = getBVBinaryLogicalReversingResults(tree, enforcedState, seenBranches, "Reverse_BVs_EQ");
                     newEnforceStates1.add(result.get(0));
                     newEnforceStates2.add(result.get(1));
                } else if (child1.getType() == VarType.INTEGER
                        && child2.getType() == VarType.INTEGER) {
                    List<StateValue> result =  getIntegerEQResults(child1, child2, enforced);
                    newEnforceStates1.add(result.get(0));
                    newEnforceStates2.add(result.get(1));
                } else if (child1.getType() == VarType.BOOLEAN
                        && child2.getType() == VarType.BOOLEAN) {
                    List<StateValue> result =  getBooleanEQResults(child1, child2, enforced);
                    newEnforceStates1.add(result.get(0));
                    newEnforceStates2.add(result.get(1));
                } else if (child1.getType() == VarType.DOUBLE
                        && child2.getType() == VarType.DOUBLE){
                    List<StateValue> result =  getDoubleEQResults(child1, child2, enforced);
                    newEnforceStates1.add(result.get(0));
                    newEnforceStates2.add(result.get(1));
                }
                else {
                    System.out.println("this eq is not between two bv or int or bool or FP");
                }

            }
        }
        enforceState(child1, newEnforceStates1, seenBranches);
        enforceState(child2, newEnforceStates2, seenBranches);

    }

    private static void handleIntegerEQ(ArrayList<Integer> seenBranches, ParentedInvariantTree child1, ParentedInvariantTree child2, boolean enforced) {
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
                enforcedInterval1.exclude(child2_min, child2_max);
            }
            enforceState(child1, enforcedInterval1, seenBranches);
            enforceState(child2, enforcedInterval2, seenBranches);
        }else {
            System.out.println("noo");
        }

//                            IntLiteralValue enforcedInterval1 = new IntLiteralValue(A_min_r, A_max_r);
//                            IntLiteralValue enforcedInterval2 = new IntLiteralValue(B_min_r, B_max_r);
    }
    private static List<StateValue> getIntegerEQResults(ParentedInvariantTree child1, ParentedInvariantTree child2, boolean enforced) {
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
            return Arrays.asList(enforcedInterval1, enforcedInterval1);
        }else if (child2_min == child2_max){
            IntLiteralValue enforcedInterval2 = new IntLiteralValue(child2_min, child2_min);
            IntLiteralValue enforcedInterval1;
            if (enforced){
                enforcedInterval1 = new IntLiteralValue(child2_min, child2_min);
            }else {
                enforcedInterval1 = intValue1.copy();
                enforcedInterval1.exclude(child2_min, child2_max);
            }
            return Arrays.asList(enforcedInterval1, enforcedInterval1);
        }else {
            System.out.println("noo");
            return null;
        }

    }
    private static List<StateValue> getBooleanEQResults(ParentedInvariantTree child1, ParentedInvariantTree child2, boolean enforced) {
        BoolLiteralValue boolValue1 = (BoolLiteralValue) child1.getStateValue();
        BoolLiteralValue boolValue2 = (BoolLiteralValue) child2.getStateValue();

        if (!boolValue1.isUnknown()){
            boolean b1 = boolValue1.getValue();
            BoolLiteralValue enf1 = new BoolLiteralValue(b1);
            if (enforced){
                BoolLiteralValue enf2 = new BoolLiteralValue(b1);
                return Arrays.asList(enf1, enf2);
            }else {
                BoolLiteralValue enf2 = new BoolLiteralValue(!b1);
                return Arrays.asList(enf1, enf2);
            }
        }if (!boolValue2.isUnknown()){
            boolean b2 = boolValue2.getValue();
            BoolLiteralValue enf2 = new BoolLiteralValue(b2);
            if (enforced){
                BoolLiteralValue enf1 = new BoolLiteralValue(b2);
                return Arrays.asList(enf1, enf2);
            }else {
                BoolLiteralValue enf1 = new BoolLiteralValue(!b2);
                return Arrays.asList(enf1, enf2);
            }
        }
        System.out.println("noo eq between two unknown booleans is not easy");
        return null;

    }
    private static List<StateValue> getDoubleEQResults(ParentedInvariantTree child1, ParentedInvariantTree child2, boolean enforced) {
        FloatingPointLiteralValue fp1 = (FloatingPointLiteralValue) child1.getStateValue();
        FloatingPointLiteralValue fp2 = (FloatingPointLiteralValue) child2.getStateValue();

        // I hope enforced is true always :_(

        if (!enforced){
            System.out.println("noo eq between two unknown booleans is not easy");
            return null;
        }
        if (!fp1.isUnknown()){
            return Arrays.asList(fp1.copy(), fp1.copy());
        }
        if (!fp2.isUnknown()){
            return Arrays.asList(fp2.copy(), fp2.copy());
        }
        System.out.println("comparing two unknown fp is not implemented");
        return null;
    }

    private static void handleNOT(ParentedInvariantTree tree, ArrayList<StateValue> enforcedStates, ArrayList<Integer> seenBranches) {
        ArrayList<StateValue> newEnforceStates = new ArrayList<>();
        for(StateValue state: enforcedStates){
            if (state instanceof BoolLiteralValue) {
                BoolLiteralValue enforcedBool = (BoolLiteralValue) state;
                BoolLiteralValue negatedEnforced = new BoolLiteralValue(enforcedBool.getNegate());
                newEnforceStates.add(negatedEnforced);
            }
        }
        for (ParentedInvariantTree child : tree.getChildren()) {
            enforceState(child, newEnforceStates, seenBranches);
        }
    }
    private static void handleNOT(ParentedInvariantTree tree, StateValue enforcedState, ArrayList<Integer> seenBranches) {
        if (enforcedState instanceof BoolLiteralValue) {
            BoolLiteralValue enforcedBool = (BoolLiteralValue) enforcedState;
            BoolLiteralValue negatedEnforced = new BoolLiteralValue(enforcedBool.getNegate());
            for (ParentedInvariantTree child : tree.getChildren()) {
                enforceState(child, negatedEnforced, seenBranches);
            }
        }
    }

    private static void handleAND(ParentedInvariantTree tree, StateValue enforcedState, ArrayList<Integer> seenBranches) {
        if (enforcedState instanceof BoolLiteralValue) {
//                      BoolLiteralValue enforcedBool = (BoolLiteralValue) enforcedState;
            ArrayList<Integer> branchIDs = new ArrayList<>();
            for (ParentedInvariantTree child : tree.getChildren()) {
                enforceState(child, enforcedState, seenBranches);
                branchIDs.add(child.getBranchID());
            }

            ArrayList<ParentedInvariantTree> variableNodes = tree.getVariableNodes();
            for (ParentedInvariantTree varNode: variableNodes) {
                StateValue currentState = null;
                for (Integer branchID : branchIDs){

                    StateValue stateForBranch = varNode.getStateForBranch(branchID);
                    if (stateForBranch == null){continue;}
                    if (currentState == null) currentState = stateForBranch.copy();
                    else {
                        boolean wasAble =  currentState.intersect(stateForBranch);
                        if (!wasAble){
                            System.out.println("phase2: intersect of " + varNode.getName() + " was not possible");
                        }
                    }
                }
                Log.info("we got " + currentState + "state for " + varNode.getName() + " variable");
                varNode.putStateForBranch(tree.getBranchID(), currentState);
            }
        }
    }
    private static void handleAND(ParentedInvariantTree tree, ArrayList<StateValue> enforcedStates, ArrayList<Integer> seenBranches) {
        ArrayList<Integer> branchIDs = new ArrayList<>();
        for (ParentedInvariantTree child : tree.getChildren()) {
            enforceState(child, enforcedStates, seenBranches);
            branchIDs.add(child.getBranchID());
        }

        // TODO: fix for list of states

        ArrayList<ParentedInvariantTree> variableNodes = tree.getVariableNodes();
        for (ParentedInvariantTree varNode: variableNodes) {
            StateValue currentState = null;
            for (Integer branchID : branchIDs){

                ArrayList<StateValue> statesForBranch = varNode.getStatesForBranch(branchID);
                if (statesForBranch == null){continue;}
                StateValue stateForBranch = statesForBranch.get(0);
                if (currentState == null) currentState = stateForBranch.copy();
                else {
                    boolean wasAble =  currentState.intersect(stateForBranch);
                    if (!wasAble){
                        System.out.println("phase2: intersect of " + varNode.getName() + " was not possible");
                    }
                }
            }
            Log.info("we got " + currentState + "state for " + varNode.getName() + " variable");
            varNode.putStatesForBranch(tree.getBranchID(), new ArrayList<StateValue>(Collections.singletonList(currentState)));
        }
    }

    private static void handleOR(ParentedInvariantTree tree, StateValue enforcedState, ArrayList<Integer> seenBranches) {
        if (enforcedState instanceof BoolLiteralValue) {
//                      BoolLiteralValue enforcedBool = (BoolLiteralValue) enforcedState;
            ArrayList<Integer> branchIDs = new ArrayList<>();
            for (ParentedInvariantTree child : tree.getChildren()) {
                enforceState(child, enforcedState, seenBranches);
                branchIDs.add(child.getBranchID());
            }

            ArrayList<ParentedInvariantTree> variableNodes = tree.getVariableNodes();
            for (ParentedInvariantTree varNode: variableNodes) {
                StateValue currentState = null;
                for (Integer branchID : branchIDs){

                    StateValue stateForBranch = varNode.getStateForBranch(branchID);
                    if (stateForBranch == null){continue;}
                    if (currentState == null) currentState = stateForBranch.copy();
                    else {
                        boolean wasAble =  currentState.union(stateForBranch);
                        if (!wasAble){
                            System.out.println("phase2: union of " + varNode.getName() + " was not possible");
                        }
                    }
                }
                Log.info("we got " + currentState + "state for " + varNode.getName() + " variable");
                varNode.putStatesForBranch(tree.getBranchID(), new ArrayList<StateValue>(Collections.singletonList(currentState)));
            }
        }
    }

    private static void handleOR(ParentedInvariantTree tree, ArrayList<StateValue> enforcedStates, ArrayList<Integer> seenBranches) {
        ArrayList<Integer> branchIDs = new ArrayList<>();

        for (ParentedInvariantTree child : tree.getChildren()) {
            enforceState(child, enforcedStates, seenBranches);
            branchIDs.add(child.getBranchID());
        }

        // TODO: fix for list of states


        ArrayList<ParentedInvariantTree> variableNodes = tree.getVariableNodes();
        for (ParentedInvariantTree varNode: variableNodes) {
            StateValue currentState = null;
            for (Integer branchID : branchIDs){

                ArrayList<StateValue> statesForBranch = varNode.getStatesForBranch(branchID);
                if (statesForBranch == null){continue;}
                StateValue stateForBranch = statesForBranch.get(0);
                if (currentState == null) currentState = stateForBranch.copy();
                else {
                    boolean wasAble =  currentState.union(stateForBranch);
                    if (!wasAble){
                        System.out.println("phase2: union of " + varNode.getName() + " was not possible");
                    }
                }
            }
            Log.info("we got " + currentState + "state for " + varNode.getName() + " variable");
            varNode.putStateForBranch(tree.getBranchID(), currentState);
        }

    }


    private static void enforceState(ParentedInvariantTree tree, ArrayList<StateValue> enforcedStates,
                                     ArrayList<Integer> seenBranches)
    {
        if (enforcedStates.size() > 1){
            boolean areAllBv = true;
            ArrayList<BVLiteralValue> temp = new ArrayList<>();
            for (StateValue s : enforcedStates){
                
                if (!(s instanceof BVLiteralValue)) {areAllBv = false;}
                else{temp.add((BVLiteralValue) s);}
            }
            if (areAllBv){
                ArrayList<BVLiteralValue> tempResult = mergeBVStates(temp);
                if (tempResult.size() < enforcedStates.size()){
                    enforcedStates.clear();
                    enforcedStates.addAll(tempResult);
                }
            }
        }

        boolean hasLogicalParent  = tree.hasLogicalParent();
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
            for (int branchID: seenBranches) {// skipped this for now but need to change for array of states
                ArrayList<StateValue> thisBranchStates = new ArrayList<>();
                for (StateValue sv: enforcedStates){
                    thisBranchStates.add(sv.copy());
                }
                tree.putStatesForBranch(branchID, thisBranchStates);
            } // todo recheck:
            return;
        }
        if (tree.getNodeType() == ParentedInvariantTree.NodeType.OPERATION) {
            Log.info("Starting to enforce " + enforcedStates.toString() + " to " + tree.getOpType() + "operation.");
            switch (tree.getOpType()) {
                case OR:
                    handleOR(tree, enforcedStates, seenBranches); // TODO change for array of states
                    break;
                case AND:
                    handleAND(tree, enforcedStates, seenBranches); // TODO change for array of states
                    break;
                case NOT:
                    handleNOT(tree, enforcedStates, seenBranches);
                    break;
                case ITE:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case EQ:
                    handleEQ(tree, enforcedStates, seenBranches);
                    break;
                case LE:
                    handleLE(tree, enforcedStates, seenBranches);
                    break;
                case LT:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case GE:
                    handleGE(tree, enforcedStates, seenBranches);
                    break;
                case GT:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case MUL:
                    handleMUL(tree, enforcedStates, seenBranches);
                    break;
                case ADD:
                    handleADD(tree, enforcedStates, seenBranches);
                    break;
                case BIT2BOOL:
                    handleBIT2BOOL(tree, enforcedStates, seenBranches);
                    break;
                case BVADD:
                    handleBVADD(tree, enforcedStates, seenBranches);
                    break;
                case BVEXTRACT:
                    handleBVEXTRACT(tree, enforcedStates, seenBranches);
                    break;
                case BVCONCAT: // assuming it's a binary operation. if not you should clean the tree first
                    // we should find the unknown lengths and determine it
                    handleBVCONCAT(tree, enforcedStates, seenBranches);
                    break;
                case BVSLE:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case BVULE:
                    handleBVULE(tree, enforcedStates, seenBranches);
                    break;
                case BVUGE:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case BVSGE:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case BVULT:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case BVUGT:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case BVNEG:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case BVSUB:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case BVLSHR:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case BVSHL:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case BVUDIV:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case BVMUL:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case ZERO_EXTEND:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case EFP_SIGN:
                case FP_SIGN:
                    handleFP_SIGN(tree, enforcedStates, seenBranches);
                    break;
                case FP_EXPONENT:
                case EFP_EXPONENT:
                    handleEXPONENT(tree, enforcedStates, seenBranches);
                    break;
                case EFP_MANTISSA:
                case FP_MANTISSA:
                    handleMANTISSA(tree, enforcedStates, seenBranches);
                    break;
                case MOD_CAST:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case INT_CAST:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case EXISTS:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case FORALL:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case FLOATING_POINT:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case DOUBLE_FLOATING_POINT:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case EXTENDED_FLOATING_POINT:
                    logUnsupportedOperationMessage(tree.getOpType());
                    break;
                case EXTENDED_DOUBLE_FLOATING_POINT:
                    logUnsupportedOperationMessage(tree.getOpType());
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


//                System.out.println(A_v_r + " " + A_m_r +", "+ B_v_r + " " + B_m_r +" "  );
                BVLiteralValue enforcedBV1 = BVLiteralValue.mkBVLiteralValue(A_v_r, A_m_r);
                BVLiteralValue enforcedBV2 = BVLiteralValue.mkBVLiteralValue(B_v_r, B_m_r);
                enforceState(child1, enforcedBV1, seenBranches);
                enforceState(child2, enforcedBV2, seenBranches);
            }
        }
    }
    private static List<StateValue> getBVBinaryLogicalReversingResults(ParentedInvariantTree tree, StateValue enforcedState,
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


//                System.out.println(A_v_r + " " + A_m_r +", "+ B_v_r + " " + B_m_r +" "  );
                BVLiteralValue enforcedBV1 = BVLiteralValue.mkBVLiteralValue(A_v_r, A_m_r);
                BVLiteralValue enforcedBV2 = BVLiteralValue.mkBVLiteralValue(B_v_r, B_m_r);

                return  Arrays.asList(enforcedBV1, enforcedBV2);
            }
        }
        return null;
    }

    private static ArrayList<StateValue>[] getBVULEReversingResults(ParentedInvariantTree tree, StateValue enforcedState) {
        List<BVLiteralValue>[] outs = new List[2];
        outs[0] = new ArrayList<BVLiteralValue>();
        outs[1] = new ArrayList<BVLiteralValue>();
        if (enforcedState instanceof BoolLiteralValue) {
            BoolLiteralValue enforcedBool = (BoolLiteralValue) enforcedState;
            boolean enforced = enforcedBool.getValue(); // assuming it's true or false

            ParentedInvariantTree child1 = tree.getChildren().get(0);
            ParentedInvariantTree child2 = tree.getChildren().get(1);

            if (child1.getType() == VarType.BITVECTOR
                    && child2.getType() == VarType.BITVECTOR) {
                BVLiteralValue bvValue1 = (BVLiteralValue) child1.getStateValue();
                BVLiteralValue bvValue2 = (BVLiteralValue) child2.getStateValue();

                ArrayList<StateValue>[] outBVULEAnswers = outBVULEAllAnswers(bvValue1, bvValue2, enforced);


                return  outBVULEAnswers;
            }
        }
        return null;
    }
    private static int countDifferences(String a, String b) {
        int minLength = Math.min(a.length(), b.length());
        int diffCount = 0;

        for (int i = 0; i < minLength; i++) {
            if (a.charAt(i) != b.charAt(i)) {
                diffCount++;
            }
        }

        diffCount += Math.abs(a.length() - b.length());

        return diffCount;
    }

    private static ArrayList<BVLiteralValue> mergeBVStates(List<BVLiteralValue> BVs) {
        if (BVs.size() <= 1){
            return (ArrayList<BVLiteralValue>) BVs;
        }
        BVLiteralValue bv1 = BVs.get(0);
        BVLiteralValue bv2 = BVs.get(1);
        if (bv1.getMaskStr().equals(bv2.getMaskStr())
                && countDifferences(bv1.getValueStr(), bv2.getValueStr()) <= 1){
            ArrayList<BVLiteralValue> result = new ArrayList<>();
            BVLiteralValue copy = bv1.copy();
            copy.union(bv2);
            result.add(copy);
            return result;
        }else {
            return (ArrayList<BVLiteralValue>) BVs;
        }

    }

    private static void saveToOut(List<String> out, List<BVLiteralValue>[] outs) {
        if (out != null){
            String A_v_r = out.get(0);
            String A_m_r = out.get(1);
            String B_v_r = out.get(2);
            String B_m_r = out.get(3);

            BVLiteralValue enforcedBV1 = BVLiteralValue.mkBVLiteralValue(A_v_r, A_m_r);
            BVLiteralValue enforcedBV2 = BVLiteralValue.mkBVLiteralValue(B_v_r, B_m_r);

            outs[0].add(enforcedBV1);
            outs[1].add(enforcedBV2);
        }
    }

    private static ArrayList<StateValue>[] outBVULEAllAnswers(
            BVLiteralValue left, BVLiteralValue right, Boolean enforce) {

        String value1 = left.getValueStr();
        String mask1  = left.getMaskStr();
        String value2 = right.getValueStr();
        String mask2  = right.getMaskStr();

        boolean leftFixed  = !mask1.contains("0");
        boolean rightFixed = !mask2.contains("0");

        if (!leftFixed && !rightFixed) {
            throw new RuntimeException("At least one side must be fixed.");
        }

        ArrayList<StateValue> lhsAnswers = new ArrayList<>();
        ArrayList<StateValue> rhsAnswers = new ArrayList<>();

        if (leftFixed) {
            lhsAnswers.add(left.copy());
            rhsAnswers = computeFlexibleSide(value1, value2, mask2, enforce, true);
            if (enforce) rhsAnswers.add(left.copy()); // equality case
        } else {
            rhsAnswers.add(right.copy());
            lhsAnswers = computeFlexibleSide(value2, value1, mask1, enforce, false);
            if (enforce) lhsAnswers.add(right.copy()); // equality case
        }

        ArrayList<StateValue>[] result = new ArrayList[2];
        result[0] = lhsAnswers;
        result[1] = rhsAnswers;
        return result;
    }

    /**
     * Computes valid assignments for the flexible (unknown) side of a BVULE constraint.
     *
     * @param fixed      bit-string of the fixed side
     * @param flexValue  value bits of the flexible side
     * @param flexMask   mask bits of the flexible side (0 = unknown, 1 = known)
     * @param enforce    true  => fixed <= flexible (flexible must be >= fixed)
     *                   false => fixed >  flexible (flexible must be <  fixed)
     * @param fixedIsLHS true when fixed side is LHS of the original BVULE expression
     */
    private static ArrayList<StateValue> computeFlexibleSide(
            String fixed, String flexValue, String flexMask,
            boolean enforce, boolean fixedIsLHS) {

        ArrayList<StateValue> answers = new ArrayList<>();
        int n = fixed.length();

        // When fixedIsLHS:  enforce => fixed <= flex  => flex >= fixed
        //                  !enforce => fixed >  flex  => flex <  fixed
        // When !fixedIsLHS: enforce => flex <= fixed  => flex <= fixed  (flex is LHS)
        //                  !enforce => flex >  fixed  => flex >  fixed
        // In both cases, after normalising, we want:
        //   enforce  => flex should be made >= fixed
        //   !enforce => flex should be made <  fixed
        boolean flexShouldBeGreater = enforce == fixedIsLHS;

        for (int i = 0; i < n; i++) {
            char f  = fixed.charAt(i);
            char fv = flexValue.charAt(i);
            char fm = flexMask.charAt(i);

            if (fm == '1') {
                // This bit is already known
                if (fv == f) continue; // still equal, keep scanning

                if (flexShouldBeGreater) {
                    // flex > fixed at this bit: done (remaining bits can be anything)
                    if (fv == '1' && f == '0') { answers.add(bvuleCopyPrefix(fixed, flexValue, flexMask, i)); }
                    // flex < fixed at this bit: impossible to satisfy
                    break;
                } else {
                    // flex < fixed at this bit: done
                    if (fv == '0' && f == '1') { answers.add(bvuleCopyPrefix(fixed, flexValue, flexMask, i)); }
                    // flex > fixed at this bit: impossible to satisfy
                    break;
                }
            } else {
                // This bit is unknown — set it to make the comparison go the right way
                char targetBit = flexShouldBeGreater ? '1' : '0';
                // Only useful if fixed bit agrees with the direction we need
                if (flexShouldBeGreater && f == '0') {
                    // Setting flex[i] = 1 makes flex > fixed here; remaining bits free
                    answers.add(bvuleSetBit(fixed, flexValue, flexMask, i, targetBit));
                } else if (!flexShouldBeGreater && f == '1') {
                    // Setting flex[i] = 0 makes flex < fixed here; remaining bits free
                    answers.add(bvuleSetBit(fixed, flexValue, flexMask, i, targetBit));
                }
                // If f == targetBit, setting flex[i] = targetBit keeps them equal — continue scanning
            }
        }

        return answers;
    }

    private static BVLiteralValue bvuleCopyPrefix(
            String fixed,
            String value,
            String mask,
            int index
    ) {

        char[] v = value.toCharArray();
        char[] m = mask.toCharArray();

        for (int i = 0; i <= index; i++) {
            v[i] = fixed.charAt(i);
            m[i] = '1';
        }

        return BVLiteralValue.mkBVLiteralValue(
                new String(v),
                new String(m)
        );
    }
    private static BVLiteralValue bvuleSetBit(
            String fixed,
            String value,
            String mask,
            int index,
            char bit
    ) {

        char[] v = value.toCharArray();
        char[] m = mask.toCharArray();

        for (int i = 0; i < index; i++) {
            v[i] = fixed.charAt(i);
            m[i] = '1';
        }

        v[index] = bit;
        m[index] = '1';

        return BVLiteralValue.mkBVLiteralValue(
                new String(v),
                new String(m)
        );
    }



    private static BVLiteralValue bvuleCopyPrefix(String value1, String value2, int i) { // return value1[:i-1]1?????
        ArrayList<BoolLiteralValue> booleanLiterals = new ArrayList<>();
        for (int j = 0; j < i; j++) {
            booleanLiterals.add(new BoolLiteralValue(value1.charAt(j) == '1'));
        }
        booleanLiterals.add(new BoolLiteralValue(true));
        for (int j = i +1; j< value2.length(); j++) {
            booleanLiterals.add(new BoolLiteralValue(GBool.UNKNOWN));
        }
        Collections.reverse(booleanLiterals);
        BVLiteralValue answer = new BVLiteralValue(booleanLiterals);
        return answer;
    }


    private static final String BVULE_FILE_NAME = "Reverse_BVs_ULE_v2";
    private static List<String> outBVULEFirstAnswer(String value1, String mask1,
                                                    String value2, String mask2, String enforcedStr) {

        // todo: find which of these two is fixed
        // todo: find the most significant unknown bit and set it to one

        if (!mask1.contains("0")){
//            first one is fixed
            int msm = mask2.indexOf('0');// most significant mask
            mask2 = mask2.substring(0, msm) + '1' + mask2.substring(msm + 1);
            value2 = value2.substring(0, msm) + '1' + value2.substring(msm + 1);

        }else if (!mask2.contains("0")) {
//            second one is fixed
            int msm = mask1.indexOf('0');// most significant mask
            mask1 = mask1.substring(0, msm) + '1' + mask1.substring(msm + 1);
            value1 = value1.substring(0, msm) + '1' + value1.substring(msm + 1);

        } else {
            System.out.println("can not reverse bvule two flexible fp"); return null;
        }

        List<String> out = PythonBridge.run(BVULE_FILE_NAME,
                String.valueOf(value1.length()),
                String.valueOf(value1), String.valueOf(mask1),
                String.valueOf(value2), String.valueOf(mask2),
                (enforcedStr)
        );
        return out;
    }
    private static List<String> outBVULESecondAnswer(String value1, String mask1,
                                                    String value2, String mask2, String enforcedStr) {

        // todo: find which of these two is fixed
        // todo: find the most significant unknown bit and set it to one

        if (!mask1.contains("0")){
//            first one is fixed
            int msm = mask2.indexOf('0');// most significant mask
            mask2 = mask2.substring(0, msm) + '1' + mask2.substring(msm + 1);
            value2 = value2.substring(0, msm) + '0' + value2.substring(msm + 1);

        }else if (!mask2.contains("0")) {
//            second one is fixed
            int msm = mask1.indexOf('0');// most significant mask
            mask1 = mask1.substring(0, msm) + '1' + mask1.substring(msm + 1);
            value1 = value1.substring(0, msm) + '0' + value1.substring(msm + 1);

        } else {
            System.out.println("can not reverse bvule two flexible fp"); return null;
        }

        List<String> out = PythonBridge.run(BVULE_FILE_NAME,
                String.valueOf(value1.length()),
                String.valueOf(value1), String.valueOf(mask1),
                String.valueOf(value2), String.valueOf(mask2),
                (enforcedStr)
        );
        return out;
    }


}