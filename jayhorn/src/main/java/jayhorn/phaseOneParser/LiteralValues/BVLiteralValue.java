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
            value.add(blv.getValue());
        }
        return value;
    }
    public String getMaskStr(){
        ArrayList<Boolean> mask = getMask();
        StringBuilder sb = new StringBuilder(mask.size());
        for (int i = mask.size() - 1; i >= 0; i--) {
            sb.append(mask.get(i) ? '1' : '0');
        }
        return sb.toString();
    }
    public String getValueStr(){
        ArrayList<Boolean> value = getValue();
        StringBuilder sb = new StringBuilder(value.size());
        for (int i = value.size() - 1; i >= 0; i--) {
            sb.append(value.get(i) ? '1' : '0');
        }

        return sb.toString();
    }

    public boolean isConcrete(){
        for (BoolLiteralValue bit:
             state) {
           if (bit.isUnknown()) return false;
        }
        return true;
    }

    public void setBit(int index, GBool bit){
        state.get(index).setState(bit);
    }

    public static BVLiteralValue mkBVLiteralValue(String value, String mask){
        int arity = mask.length();
        ArrayList<BoolLiteralValue> boolLiteralList = new ArrayList<>();
        for (int i = arity-1; i >= 0 ; i--) {
            char vi = value.charAt(i);
            char mi = mask.charAt(i);
            if (mi == '0') {
                boolLiteralList.add(new BoolLiteralValue(GBool.UNKNOWN));
            }else if (vi == '1'){
                boolLiteralList.add(new BoolLiteralValue(GBool.TRUE));
            }else {
                boolLiteralList.add(new BoolLiteralValue(GBool.FALSE));
            }
        }
        return new BVLiteralValue(boolLiteralList);
    }

    @Override
    public BVLiteralValue copy() {
        ArrayList<BoolLiteralValue> newList = new ArrayList<>(state.size());
        for (BoolLiteralValue bit : state) {
            newList.add(bit.copy());
        }
        return new BVLiteralValue(newList);
    }

    @Override
    public boolean union(StateValue other) {
        if (other instanceof BVLiteralValue){
            BVLiteralValue otherBV = (BVLiteralValue) other;
            if (otherBV.state.size() != this.state.size()) return false;
            int i = 0;
            boolean wasAble = true;
            for (BoolLiteralValue bit: state){
                wasAble = wasAble && bit.union(otherBV.state.get(i));
                i++;
            }
            return wasAble;

        }return false;
    }
    @Override
    public boolean intersect(StateValue other) {
        if (other instanceof BVLiteralValue){
            BVLiteralValue otherBV = (BVLiteralValue) other;
            if (otherBV.state.size() != this.state.size()) return false;
            int i = 0;
            boolean wasAble = true;
            for (BoolLiteralValue bit: state){
                wasAble = wasAble && bit.intersect(otherBV.state.get(i));
                i++;
            }
            return wasAble;

        }return false;
    }

    @Override
    public boolean isUnknown() {
        boolean r = false;
        for (BoolLiteralValue bit: state){
            r = r || bit.isUnknown();
        }
        return r;
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        for (BoolLiteralValue bit : state){
            st.append(bit.toString());
        }
        return st.reverse().toString();
    }
}
