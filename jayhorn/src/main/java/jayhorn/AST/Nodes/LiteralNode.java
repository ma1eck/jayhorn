package jayhorn.AST.Nodes;

import jayhorn.AST.ASTHelper;

import java.math.BigInteger;
import java.util.Map;

public class LiteralNode extends InvariantTree {
    private static final long serialVersionUID = 1L;

    private final Object value;
    private final VarType type;

    public LiteralNode(Object value, VarType type) {
        this.value = value;
        this.type = type;
    }

    public Object getValue() {
        if (getType() == VarType.BITVECTOR){
            String value1 = bvValue2String();
            if (value1 != null) return value1;
        }
        return value;
    }

    private String  bvValue2String() {
        if (value instanceof  String) return (String) value;
        if (value instanceof Integer) return int2bvString((Integer) value);
        if (value instanceof Long) return long2bvString((Long) value);
        if (value instanceof BigInteger) return bigInt2bvString((BigInteger) value);
        return null;
    }

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

    public static LiteralNode getIntLiteral(Long value){
        return new LiteralNode(value, VarType.LONG);
    }
    public static LiteralNode getIntLiteral(Integer value){
        return new LiteralNode(value, VarType.INTEGER);
    }
    public static LiteralNode getBVLiteral(String value){
        return new LiteralNode(value, VarType.BITVECTOR);
    }
    public static LiteralNode getBoolean(boolean value){
        return new LiteralNode(value, VarType.BOOLEAN);
    }

    public static LiteralNode createNumericLiteralNode(BigInteger valBI) {
        Number num = ASTHelper.shrinkBigInteger(valBI);
        if (num instanceof Integer) {
            return new LiteralNode(num, VarType.INTEGER);
        }
        if (num instanceof Long) {
            return new LiteralNode(num, VarType.LONG);
        }
        return new LiteralNode(num, VarType.BIGINT);
    }


    private static String int2bvString(int value) {
        return Integer.toBinaryString(value);
    }
    private static String long2bvString(long value) {
        return Long.toBinaryString(value);
    }
    private static String bigInt2bvString(BigInteger value) {
        return value.toString(2);
    }

}
