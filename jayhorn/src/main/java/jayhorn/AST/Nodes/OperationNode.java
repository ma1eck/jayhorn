package jayhorn.AST.Nodes;

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
}
