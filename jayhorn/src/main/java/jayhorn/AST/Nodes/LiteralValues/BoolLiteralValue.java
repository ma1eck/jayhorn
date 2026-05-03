package jayhorn.AST.Nodes.LiteralValues;

public class BoolLiteralValue {
    public GBool state;

    public BoolLiteralValue(GBool state){
        this.state = state;
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

    public void negate(){
        switch (state){
            case FALSE:
                this.state = GBool.TRUE;
                break;
            case TRUE:
                this.state = GBool.FALSE;
        }
    }


}
