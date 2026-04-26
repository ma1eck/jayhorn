package jayhorn.AST;

public class InvariantTree {
    private final String name;
    private final InvariantTree[] arguments;

    public InvariantTree(String name, InvariantTree[] arguments) {
        this.name = name;
        this.arguments = arguments != null ? arguments.clone() : new InvariantTree[0];
    }

    public String getName() {
        return name;
    }

    public InvariantTree[] getArguments() {
        return arguments.clone();
    }

    public boolean isLeaf() {
        return arguments.length == 0;
    }

    @Override
    public String toString() {
        if (arguments.length == 0) {
            return name;
        }

        StringBuilder sb = new StringBuilder(name + "(");
        for (int i = 0; i < arguments.length; i++) {
            sb.append(arguments[i]);
            if (i < arguments.length - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }
}

