package jayhorn.phaseOneParser.LiteralValues;

public interface StateValue {
    StateValue copy();

    boolean union(StateValue other);
    boolean intersect(StateValue other);
    boolean isUnknown();
}
