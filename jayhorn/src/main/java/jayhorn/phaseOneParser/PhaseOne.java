package jayhorn.phaseOneParser;

import jayhorn.AST.ASTHelper;
import jayhorn.AST.Nodes.InvariantTree;
import jayhorn.AST.Nodes.VarType;
import jayhorn.AST.Nodes.VariableNode;
import jayhorn.phaseOneParser.LiteralValues.StateValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PhaseOne {
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

        return initialTree;
    }

//    private static ParentedInvariantTree phase1(ParentedInvariantTree tree){
//        if (tree.getNodeType() == ParentedInvariantTree.NodeType.VARIABLE ||
//                tree.getNodeType() == ParentedInvariantTree.NodeType.LITERAL){
//            return tree;
//        }
//        case (tree.getOpType()){
//
//        }
//
//
//    }

}
