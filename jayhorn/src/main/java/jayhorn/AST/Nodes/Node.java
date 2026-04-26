package jayhorn.AST.Nodes;

public abstract class Node {
    public abstract <T> T accept(NodeVisitor<T> visitor);
    // Pretty print with indentation
    public String toPrettyString() {
        return toPrettyString(0);
    }

    protected String toPrettyString(int indent){
        return this.toString();
    }

    protected String getIndent(int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append("  ");
        }
        return sb.toString();
    }
}