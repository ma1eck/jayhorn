package jayhorn.phaseOneParser.LiteralValues;

import soottocfg.cfg.expression.literal.BooleanLiteral;

public class BoolLiteralValue implements StateValue {
    public GBool state;

    public BoolLiteralValue(GBool state){
        this.state = state;
    }
    public BoolLiteralValue(boolean b){
        if (b)
            this.state = GBool.TRUE;
        else
            this.state = GBool.FALSE;
    }
    public BoolLiteralValue(){
        this.state = GBool.UNKNOWN;
    }

    public Boolean getMask(){
        switch (state){
            case UNKNOWN:
                return false;
            case FALSE:
            case TRUE:
                return true;
            default:
                return null;
        }
    }
    public Boolean getValue(){
        switch (state){
            case UNKNOWN:
            case FALSE:
                return false;
            case TRUE:
                return true;
            default:
                return null;
        }
    }

    public void setState(GBool state){
        this.state = state;
    }

    public void setState(boolean b){
        if (b)
            this.state = GBool.TRUE;
        else
            this.state = GBool.FALSE;
    }

    public void negate(){
        switch (state){
            case FALSE:
                this.state = GBool.TRUE;
                break;
            case TRUE:
                this.state = GBool.FALSE;
        }
    }
    public GBool getNegate(){
        switch (state){
            case FALSE:
                return GBool.TRUE;
            case TRUE:
                return GBool.FALSE;
        }
        return GBool.UNKNOWN;
    }

    public boolean isTrue(){
        return state.equals(GBool.TRUE);
    }
    public boolean isFalse(){
        return state.equals(GBool.FALSE);
    }
    public boolean isUnknown(){
        return state.equals(GBool.UNKNOWN);
    }

    @Override
    public BoolLiteralValue copy() {
        return new BoolLiteralValue(state);
    }

//    @Override
    public boolean intersect(StateValue other) {
        if (other instanceof BoolLiteralValue){
            BoolLiteralValue otherB = (BoolLiteralValue) other;
            if (this.isUnknown()) {
                setState(otherB.state);
                return true;
            }if (otherB.isUnknown()){
                return true;
            }if (this.state != otherB.state) return false;
            return true;
        }
        return false;
    }
    @Override
    public boolean union(StateValue other) {
        if (other instanceof BoolLiteralValue){
            BoolLiteralValue otherB = (BoolLiteralValue) other;
            if (this.isUnknown() || otherB.isUnknown() || this.state != otherB.state) {
                setState(GBool.UNKNOWN);
                return true;
            }
            return true;
        }
        return false;
    }
}
