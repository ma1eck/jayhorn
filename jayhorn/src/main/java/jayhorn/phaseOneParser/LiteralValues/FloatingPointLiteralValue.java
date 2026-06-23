package jayhorn.phaseOneParser.LiteralValues;

import jayhorn.AST.ASTHelper;

import java.util.List;

public class FloatingPointLiteralValue implements StateValue {
    // If a field is null, it means this object doesn't hold that piece of data yet.

    public BoolLiteralValue sign;
    public BVLiteralValue exponent;
    public BVLiteralValue mantissa;

    public FloatingPointLiteralValue(BoolLiteralValue sign, BVLiteralValue exponent, BVLiteralValue mantissa){
        this.sign = sign;
        this.exponent = exponent;
        this.mantissa = mantissa;
    }
    public FloatingPointLiteralValue(int exponentArity, int mantissaArity){
        this.sign = new BoolLiteralValue();
        this.exponent = new BVLiteralValue(exponentArity);
        this.mantissa = new BVLiteralValue(mantissaArity);
    }
    public static FloatingPointLiteralValue createSignOnly(BoolLiteralValue sign) {
        return new FloatingPointLiteralValue(sign, null, null);
    }

    public static FloatingPointLiteralValue createExponentOnly(BVLiteralValue exponent) {
        return new FloatingPointLiteralValue(null, exponent, null);
    }

    public static FloatingPointLiteralValue createMantissaOnly(BVLiteralValue mantissa) {
        return new FloatingPointLiteralValue(null, null, mantissa);
    }

    public BoolLiteralValue getSign() {
        return sign;
    }

    public BVLiteralValue getExponent() {
        return exponent;
    }

    public BVLiteralValue getMantissa() {
        return mantissa;
    }

    public boolean isUnknown(){
        return sign.isUnknown() || exponent.isUnknown() || mantissa.isUnknown();
    }

    @Override
    public FloatingPointLiteralValue copy() {
        BoolLiteralValue sign_copy = null;
        if (sign != null){
            sign_copy = sign.copy();
        }
        BVLiteralValue exponent_copy = null;
        if (exponent != null){
            exponent_copy = exponent.copy();
        }
        BVLiteralValue mantissa_copy = null;
        if (mantissa != null){
            mantissa_copy = mantissa.copy();
        }

        return new FloatingPointLiteralValue(sign_copy, exponent_copy, mantissa_copy);
    }

    @Override
    public boolean union(StateValue other) {
        if (!(other instanceof FloatingPointLiteralValue)) return false;
        FloatingPointLiteralValue otherFP = (FloatingPointLiteralValue) other;

        boolean wasAble = true;
        if (this.sign != null && otherFP.sign != null) {
            wasAble &= this.sign.union(otherFP.sign);
        } else if (this.sign == null && otherFP.sign != null) {
            this.sign = otherFP.sign; // Take the sign from the other
        }

        if (this.exponent != null && otherFP.exponent != null) {
            wasAble &= this.exponent.union(otherFP.exponent);
        } else if (this.exponent == null && otherFP.exponent != null) {
            this.exponent = otherFP.exponent;
        }
        if (this.mantissa != null && otherFP.mantissa != null) {
            wasAble &= this.mantissa.union(otherFP.mantissa);
        } else if (this.mantissa == null && otherFP.mantissa != null) {
            this.mantissa = otherFP.mantissa;
        }
        return wasAble;
    }

    @Override
    public boolean intersect(StateValue other) {
        if (!(other instanceof FloatingPointLiteralValue)) return false;
        FloatingPointLiteralValue otherFP = (FloatingPointLiteralValue) other;

        boolean wasAble = true;
        if (this.sign != null && otherFP.sign != null) {
            wasAble &= this.sign.intersect(otherFP.sign);
        } else if (this.sign == null && otherFP.sign != null) {
            this.sign = otherFP.sign; // Take the sign from the other
        }

        if (this.exponent != null && otherFP.exponent != null) {
            wasAble &= this.exponent.intersect(otherFP.exponent);
        } else if (this.exponent == null && otherFP.exponent != null) {
            this.exponent = otherFP.exponent;
        }
        if (this.mantissa != null && otherFP.mantissa != null) {
            wasAble &= this.mantissa.intersect(otherFP.mantissa);
        } else if (this.mantissa == null && otherFP.mantissa != null) {
            this.mantissa = otherFP.mantissa;
        }
        return wasAble;
    }

    @Override
    public String toString() {
        return "FloatingPointLiteralValue{" +
                "sign=" + sign +
                ", exponent=" + exponent +
                ", mantissa=" + mantissa +
                "} (" + getRangeFormat() + ")";

    }

    public void setSign(BoolLiteralValue sign) {
        this.sign = sign;
    }

    public void setExponent(BVLiteralValue exponent) {
        this.exponent = exponent;
    }

    public void setMantissa(BVLiteralValue mantissa) {
        this.mantissa = mantissa;
    }

    public String getRangeFormat(){
        FloatingPointLiteralValue copy = this.copy();
        if (copy.sign == null) copy.setSign(new BoolLiteralValue());
        if (copy.exponent == null) copy.setExponent(new BVLiteralValue(11));
        if (copy.mantissa == null) copy.setMantissa(new BVLiteralValue(53));
        List<String> outputs = ASTHelper.convertFloatBitmaskToIntervals(copy);

        String message = outputs.get(outputs.size()-1);
        StringBuilder sb = new StringBuilder();
        sb.append(message).append(": ");
        for (int i=0; i<outputs.size()-1; i++){
            if (i % 2 == 0){
                sb.append("["); sb.append(outputs.get(i)); sb.append(", ");
            }else {
                sb.append(outputs.get(i)); sb.append("]");
                if (i!=outputs.size()-2) sb.append(" + ");
            }
        }

        return sb.toString();
    }
}
