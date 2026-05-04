package jayhorn.phaseOneParser;

import jas.Var;
import jayhorn.AST.Nodes.*;
import jayhorn.phaseOneParser.LiteralValues.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParentedInvariantTree extends InvariantTree {
    private static final long serialVersionUID = 1L;

    public enum NodeType {
        OPERATION,
        VARIABLE,
        LITERAL
    }

    private final NodeType nodeType;
    private final List<ParentedInvariantTree> parents;

    private OpType opType;
    private List<ParentedInvariantTree> children;
    private List<Integer> params;

    private String name;
    private VarType type;
    private Object value;

    private StateValue stateValue;

    private ParentedInvariantTree(NodeType nodeType) {
        this.nodeType = nodeType;
        this.parents = new ArrayList<ParentedInvariantTree>();
        this.children = new ArrayList<ParentedInvariantTree>();
        this.params = new ArrayList<Integer>();
    }

    public static ParentedInvariantTree operation(OpType opType, List<ParentedInvariantTree> children) {
        return operation(opType, children, new ArrayList<Integer>());
    }

    public static ParentedInvariantTree operation(
            OpType opType,
            List<ParentedInvariantTree> children,
            List<Integer> params) {
        ParentedInvariantTree node = new ParentedInvariantTree(NodeType.OPERATION);
        node.opType = opType;
        if (params != null) {
            node.params = new ArrayList<Integer>(params);
        }
        if (children != null) {
            for (ParentedInvariantTree child : children) {
                node.addChild(child);
            }
        }
        VarType resultType = opType.getResultVarType();
        StateValue varState = getVariableStateByType(resultType);
        if (varState != null){
            node.setStateValue(varState);
        }
        return node;
    }

    public static ParentedInvariantTree variable(String name, VarType type, StateValue state) {
        ParentedInvariantTree node = new ParentedInvariantTree(NodeType.VARIABLE);
        node.name = name;
        node.type = type;
        node.setStateValue(state);
        return node;
    }
    public static ParentedInvariantTree variable(String name, VarType type) {
        ParentedInvariantTree node = new ParentedInvariantTree(NodeType.VARIABLE);
        node.name = name;
        node.type = type;
        StateValue varState = getVariableStateByType(type);
        if (varState != null){
            node.setStateValue(varState);
        }
        return node;
    }
    public static StateValue getVariableStateByType(VarType type){
        switch (type){
            case BOOLEAN:
               return new BoolLiteralValue();
            case INTEGER:
                return new IntLiteralValue();
            case FLOAT:
                return new FloatingPointLiteralValue(8 ,11);
            case DOUBLE:
                return new FloatingPointLiteralValue(24 ,53);
            case EFLOAT:
                return new FloatingPointLiteralValue(9 ,12);
            case EDOUBLE:
                return new FloatingPointLiteralValue(72 ,159);
            case BITVECTOR:
                return new BVLiteralValue(200);
                // ?? should store arity
            default:
                return null;
        }
    }

    public static ParentedInvariantTree literal(Object value, VarType type) {
        ParentedInvariantTree node = new ParentedInvariantTree(NodeType.LITERAL);
        node.value = value;
        node.type = type;
        StateValue stateByType = getLiteralStateByType(value, type);
        if (stateByType != null){
            node.setStateValue(stateByType);
        }
        return node;
    }

    private static StateValue getLiteralStateByType(Object value, VarType type){
        switch (type){
            case BOOLEAN:
                if (value instanceof Boolean){
                    boolean b = (Boolean) value;
                    return new BoolLiteralValue( b ? GBool.TRUE : GBool.FALSE);
                }else return new BoolLiteralValue();
            case INTEGER:
                if (value instanceof Integer){
                    int i = (Integer) value;
                    return new IntLiteralValue(i);
                }else return new IntLiteralValue();
            case FLOAT:
                return new FloatingPointLiteralValue(8 ,11);
            case DOUBLE:
                return new FloatingPointLiteralValue(24 ,53);
            case EFLOAT:
                return new FloatingPointLiteralValue(9 ,12);
            case EDOUBLE:
                return new FloatingPointLiteralValue(72 ,159);
            case BITVECTOR:
                if (value instanceof String){
                    String valueStr = (String) value;
                    ArrayList<BoolLiteralValue> state = new ArrayList<>();
                    for (int i = 0; i < valueStr.length(); i++) {
                        char c = valueStr.charAt(i);
                        if (c == '1') {
                            state.add(new BoolLiteralValue(true));
                        } else if (c == '0') {
                            state.add(new BoolLiteralValue(false));
                        }
                    }
                    return new BVLiteralValue(state);
                }
            default:
                return null;
        }
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public List<ParentedInvariantTree> getParents() {
        return parents;
    }

    public OpType getOpType() {
        return opType;
    }

    public List<ParentedInvariantTree> getChildren() {
        return children;
    }

    public List<Integer> getParams() {
        return params;
    }

    public String getName() {
        return name;
    }

    public VarType getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public StateValue getStateValue(){
        return this.stateValue;
    }
    public void setStateValue(StateValue stateValue){
        this.stateValue = stateValue;
    }

    public void addChild(ParentedInvariantTree child) {
        children.add(child);
        child.addParent(this);
    }

    public void addParent(ParentedInvariantTree parent) {
        if (!parents.contains(parent)) {
            parents.add(parent);
        }
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return asInvariantTree().accept(visitor);
    }

    @Override
    public String toString() {
        if (nodeType == NodeType.VARIABLE) {
            return name + ":" + type;
        }
        if (nodeType == NodeType.LITERAL) {
            if (type == VarType.FLOAT || type == VarType.DOUBLE) {
                if (value instanceof Map) {
                    Map<?, ?> fp = (Map<?, ?>) value;
                    return type + "{s=" + fp.get("sign") + ", e=" + fp.get("exponent") + ", m=" + fp.get("mantissa") + "}";
                }
            }
            return String.valueOf(value);
        }

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
        if (nodeType != NodeType.OPERATION) {
            return toString();
        }

        StringBuilder sb = new StringBuilder();
        sb.append(opType.name());
        sb.append("(");

        if (params != null && !params.isEmpty()) {
            sb.append("[");
            sb.append(String.join(", ", params.stream()
                    .map(Object::toString)
                    .toArray(String[]::new)));
            sb.append("]");
        }

        if (children.isEmpty()) {
            return sb.append("()").toString();
        }

        for (int i = 0; i < children.size(); i++) {
            ParentedInvariantTree child = children.get(i);
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

    private InvariantTree asInvariantTree() {
        if (nodeType == NodeType.VARIABLE) {
            return new VariableNode(name, type);
        }
        if (nodeType == NodeType.LITERAL) {
            return new LiteralNode(value, type);
        }

        List<InvariantTree> invariantChildren = new ArrayList<InvariantTree>(children.size());
        for (ParentedInvariantTree child : children) {
            invariantChildren.add(child.asInvariantTree());
        }
        return new OperationNode(opType, invariantChildren, new ArrayList<Integer>(params));
    }
}
