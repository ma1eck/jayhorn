package jayhorn.phaseOneParser.LiteralValues;

import com.google.common.collect.Range;
import scala.Int;

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
    public void exclude(int start, int end) {
        this.state.exclude(start, end);
    }

    public Boolean hasOverlap(IntLiteralValue other){
        return this.state.hasOverlap(other.state);
    }


    public IntLiteralValue copy() {
        return new IntLiteralValue(state.copy());
    }

    @Override
    public boolean union(StateValue other) {
        if (other instanceof IntLiteralValue){
            state.union(((IntLiteralValue) other).state.getRangeSet());
            return true;
        }
        return false;
    }

    @Override
    public boolean intersect(StateValue other) {
        if (other instanceof IntLiteralValue){
            state.intersect(((IntLiteralValue) other).state.getRangeSet());
            return true;
        }
        return false;
    }
}
