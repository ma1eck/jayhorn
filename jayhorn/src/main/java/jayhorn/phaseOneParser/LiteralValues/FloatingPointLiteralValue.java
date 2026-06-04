package jayhorn.phaseOneParser.LiteralValues;

public class FloatingPointLiteralValue implements StateValue {
    public BoolLiteralValue sign;
    public BVLiteralValue exponent;
    public BVLiteralValue mantissa;
    private final boolean singular; // used when you only want to store data for exponent, mantissa or sign.
    // don't use this. use polymorphism

    public FloatingPointLiteralValue(BoolLiteralValue sign, BVLiteralValue exponent, BVLiteralValue mantissa){
        this.sign = sign;
        this.exponent = exponent;
        this.mantissa = mantissa;
        this.singular = false;
    }
    public FloatingPointLiteralValue(int exponentArity, int mantissaArity){
        this.sign = new BoolLiteralValue();
        this.exponent = new BVLiteralValue(exponentArity);
        this.mantissa = new BVLiteralValue(mantissaArity);
        this.singular = false;
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
        return false;
    }

    @Override
    public boolean intersect(StateValue other) {
        return false;
    }
}
