package jayhorn.phaseOneParser.LiteralValues.SingularFPValue;

import jayhorn.phaseOneParser.LiteralValues.BVLiteralValue;
import jayhorn.phaseOneParser.LiteralValues.BoolLiteralValue;
import jayhorn.phaseOneParser.LiteralValues.FloatingPointLiteralValue;

public class ExponentLiteralValue extends FloatingPointLiteralValue {
    public ExponentLiteralValue(BVLiteralValue exponent) {
        super(sign, exponent, mantissa); ??
    }
}
