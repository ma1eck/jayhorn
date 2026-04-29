package jayhorn.AST.Nodes;
public class VariableNode extends InvariantTree {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final VarType type;

    public VariableNode(String name, VarType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() { return name; }
    public VarType getType() { return type; }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return name + ":" + type;
    }
}
