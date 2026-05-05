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

}
