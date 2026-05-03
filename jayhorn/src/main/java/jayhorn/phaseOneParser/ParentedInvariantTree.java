package jayhorn.AST.Nodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParentedInvariantTree extends InvariantTree {

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
        return node;
    }

    public static ParentedInvariantTree variable(String name, VarType type) {
        ParentedInvariantTree node = new ParentedInvariantTree(NodeType.VARIABLE);
        node.name = name;
        node.type = type;
        return node;
    }

    public static ParentedInvariantTree literal(Object value, VarType type) {
        ParentedInvariantTree node = new ParentedInvariantTree(NodeType.LITERAL);
        node.value = value;
        node.type = type;
        return node;
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
