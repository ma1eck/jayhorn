package jayhorn.phaseOneParser.LiteralValues;

public class FloatingPointLiteralValue implements StateValue {
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

    public BoolLiteralValue getSign() {
        return sign;
    }

    public BVLiteralValue getExponent() {
        return exponent;
    }

    public BVLiteralValue getMantissa() {
        return mantissa;
    }
}
