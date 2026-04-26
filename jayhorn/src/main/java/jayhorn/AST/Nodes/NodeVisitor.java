package jayhorn.AST.Nodes;

public interface NodeVisitor<T> {
    T visit(OperationNode node);
    T visit(VariableNode node);
    T visit(LiteralNode node);
}
