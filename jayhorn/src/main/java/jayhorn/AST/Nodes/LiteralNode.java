package jayhorn.AST.Nodes;

import java.util.Map;

public class LiteralNode extends Node {
    private final Object value;
    private final VarType type;

    public LiteralNode(Object value, VarType type) {
        this.value = value;
        this.type = type;
    }

    public Object getValue() { return value; }
    public VarType getType() { return type; }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        if (type == VarType.FLOAT || type == VarType.DOUBLE) {
            if (value instanceof Map) {
                Map<?, ?> fp = (Map<?, ?>) value;
                return type + "{s=" + fp.get("sign") + ", e=" + fp.get("exponent") + ", m=" + fp.get("mantissa") + "}";
            }
        }
        return String.valueOf(value);
    }
}