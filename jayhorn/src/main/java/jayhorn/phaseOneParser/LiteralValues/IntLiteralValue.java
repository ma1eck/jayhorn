package jayhorn.phaseOneParser.LiteralValues;

public class IntLiteralValue implements StateValue {
    public ValidIntegerRange state;

    public IntLiteralValue(ValidIntegerRange state){
        this.state = state;
    }
    public IntLiteralValue(int point){
        this.state = new ValidIntegerRange(point);
    }
    public IntLiteralValue(){
        this.state = new ValidIntegerRange();
    }
    public IntLiteralValue(int start, int end){
        this.state = new ValidIntegerRange(start, end);
    }

    public Integer getMinValue(){
        return this.state.getMinValue();
    }
    public Integer getMaxValue(){
        return this.state.getMaxValue();
    }

    public void union(int start, int end) {
        this.state.union(start, end);
    }
    public void intersect(int start, int end) {
        this.state.intersect(start, end);
    }

    public Boolean hasOverlap(IntLiteralValue other){
        return this.state.hasOverlap(other.state);
    }


}
