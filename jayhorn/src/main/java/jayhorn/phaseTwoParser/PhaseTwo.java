package jayhorn.phaseTwoParser;

import jayhorn.AST.Nodes.OpType;
import jayhorn.AST.Nodes.VarType;
import jayhorn.Log;
import jayhorn.phaseOneParser.LiteralValues.*;
import jayhorn.phaseOneParser.ParentedInvariantTree;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
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

        boolean hasLogicalParent = hasLogicalParent(tree);

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
                                                      ArrayList<Integer> seenBranches)
    {

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
                    break;
                case EQ:
                    handleEQ(tree, enforcedState, seenBranches);
                    break;
                case LE:
                    break;
                case LT:
                    break;
                case GE:
                    handleGE(tree, enforcedState, seenBranches);
                    break;
                case GT:
                    break;
                case MUL:
                    break;
                case ADD:
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
                    break;
                case BVULE:
                    handleBVULE(tree, enforcedState, seenBranches);
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
        bvBinaryLogicalReversing(tree, enforcedState, seenBranches, "Reverse_BVs_ULE");
    }

    private static void handleBVULE(ParentedInvariantTree tree, ArrayList<StateValue> enforcedStates, ArrayList<Integer> seenBranches) {
        ArrayList<StateValue> newEnforceStates1 = new ArrayList<>();
        ArrayList<StateValue> newEnforceStates2 = new ArrayList<>();
        ParentedInvariantTree child1 = tree.getChildren().get(0);
        ParentedInvariantTree child2 = tree.getChildren().get(1);

        if (child1.getType() == VarType.BITVECTOR
                && child2.getType() == VarType.BITVECTOR) {
            for (StateValue enforcedState: enforcedStates){
                List<StateValue> result = getBVBinaryLogicalReversingResults(tree, enforcedState, seenBranches, "Reverse_BVs_ULE");
                newEnforceStates1.add(result.get(0));
                newEnforceStates2.add(result.get(1));
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
                        newEnforce1.add(enforcedBV1);
                        newEnforce2.add(enforcedBV2);
                }
            }
            enforceState(child1, newEnforce1, seenBranches);
            enforceState(child2, newEnforce2, seenBranches);
        }

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
                    List<StateValue> result =  getIntegerEQResults(seenBranches, child1, child2, enforced);
                    newEnforceStates1.add(result.get(0));
                    newEnforceStates2.add(result.get(1));
                } else {
                    System.out.println("this eq is not between two bv or int");
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
    private static List<StateValue> getIntegerEQResults(ArrayList<Integer> seenBranches, ParentedInvariantTree child1, ParentedInvariantTree child2, boolean enforced) {
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

                StateValue stateForBranch = varNode.getStatesForBranch(branchID).get(0);
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
                varNode.putStateForBranch(tree.getBranchID(), currentState);
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

                StateValue stateForBranch = varNode.getStatesForBranch(branchID).get(0);
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
            varNode.putStateForBranch(tree.getBranchID(), currentState);
        }

    }


    private static void enforceState(ParentedInvariantTree tree, ArrayList<StateValue> enforcedStates,
                                     ArrayList<Integer> seenBranches)
    {

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
                    break;
                case EQ:
                    handleEQ(tree, enforcedStates, seenBranches);
                    break;
                case LE:
                    break;
                case LT:
                    break;
                case GE:
                    handleGE(tree, enforcedStates, seenBranches);
                    break;
                case GT:
                    break;
                case MUL:
                    break;
                case ADD:
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
                    break;
                case BVULE:
                    handleBVULE(tree, enforcedStates, seenBranches);
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


}