package jayhorn.phaseOneParser.LiteralValues;

import java.util.ArrayList;

public class BVLiteralValue implements StateValue {
    public ArrayList<BoolLiteralValue> state;

    public BVLiteralValue(ArrayList<BoolLiteralValue> state){
        this.state = state;
    }

    public BVLiteralValue(int arity){
        state = new ArrayList<>();
        for (int i = 0; i < arity; i++) {
            state.add(new BoolLiteralValue());
        }
    }

    public ArrayList<Boolean> getMask(){
        ArrayList<Boolean> mask = new ArrayList<>();
        for (BoolLiteralValue blv: state) {
            mask.add(blv.getMask());
        }
        return mask;
    }    
    public ArrayList<Boolean> getValue(){
        ArrayList<Boolean> value = new ArrayList<>();
        for (BoolLiteralValue blv: state) {
            value.add(blv.getMask());
        }
        return value;
    }

    public void setBit(int index, GBool bit){
        state.get(index).setState(bit);
    }

}
