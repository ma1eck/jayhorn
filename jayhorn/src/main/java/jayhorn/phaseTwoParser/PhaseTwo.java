package jayhorn.phaseTwoParser;

import jayhorn.AST.ASTHelper;
import jayhorn.AST.Nodes.InvariantTree;
import jayhorn.AST.Nodes.OpType;
import jayhorn.AST.Nodes.VarType;
import jayhorn.AST.Nodes.VariableNode;
import jayhorn.phaseOneParser.LiteralValues.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.microsoft.z3.*;
import jayhorn.phaseOneParser.ParentedInvariantTree;

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
                tree.putStateForBranch(branchID, enforcedState);
            } // todo recheck:
            return;
        }
        if (tree.getNodeType() == ParentedInvariantTree.NodeType.OPERATION) {
            switch (tree.getOpType()){
                case OR:
                    if (enforcedState instanceof BoolLiteralValue) {
//                      BoolLiteralValue enforcedBool = (BoolLiteralValue) enforcedState;
                        for (ParentedInvariantTree child : tree.getChildren()){
                            enforceState(child, enforcedState, seenBranches);
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
                        for (ParentedInvariantTree child : tree.getChildren()){
                            enforceState(child, negatedEnforced, seenBranches);
                        }
                    }
                    break;
                case ITE:
                    break;
                case EQ:
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
                    break;
                case BVADD:
                    break;
                case BVEXTRACT:
                    break;
                case BVCONCAT:
                    break;
                case BVSLE:
                    break;
                case BVULE:
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
                case FP_SIGN:
                    break;
                case FP_EXPONENT:
                    break;
                case FP_MANTISSA:
                    break;
                case EFP_SIGN:
                    break;
                case EFP_EXPONENT:
                    break;
                case EFP_MANTISSA:
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


}