package jayhorn.solver.spacer;

import jayhorn.solver.*;
import jayhorn.solver.princess.PrincessFloatingPointType;
import com.microsoft.z3.Context;

public class SpacerFloatingPointADTFactory implements FloatingPointADTFactory {

    private final Context ctx;

    public SpacerFloatingPointADTFactory(Context ctx) {
        this.ctx = ctx;
    }


    @Override
    public ProverADT spawnFloatingPointADT(PrincessFloatingPointType.Precision precision) {
        // Determine bit widths based on precision
        boolean isSingle = (precision == PrincessFloatingPointType.Precision.Single);
        int exponentWidth = isSingle ? 8 : 11;
        int mantissaWidth = isSingle ? 24 : 53;

        ProverType exponentType = new BitVectorType(exponentWidth);
        ProverType mantissaType = new BitVectorType(mantissaWidth);


        String typeName = isSingle ? "FloatingPoint" : "DoubleFloatingPoint";

        return SpacerADT.mkSimpleADT(
                ctx,
                typeName,
                typeName,
                new ProverType[]{
                        BoolType.INSTANCE,    // sign
                        exponentType,         // exponent
                        mantissaType,         // mantissa
//                        BoolType.INSTANCE,    // isNan
//                        BoolType.INSTANCE,    // isInfinity
//                        BoolType.INSTANCE,    // OVF (overflow)
//                        BoolType.INSTANCE     // UDF (underflow)
                },
                new String[]{"sign", "exponent", "mantissa",/* "isNan", "isInfinity", "OVF", "UDF"*/}
        );
    }
}