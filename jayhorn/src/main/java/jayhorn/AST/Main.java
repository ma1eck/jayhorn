package jayhorn.AST;

import jayhorn.AST.Nodes.*;
import jayhorn.phaseOneParser.LiteralValues.FloatingPointLiteralValue;
import jayhorn.phaseOneParser.LiteralValues.IntLiteralValue;
import jayhorn.phaseOneParser.ParentedInvariantTree;
import jayhorn.phaseOneParser.PhaseOne;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static jayhorn.AST.Nodes.OperationNode.*;

public class Main {
//    (or
//	     (not (= b2_99 1))
//         (not (= (exponent d0_100_3)
//                 #b10000000001))
//         (not (bvule #b10100000000000000000000000000000000000000000000000000
//                     ((_ extract 157 105)
//                       (bvadd #b000010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
//                              (concat #b0
//                                      (mantissa d0_100_3)
//                                      #b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000))))
//		  )
//         (= ((_ extract 157 105)
//              (bvadd #b000010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
//                     (concat #b0
//                             (mantissa d0_100_3)
//                             #b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000)))
//            #b10100000000000000000000000000000000000000000000000000)
//	  )
    private static InvariantTree test1(){
        VariableNode b2_99 = new VariableNode("b2_99", VarType.INTEGER);
        LiteralNode one = LiteralNode.getIntLiteral(1);

        VariableNode d0_100_3 = new VariableNode("d0_100_3", VarType.DOUBLE);
        LiteralNode b10000000001 = LiteralNode.getBVLiteral("#b10000000001");

        LiteralNode b10100000000000000000000000000000000000000000000000000 = LiteralNode.getBVLiteral(
                "b10100000000000000000000000000000000000000000000000000");
        LiteralNode b000010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
                = LiteralNode.getBVLiteral(
                "b000010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        LiteralNode b0 = LiteralNode.getBVLiteral("b0");
        LiteralNode b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000 =
                LiteralNode.getBVLiteral("b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");


        InvariantTree or = mkOr(mkNot(mkEq(b2_99, one)),
                mkNot(mkEq(mkFPExponent(d0_100_3),b10000000001)),
                mkNot(mkBvule(b10100000000000000000000000000000000000000000000000000,
                        mkExtract(157, 105,
                                mkBVAdd(b000010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000,
                                        mkConcat(b0, mkFPMantissa(d0_100_3),
                                                b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000)))
                )),
                mkEq(mkExtract(157, 105,
                                mkBVAdd(b000010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000,
                                        mkConcat(b0, mkFPMantissa(d0_100_3),
                                                b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000))),
                        b10100000000000000000000000000000000000000000000000000)
        );
        return or;
    }
    public static void main(String[] args) throws IOException {
        InvariantTree t1 = test1();

        ParentedInvariantTree pt1 = PhaseOne.parse(t1);

        ((FloatingPointLiteralValue) pt1.getChildren().get(1).getChildren().get(0).getChildren().get(0).getChildren()
                .get(0).getStateValue()).getSign().setState(true);
        System.out.println(t1.toPrettyString());
//        ASTHelper.writeJsonToFile(t1, "t1");
//        InvariantTree t1_ = ASTHelper.readJsonFromFile("t1");
//        System.out.println(t1_.toString().equals(t1.toString()));
    }

}
