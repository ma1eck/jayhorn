package jayhorn.phaseOneParser.LiteralValues;

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

    @Override
    public FloatingPointLiteralValue copy() {
        return new FloatingPointLiteralValue(sign.copy(), exponent.copy(), mantissa.copy());
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
}
