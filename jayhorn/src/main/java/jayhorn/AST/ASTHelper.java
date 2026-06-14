package jayhorn.AST;


import jayhorn.AST.Nodes.*;
import jayhorn.phaseOneParser.LiteralValues.FloatingPointLiteralValue;
import jayhorn.phaseOneParser.LiteralValues.StateValue;
import jayhorn.phaseOneParser.ParentedInvariantTree;
import jayhorn.phaseTwoParser.PythonBridge;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ASTHelper {

    public static InvariantTree clampModCast(InvariantTree tree){
        if (!(tree instanceof OperationNode)) {
            return tree;
        }

        OperationNode opNode = (OperationNode) tree;
        OpType type = opNode.getOpType();
        List<InvariantTree> children = opNode.getChildren();

        if (type != OpType.MOD_CAST) {
            List<InvariantTree> newChildren = new ArrayList<>(children.size());
            for (InvariantTree child : children) {
                newChildren.add(clampModCast(child));
            }

            return new OperationNode(type, newChildren, ((OperationNode) tree).getParams());
        }

        if (children.size() != 3) {
            return tree;
        }

        InvariantTree child0 = children.get(0);
        InvariantTree child1 = children.get(1);
        InvariantTree child2 = children.get(2);

        if (child0 instanceof LiteralNode && child1 instanceof LiteralNode && child2 instanceof LiteralNode) {
            Object val0 = ((LiteralNode) child0).getValue();
            Object val1 = ((LiteralNode) child1).getValue();
            Object val2 = ((LiteralNode) child2).getValue();

            if (val0 instanceof Number && val1 instanceof Number && val2 instanceof Number) {
                BigInteger low = toBigInteger((Number) val0);
                BigInteger high = toBigInteger((Number) val1);
                BigInteger arg = toBigInteger((Number) val2);

                // if low > high, do nothing
                if (low.compareTo(high) > 0) {
                    return tree;
                }

                // mod = high - low + 1
                BigInteger mod = high.subtract(low).add(BigInteger.ONE);
                BigInteger res = arg.subtract(low).remainder(mod).add(low);

                int bitwidth = mod.bitLength();

                String binary = res.toString(2);

                if (binary.length() < bitwidth) {
                    int zerosNeeded = bitwidth - binary.length();
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < zerosNeeded; i++) {
                        sb.append('0');
                    }
                    binary = sb.toString() + binary;
                }

//                return LiteralNode.createNumericLiteralNode(res);
                return LiteralNode.getBVLiteral(binary);
            }
        }

        return tree;
    }
    public static InvariantTree simplifyConcat(InvariantTree tree){
        if (!(tree instanceof OperationNode)) {
            return tree;
        }

        OperationNode opNode = (OperationNode) tree;
        OpType type = opNode.getOpType();
        List<InvariantTree> children = opNode.getChildren();

        if (type != OpType.BVCONCAT || children.size() == 2) {
            List<InvariantTree> newChildren = new ArrayList<>(children.size());
            for (InvariantTree child : children) {
                newChildren.add(simplifyConcat(child));
            }

            return new OperationNode(type, newChildren, ((OperationNode) tree).getParams());
        }

        if (children.size() == 1) {
            return simplifyConcat(children.get(0));
        }
        int size = children.size();
        ArrayList<InvariantTree> simplifiedChildren = new ArrayList<>(size);
        for (InvariantTree child : children) {
            simplifiedChildren.add(simplifyConcat(child));
        }

        InvariantTree rightMost = simplifiedChildren.get(size - 1);

        for (int i = size - 2; i >= 0; i--) {
            ArrayList<InvariantTree> binaryChildren = new ArrayList<>();
            binaryChildren.add(simplifiedChildren.get(i));
            binaryChildren.add(rightMost);

            rightMost = new OperationNode(type, binaryChildren, opNode.getParams());
        }

        return rightMost;
    }
    private static BigInteger toBigInteger(Number num) {
        if (num instanceof BigInteger) {
            return (BigInteger) num;
        }
        // Works for Integer, Short, Byte, and Long
        return BigInteger.valueOf(num.longValue());
    }

    public static InvariantTree cleaner(InvariantTree tree){
        tree = clampModCast(tree);
        tree = simplifyConcat(tree);
        return tree;
    }

    public static ArrayList<VariableNode> getVariableNodes(InvariantTree tree) {
        Map<String, VariableNode> variables = new LinkedHashMap<String, VariableNode>();
        collectVariableNodes(tree, variables);
        return new ArrayList<VariableNode>(variables.values());
    }

    private static void collectVariableNodes(InvariantTree tree, Map<String, VariableNode> variables) {
        if (tree instanceof VariableNode) {
            VariableNode node = (VariableNode) tree;
            variables.put(node.getName() + "#" + node.getType().name(), node);
            return;
        }
        if (tree instanceof OperationNode) {
            for (InvariantTree child : ((OperationNode) tree).getChildren()) {
                collectVariableNodes(child, variables);
            }
        }
    }

    public static ParentedInvariantTree toParentedInvariantTree(InvariantTree tree) {
        return toParentedInvariantTree(tree, null, new IdentityHashMap<InvariantTree, ParentedInvariantTree>());
    }
    public static ParentedInvariantTree toParentedInvariantTree(InvariantTree tree, HashMap<String, StateValue> varStates) {
        return toParentedInvariantTree(tree, null,
                new IdentityHashMap<InvariantTree, ParentedInvariantTree>(), varStates);
    }

    public static InvariantTree fromParentedInvariantTree(ParentedInvariantTree tree) {
        return fromParentedInvariantTree(tree, new IdentityHashMap<ParentedInvariantTree, InvariantTree>());
    }

    public static InvariantTree toInvariantTree(ParentedInvariantTree tree) {
        return fromParentedInvariantTree(tree);
    }

    private static ParentedInvariantTree toParentedInvariantTree(
            InvariantTree tree,
            ParentedInvariantTree parent,
            Map<InvariantTree, ParentedInvariantTree> visited) {
        ParentedInvariantTree existing = visited.get(tree);
        if (existing != null) {
            if (parent != null) {
                existing.addParent(parent);
            }
            return existing;
        }

        ParentedInvariantTree converted;
        if (tree instanceof OperationNode) {
            OperationNode node = (OperationNode) tree;
            converted = ParentedInvariantTree.operation(
                    node.getOpType(),
                    new ArrayList<ParentedInvariantTree>(),
                    node.getParams());
            visited.put(tree, converted);
            if (parent != null) {
                converted.addParent(parent);
            }
            for (InvariantTree child : node.getChildren()) {
                converted.addChild(toParentedInvariantTree(child, converted, visited));
            }
            return converted;
        }
        if (tree instanceof VariableNode) {
            VariableNode node = (VariableNode) tree;
            converted = ParentedInvariantTree.variable(node.getName(), node.getType());
        } else if (tree instanceof LiteralNode) {
            LiteralNode node = (LiteralNode) tree;
            converted = ParentedInvariantTree.literal(node.getValue(), node.getType());
        } else {
            throw new IllegalArgumentException("Unsupported InvariantTree type: " + tree.getClass().getName());
        }

        visited.put(tree, converted);
        if (parent != null) {
            converted.addParent(parent);
        }
        return converted;
    }

    private static ParentedInvariantTree toParentedInvariantTree(
            InvariantTree tree,
            ParentedInvariantTree parent,
            Map<InvariantTree, ParentedInvariantTree> visited, HashMap<String, StateValue> varStates) {
        ParentedInvariantTree existing = visited.get(tree);
        if (existing != null) {
            if (parent != null) {
                existing.addParent(parent);
            }
            return existing;
        }

        ParentedInvariantTree converted;
        if (tree instanceof OperationNode) {
            OperationNode node = (OperationNode) tree;
            converted = ParentedInvariantTree.operation(
                    node.getOpType(),
                    new ArrayList<ParentedInvariantTree>(),
                    node.getParams());
            visited.put(tree, converted);
            if (parent != null) {
                converted.addParent(parent);
            }
            for (InvariantTree child : node.getChildren()) {
                converted.addChild(toParentedInvariantTree(child, converted, visited, varStates));
            }
//            converted.setParams(((OperationNode) tree).getParams());
            return converted;
        }
        if (tree instanceof VariableNode) {
            VariableNode node = (VariableNode) tree;
            StateValue state = varStates.get(node.getName());
            converted = ParentedInvariantTree.variable(node.getName(), node.getType(), state);
        } else if (tree instanceof LiteralNode) {
            LiteralNode node = (LiteralNode) tree;
            converted = ParentedInvariantTree.literal(node.getValue(), node.getType());
        } else {
            throw new IllegalArgumentException("Unsupported InvariantTree type: " + tree.getClass().getName());
        }

        visited.put(tree, converted);
        if (parent != null) {
            converted.addParent(parent);
        }
        return converted;
    }

    private static InvariantTree fromParentedInvariantTree(
            ParentedInvariantTree tree,
            Map<ParentedInvariantTree, InvariantTree> visited) {
        InvariantTree existing = visited.get(tree);
        if (existing != null) {
            return existing;
        }

        if (tree.getNodeType() == ParentedInvariantTree.NodeType.VARIABLE) {
            InvariantTree converted = new VariableNode(tree.getName(), tree.getType());
            visited.put(tree, converted);
            return converted;
        }
        if (tree.getNodeType() == ParentedInvariantTree.NodeType.LITERAL) {
            InvariantTree converted = new LiteralNode(tree.getValue(), tree.getType());
            visited.put(tree, converted);
            return converted;
        }

        List<InvariantTree> children = new ArrayList<InvariantTree>(tree.getChildren().size());
        for (ParentedInvariantTree child : tree.getChildren()) {
            children.add(fromParentedInvariantTree(child, visited));
        }
        InvariantTree converted = new OperationNode(
                tree.getOpType(),
                children,
                new ArrayList<Integer>(tree.getParams()));
        visited.put(tree, converted);
        return converted;
    }

    public static Number shrinkBigInteger(BigInteger valBI) {
        // Check if it fits in a 32-bit int
        if (valBI.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) >= 0 &&
                valBI.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) <= 0) {
            return valBI.intValue();
        }

        // Check if it fits in a 64-bit long
        if (valBI.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) >= 0 &&
                valBI.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) <= 0) {
            return valBI.longValue();
        }

        // Otherwise, BigInt
        return valBI;
    }

    public static String toJson(InvariantTree tree) {
        StringBuilder sb = new StringBuilder();
        appendTreeJson(sb, tree);
        return sb.toString();
    }

    public static InvariantTree fromJson(String json) {
        Object parsed = new JsonParser(json).parse();
        if (!(parsed instanceof Map)) {
            throw new IllegalArgumentException("InvariantTree JSON must be an object");
        }
        return treeFromJsonObject(asObject(parsed, "root"));
    }

    public static void writeJsonToFile(InvariantTree tree, Path path) throws IOException {
        Files.write(path, toJson(tree).getBytes(StandardCharsets.UTF_8));
    }

    public static void writeJsonToFile(InvariantTree tree, String path) throws IOException {
        writeJsonToFile(tree, java.nio.file.Paths.get(path));
    }

    public static InvariantTree readJsonFromFile(Path path) throws IOException {
        byte[] bytes = Files.readAllBytes(path);
        return fromJson(new String(bytes, StandardCharsets.UTF_8));
    }

    public static InvariantTree readJsonFromFile(String path) throws IOException {
        return readJsonFromFile(java.nio.file.Paths.get(path));
    }

    private static void appendTreeJson(StringBuilder sb, InvariantTree tree) {
        if (tree instanceof OperationNode) {
            appendOperationJson(sb, (OperationNode) tree);
            return;
        }
        if (tree instanceof VariableNode) {
            appendVariableJson(sb, (VariableNode) tree);
            return;
        }
        if (tree instanceof LiteralNode) {
            appendLiteralJson(sb, (LiteralNode) tree);
            return;
        }
        throw new IllegalArgumentException("Unsupported InvariantTree type: " + tree.getClass().getName());
    }

    private static void appendOperationJson(StringBuilder sb, OperationNode node) {
        sb.append('{');
        appendJsonField(sb, "node", "operation");
        sb.append(',');
        appendJsonField(sb, "opType", node.getOpType().name());
        sb.append(',');
        appendJsonString(sb, "params");
        sb.append(':');
        appendIntegerArrayJson(sb, node.getParams());
        sb.append(',');
        appendJsonString(sb, "children");
        sb.append(':');
        sb.append('[');
        for (int i = 0; i < node.getChildren().size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            appendTreeJson(sb, node.getChildren().get(i));
        }
        sb.append(']');
        sb.append('}');
    }

    private static void appendVariableJson(StringBuilder sb, VariableNode node) {
        sb.append('{');
        appendJsonField(sb, "node", "variable");
        sb.append(',');
        appendJsonField(sb, "name", node.getName());
        sb.append(',');
        appendJsonField(sb, "varType", node.getType().name());
        sb.append('}');
    }

    private static void appendLiteralJson(StringBuilder sb, LiteralNode node) {
        Object value = node.getValue();
        sb.append('{');
        appendJsonField(sb, "node", "literal");
        sb.append(',');
        appendJsonField(sb, "varType", node.getType().name());
        sb.append(',');
        appendJsonField(sb, "valueType", getJsonValueType(value));
        sb.append(',');
        appendJsonString(sb, "value");
        sb.append(':');
        appendValueJson(sb, value);
        sb.append('}');
    }

    private static InvariantTree treeFromJsonObject(Map<String, Object> json) {
        String nodeType = requireString(json, "node");
        if ("operation".equals(nodeType)) {
            OpType opType = OpType.valueOf(requireString(json, "opType"));
            List<InvariantTree> children = new ArrayList<InvariantTree>();
            for (Object child : requireArray(json, "children")) {
                children.add(treeFromJsonObject(asObject(child, "child")));
            }
            List<Integer> params = new ArrayList<Integer>();
            Object rawParams = json.get("params");
            if (rawParams != null) {
                for (Object param : asArray(rawParams, "params")) {
                    params.add(asInt(param, "params"));
                }
            }
            return new OperationNode(opType, children, params);
        }
        if ("variable".equals(nodeType)) {
            return new VariableNode(requireString(json, "name"), VarType.valueOf(requireString(json, "varType")));
        }
        if ("literal".equals(nodeType)) {
            VarType varType = VarType.valueOf(requireString(json, "varType"));
            Object value = valueFromJson(json.get("value"), optionalString(json, "valueType"));
            return new LiteralNode(value, varType);
        }
        throw new IllegalArgumentException("Unknown node type: " + nodeType);
    }

    private static Object valueFromJson(Object value, String valueType) {
        if ("BigInteger".equals(valueType)) {
            return new BigInteger(asString(value, "value"));
        }
        if ("Long".equals(valueType)) {
            return Long.valueOf(asString(value, "value"));
        }
        if ("Integer".equals(valueType)) {
            return Integer.valueOf(asString(value, "value"));
        }
        if ("Float".equals(valueType)) {
            return Float.valueOf(asString(value, "value"));
        }
        if ("Double".equals(valueType)) {
            return Double.valueOf(asString(value, "value"));
        }
        if ("Map".equals(valueType)) {
            return decodeJsonMap(asObject(value, "value"));
        }
        return value;
    }

    private static String getJsonValueType(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof BigInteger) {
            return "BigInteger";
        }
        if (value instanceof Long) {
            return "Long";
        }
        if (value instanceof Integer) {
            return "Integer";
        }
        if (value instanceof Float) {
            return "Float";
        }
        if (value instanceof Double) {
            return "Double";
        }
        if (value instanceof Boolean) {
            return "Boolean";
        }
        if (value instanceof Map) {
            return "Map";
        }
        return "String";
    }

    private static void appendIntegerArrayJson(StringBuilder sb, List<Integer> values) {
        sb.append('[');
        if (values != null) {
            for (int i = 0; i < values.size(); i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(values.get(i));
            }
        }
        sb.append(']');
    }

    private static void appendValueJson(StringBuilder sb, Object value) {
        if (value == null) {
            sb.append("null");
        } else if (value instanceof Boolean) {
            sb.append(value);
        } else if (value instanceof Integer || value instanceof Long) {
            sb.append(value);
        } else if (value instanceof Float || value instanceof Double) {
            appendJsonString(sb, value.toString());
        } else if (value instanceof BigInteger) {
            appendJsonString(sb, value.toString());
        } else if (value instanceof Map) {
            appendMapJson(sb, (Map<?, ?>) value);
        } else {
            appendJsonString(sb, value.toString());
        }
    }

    private static void appendMapJson(StringBuilder sb, Map<?, ?> map) {
        sb.append('{');
        int i = 0;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (i > 0) {
                sb.append(',');
            }
            appendJsonString(sb, String.valueOf(entry.getKey()));
            sb.append(':');
            appendTypedValueJson(sb, entry.getValue());
            i++;
        }
        sb.append('}');
    }

    private static void appendTypedValueJson(StringBuilder sb, Object value) {
        sb.append('{');
        appendJsonField(sb, "valueType", getJsonValueType(value));
        sb.append(',');
        appendJsonString(sb, "value");
        sb.append(':');
        appendValueJson(sb, value);
        sb.append('}');
    }

    private static Map<String, Object> decodeJsonMap(Map<String, Object> encodedMap) {
        Map<String, Object> decoded = new LinkedHashMap<String, Object>();
        for (Map.Entry<String, Object> entry : encodedMap.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                Map<String, Object> encodedValue = asObject(value, entry.getKey());
                if (encodedValue.containsKey("valueType") && encodedValue.containsKey("value")) {
                    decoded.put(entry.getKey(), valueFromJson(
                            encodedValue.get("value"),
                            optionalString(encodedValue, "valueType")));
                    continue;
                }
            }
            decoded.put(entry.getKey(), value);
        }
        return decoded;
    }

    private static void appendJsonField(StringBuilder sb, String name, String value) {
        appendJsonString(sb, name);
        sb.append(':');
        appendJsonString(sb, value);
    }

    private static void appendJsonString(StringBuilder sb, String value) {
        sb.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
    }

    private static String requireString(Map<String, Object> object, String field) {
        if (!object.containsKey(field)) {
            throw new IllegalArgumentException("Missing field: " + field);
        }
        return asString(object.get(field), field);
    }

    private static String optionalString(Map<String, Object> object, String field) {
        Object value = object.get(field);
        return value == null ? null : asString(value, field);
    }

    private static String asString(Object value, String field) {
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        throw new IllegalArgumentException("Expected string for field: " + field);
    }

    private static int asInt(Object value, String field) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(asString(value, field));
    }

    private static List<Object> requireArray(Map<String, Object> object, String field) {
        if (!object.containsKey(field)) {
            throw new IllegalArgumentException("Missing field: " + field);
        }
        return asArray(object.get(field), field);
    }

    @SuppressWarnings("unchecked")
    private static List<Object> asArray(Object value, String field) {
        if (value instanceof List) {
            return (List<Object>) value;
        }
        throw new IllegalArgumentException("Expected array for field: " + field);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asObject(Object value, String field) {
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        throw new IllegalArgumentException("Expected object for field: " + field);
    }

    private static class JsonParser {
        private final String json;
        private int index;

        JsonParser(String json) {
            this.json = json;
        }

        Object parse() {
            Object value = parseValue();
            skipWhitespace();
            if (index != json.length()) {
                throw new IllegalArgumentException("Unexpected trailing JSON at index " + index);
            }
            return value;
        }

        private Object parseValue() {
            skipWhitespace();
            if (index >= json.length()) {
                throw new IllegalArgumentException("Unexpected end of JSON");
            }
            char c = json.charAt(index);
            if (c == '{') {
                return parseObject();
            }
            if (c == '[') {
                return parseArray();
            }
            if (c == '"') {
                return parseString();
            }
            if (c == 't') {
                readLiteral("true");
                return Boolean.TRUE;
            }
            if (c == 'f') {
                readLiteral("false");
                return Boolean.FALSE;
            }
            if (c == 'n') {
                readLiteral("null");
                return null;
            }
            if (c == '-' || Character.isDigit(c)) {
                return parseNumber();
            }
            throw new IllegalArgumentException("Unexpected JSON value at index " + index);
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> object = new LinkedHashMap<String, Object>();
            index++;
            skipWhitespace();
            if (peek('}')) {
                index++;
                return object;
            }
            while (true) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                expect(':');
                object.put(key, parseValue());
                skipWhitespace();
                if (peek('}')) {
                    index++;
                    return object;
                }
                expect(',');
            }
        }

        private List<Object> parseArray() {
            List<Object> array = new ArrayList<Object>();
            index++;
            skipWhitespace();
            if (peek(']')) {
                index++;
                return array;
            }
            while (true) {
                array.add(parseValue());
                skipWhitespace();
                if (peek(']')) {
                    index++;
                    return array;
                }
                expect(',');
            }
        }

        private String parseString() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (index < json.length()) {
                char c = json.charAt(index++);
                if (c == '"') {
                    return sb.toString();
                }
                if (c == '\\') {
                    if (index >= json.length()) {
                        throw new IllegalArgumentException("Unterminated JSON escape");
                    }
                    char escaped = json.charAt(index++);
                    switch (escaped) {
                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case '/': sb.append('/'); break;
                        case 'b': sb.append('\b'); break;
                        case 'f': sb.append('\f'); break;
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;
                        case 'u':
                            if (index + 4 > json.length()) {
                                throw new IllegalArgumentException("Invalid JSON unicode escape");
                            }
                            sb.append((char) Integer.parseInt(json.substring(index, index + 4), 16));
                            index += 4;
                            break;
                        default:
                            throw new IllegalArgumentException("Invalid JSON escape: \\" + escaped);
                    }
                } else {
                    sb.append(c);
                }
            }
            throw new IllegalArgumentException("Unterminated JSON string");
        }

        private Number parseNumber() {
            int start = index;
            if (peek('-')) {
                index++;
            }
            while (index < json.length() && Character.isDigit(json.charAt(index))) {
                index++;
            }
            if (index < json.length() && json.charAt(index) == '.') {
                index++;
                while (index < json.length() && Character.isDigit(json.charAt(index))) {
                    index++;
                }
                return Double.valueOf(json.substring(start, index));
            }
            String number = json.substring(start, index);
            try {
                return Integer.valueOf(number);
            } catch (NumberFormatException ignored) {
                try {
                    return Long.valueOf(number);
                } catch (NumberFormatException ignoredAgain) {
                    return new BigInteger(number);
                }
            }
        }

        private void readLiteral(String literal) {
            if (!json.startsWith(literal, index)) {
                throw new IllegalArgumentException("Expected " + literal + " at index " + index);
            }
            index += literal.length();
        }

        private void expect(char expected) {
            skipWhitespace();
            if (index >= json.length() || json.charAt(index) != expected) {
                throw new IllegalArgumentException("Expected '" + expected + "' at index " + index);
            }
            index++;
        }

        private boolean peek(char expected) {
            return index < json.length() && json.charAt(index) == expected;
        }

        private void skipWhitespace() {
            while (index < json.length() && Character.isWhitespace(json.charAt(index))) {
                index++;
            }
        }
    }

    public static List<String> convertFloatBitmaskToIntervals(FloatingPointLiteralValue fp){

        List<String> out = PythonBridge.run("BitmasksToExactDoubleIntervals_v2",
                String.valueOf(fp.getExponent().getValueStr()),
                String.valueOf(fp.getExponent().getMaskStr()),
                String.valueOf(fp.getMantissa().getValueStr()),
                String.valueOf(fp.getMantissa().getMaskStr()),
                String.valueOf(fp.getSign().getValue()),
                String.valueOf(fp.getSign().getMask())
        );
        return out;

    }
}
