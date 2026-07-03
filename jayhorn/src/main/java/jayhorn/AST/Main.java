package jayhorn.AST;

import com.microsoft.z3.AST;
import jayhorn.AST.Nodes.*;
import jayhorn.Options;
import jayhorn.phaseOneParser.LiteralValues.FloatingPointLiteralValue;
import jayhorn.phaseOneParser.ParentedInvariantTree;
import jayhorn.phaseOneParser.PhaseOne;
import jayhorn.phaseTwoParser.PhaseTwo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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
    private static InvariantTree eg1_left_side(){
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

    private static InvariantTree test1_1(){
        VariableNode b2_99 = new VariableNode("b2_99", VarType.INTEGER);
        LiteralNode one = LiteralNode.getIntLiteral(1);
        LiteralNode two = LiteralNode.getIntLiteral(2);

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


        InvariantTree or = mkOr(
                mkNot(mkEq(two, one)),
                mkNot(mkEq(b2_99, one)),
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
    private static InvariantTree test2(){
//        VariableNode b2_99 = new VariableNode("b2_99", VarType.INTEGER);
//        LiteralNode one = LiteralNode.getIntLiteral(1);
//        LiteralNode two = LiteralNode.getIntLiteral(2);

        LiteralNode b10000000001 = LiteralNode.getBVLiteral("#b10000000001");

        LiteralNode b10100000000000000000000000000000000000000000000000000 = LiteralNode.getBVLiteral(
                "b10100000000000000000000000000000000000000000000000000");
        LiteralNode b000010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
                = LiteralNode.getBVLiteral(
                "b000010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        LiteralNode b0 = LiteralNode.getBVLiteral("b0");
        LiteralNode b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000 =
                LiteralNode.getBVLiteral("b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");


        InvariantTree result = mkConcat(b10100000000000000000000000000000000000000000000000000, b10000000001);
        return result;
    }
    private static InvariantTree test3(){

        VariableNode d0_100_3 = new VariableNode("d0_100_3", VarType.DOUBLE);

        LiteralNode b000010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
                = LiteralNode.getBVLiteral(
                "b000010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        LiteralNode b0 = LiteralNode.getBVLiteral("b0");
        LiteralNode b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000 =
                LiteralNode.getBVLiteral("b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");


        InvariantTree result = mkBVAdd(b000010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000,
                mkConcat(b0, mkFPMantissa(d0_100_3),
                        b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000));
        return result;
    }


//    (or (not (= b2_99 1))
//         ((_ bit2bool 50)
//           (mantissa d0_100_3))
//         (not (= ((_ extract 10 10)
//                   (exponent d0_100_3))
//                 #b1))
//         (= ((_ extract 157 105)
//              (bvadd #b000010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
//                     (concat #b0
//                             ((_ extract 158 106)
//                               (bvadd #b001000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
//                                      (concat #b0
//                                              ((_ extract 157 105)
//                                                (bvadd #b000100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
//                                                       (concat #b0
//                                                               (mantissa d0_100_3)
//                                                               #b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000)))
//                                              #b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000)))
//                             #b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000)))
//            #b10100000000000000000000000000000000000000000000000000)
//         (= ((_ extract 158 158)
//              (bvadd #b001000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
//                     (concat #b0
//                             ((_ extract 157 105)
//                               (bvadd #b000100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
//                                      (concat #b0
//                                              (mantissa d0_100_3)
//                                              #b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000)))
//                             #b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000)))
//            #b0)
//         (not (>= i3_101 3)))
    private static InvariantTree eg1_right_side(){
        VariableNode d0_100_3 = new VariableNode("d0_100_3", VarType.DOUBLE);
        VariableNode b2_99 = new VariableNode("b2_99", VarType.INTEGER);
        VariableNode i3_101 = new VariableNode("i3_101", VarType.INTEGER);
        LiteralNode one = LiteralNode.getIntLiteral(1);
        LiteralNode three = LiteralNode.getIntLiteral(3);
        LiteralNode bit1 = LiteralNode.getBVLiteral("#b1");
        LiteralNode b10100000000000000000000000000000000000000000000000000 = LiteralNode.getBVLiteral(
                "#b10100000000000000000000000000000000000000000000000000");
        LiteralNode b000010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000 = LiteralNode.getBVLiteral(
                "#b000010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        LiteralNode b0 = LiteralNode.getBVLiteral("b0");
        LiteralNode b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000 =
                LiteralNode.getBVLiteral("b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        LiteralNode b001000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000 =
                LiteralNode.getBVLiteral("b001000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        LiteralNode b000100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000 =
                LiteralNode.getBVLiteral("b000100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");


        InvariantTree result = mkOr(
                mkNot(mkEq(b2_99, one)),
                mkBit2Bool(mkFPMantissa(d0_100_3), 50),
                mkNot(mkEq(mkExtract(10, 10, mkFPExponent(d0_100_3)), bit1)),
                mkEq(b10100000000000000000000000000000000000000000000000000,
                        mkExtract(157, 105,
                                        mkBVAdd(b000010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000,
                                                mkConcat(b0,
                                                        mkExtract(158, 106,
                                                                mkBVAdd(b001000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000,
                                                                        mkConcat(b0, (mkExtract(157, 105, mkBVAdd(b000100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000,
                                                                                mkConcat(b0, mkFPMantissa(d0_100_3), b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000 ))
                                                                                )),
                                                                                b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000))),
                                                        b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000)
                                        ))),
                mkEq(mkExtract(158, 158,
                                mkBVAdd(b001000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000,
                                        mkConcat(b0,
                                                mkExtract(157, 105,
                                                        mkBVAdd(b000100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000,
                                                                mkConcat(b0,
                                                                        mkFPMantissa(d0_100_3),
                                                                        b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
                                                                        ))),
                                                b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
                                        ))),
                        b0),
                mkNot(mkGe(i3_101, three))
        );

        return result;
    }

//NOT(
//    EQ(
//      BVEXTRACT([97, 97]
//        BVLSHR(
//          BVCONCAT(
//            #b0,
//            FP_MANTISSA(
//              v52:DOUBLE
//                        ),
//            #b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
//                    ),
//          BVCONCAT(
//            #x0000000000000000000000000000000000000,
//            BVADD(
//              FP_EXPONENT(
//                v56:DOUBLE
//                            ),
//              BVMUL(
//                #b11111111111,
//                FP_EXPONENT(
//                  v52:DOUBLE
//                                )
//                            )
//                        )
//                    )
//                )
//            ),
//      #b1
//        )
//    ),
//  NOT(
//    EQ(
//      BVEXTRACT([97, 97]
//        BVADD(
//          BVCONCAT(
//            BVEXTRACT([45, 0]
//              FP_MANTISSA(
//                v52:DOUBLE
//                            )
//                        ),
//            #b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
//                    ),
//          BVMUL(
//            #b1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111,
//            BVEXTRACT([150, 0]
//              BVLSHR(
//                BVCONCAT(
//                  #b0,
//                  FP_MANTISSA(
//                    v56:DOUBLE
//                                    ),
//                  #b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
//                                ),
//                BVCONCAT(
//                  #x0000000000000000000000000000000000000,
//                  BVADD(
//                    BVMUL(
//                      #b11111111111,
//                      FP_EXPONENT(
//                        v56:DOUBLE
//                                            )
//                                        ),
//                    FP_EXPONENT(
//                      v52:DOUBLE
//                                        )
//                                    )
//                                )
//                            )
//                        )
//                    )
//                )
//            ),
//      #b1
//        )
//    ),
//  NOT(
//    EQ(
//      BVEXTRACT([85, 85]
//        BVADD(
//          BVMUL(
//            #b1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111,
//            BVEXTRACT([138, 0]
//              BVLSHR(
//                BVCONCAT(
//                  #b0,
//                  FP_MANTISSA(
//                    v56:DOUBLE
//                                    ),
//                  #b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
//                                ),
//                BVCONCAT(
//                  #x0000000000000000000000000000000000000,
//                  BVADD(
//                    BVMUL(
//                      #b11111111111,
//                      FP_EXPONENT(
//                        v56:DOUBLE
//                                            )
//                                        ),
//                    FP_EXPONENT(
//                      v52:DOUBLE
//                                        )
//                                    )
//                                )
//                            )
//                        )
//                    ),
//          BVCONCAT(
//            BVEXTRACT([33, 0]
//              FP_MANTISSA(
//                v52:DOUBLE
//                            )
//                        ),
//            #b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
//                    )
//                )
//            ),
//      #b1
//        )
//    ),
    private static InvariantTree eg2(){
        return mkAnd(eg2_branch1(), eg2_branch2(), eg2_branch3());
    }

    private static InvariantTree mulTestTree(){
        VariableNode v52 = new VariableNode("v52", VarType.DOUBLE);
        VariableNode v56 = new VariableNode("v56", VarType.DOUBLE);
        LiteralNode b00000000110 =
                LiteralNode.getBVLiteral("#b00000000110");
        LiteralNode b0 = LiteralNode.getBVLiteral("#b0");


        return (mkEq(b00000000110, mkBvmul(
                mkConcat(b0, mkFPExponent(v52)), mkConcat(b0, mkFPExponent(v56))
        )));
    }

//    NOT(
//    EQ(
//      BVEXTRACT([97, 97]
//        BVLSHR(
//          BVCONCAT(
//            #b0,
//            FP_MANTISSA(
//              v52:DOUBLE
//                        ),
//            #b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
//                    ),
//          BVCONCAT(
//            #x0000000000000000000000000000000000000,
//            BVADD(
//              FP_EXPONENT(
//                v56:DOUBLE
//                            ),
//              BVMUL(
//                #b11111111111,
//                FP_EXPONENT(
//                  v52:DOUBLE
//                                )
//                            )
//                        )
//                    )
//                )
//            ),
//      #b1
//        )
//    ),
    private static InvariantTree eg2_branch1(){
        VariableNode v52 = new VariableNode("v52", VarType.DOUBLE);
        VariableNode v56 = new VariableNode("v56", VarType.DOUBLE);
        LiteralNode b0 = LiteralNode.getBVLiteral("#b0");
        LiteralNode b1 = LiteralNode.getBVLiteral("#b1");
        LiteralNode b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000 = LiteralNode.getBVLiteral(
                "#b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        LiteralNode b11111111111 =
                LiteralNode.getBVLiteral("#b11111111111");
        LiteralNode x0000000000000000000000000000000000000 =
                LiteralNode.getBVLiteral("#b0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");


        InvariantTree result =
                mkNot(
                        mkEq(
                                mkExtract(97,97,
                                        mkBvlshr(
                                                mkConcat(b0, mkFPMantissa(v52), b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000),
                                                mkConcat(x0000000000000000000000000000000000000,
                                                        mkBVAdd(mkFPExponent(v56),
                                                                mkBvmul(b11111111111, mkFPExponent(v52)))))),
                                b1));

        return result;
    }

//    NOT(
//    EQ(
//      BVEXTRACT([97, 97]
//        BVADD(
//          BVCONCAT(
//            BVEXTRACT([45, 0]
//              FP_MANTISSA(
//                v52:DOUBLE
//                            )
//                        ),
//            #b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
//                    ),
//          BVMUL(
//            #b1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111,
//            BVEXTRACT([150, 0]
//              BVLSHR(
//                BVCONCAT(
//                  #b0,
//                  FP_MANTISSA(
//                    v56:DOUBLE
//                                    ),
//                  #b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
//                                ),
//                BVCONCAT(
//                  #x0000000000000000000000000000000000000,
//                  BVADD(
//                    BVMUL(
//                      #b11111111111,
//                      FP_EXPONENT(
//                        v56:DOUBLE
//                                            )
//                                        ),
//                    FP_EXPONENT(
//                      v52:DOUBLE
//                                        )
//                                    )
//                                )
//                            )
//                        )
//                    )
//                )
//            ),
//      #b1
//        )
//    ),
    private static InvariantTree eg2_branch2(){
        VariableNode v52 = new VariableNode("v52", VarType.DOUBLE);
        VariableNode v56 = new VariableNode("v56", VarType.DOUBLE);
        LiteralNode b0 = LiteralNode.getBVLiteral("#b0");
        LiteralNode b1 = LiteralNode.getBVLiteral("#b1");
        LiteralNode b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000 = LiteralNode.getBVLiteral(
                "#b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        LiteralNode b1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111 = LiteralNode.getBVLiteral(
                "#b1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111");
        LiteralNode b11111111111 =
                LiteralNode.getBVLiteral("#b11111111111");
        LiteralNode x0000000000000000000000000000000000000 =
                LiteralNode.getBVLiteral("#b0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");


        InvariantTree result =
                mkNot(
                        mkEq(
                                mkExtract(97,97,
                                        mkBVAdd(
                                                mkConcat(
                                                        mkExtract(45, 0,
                                                                mkFPMantissa(v52)),
                                                        b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000),
                                                mkBvmul(b1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111,
                                                        mkExtract(150, 0,
                                                                mkBvlshr(
                                                                        mkConcat(b0, mkFPMantissa(v56),b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000),
                                                                        mkConcat(x0000000000000000000000000000000000000,
                                                                                mkBVAdd(
                                                                                        mkBvmul(b11111111111,
                                                                                                mkFPExponent(v56)),
                                                                                        mkFPExponent(v52))))))
                                                )),
                                b1));
        return result;
    }

//  NOT(
//    EQ(
//      BVEXTRACT([85, 85]
//        BVADD(
//          BVMUL(
//            #b1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111,
//            BVEXTRACT([138, 0]
//              BVLSHR(
//                BVCONCAT(
//                  #b0,
//                  FP_MANTISSA(
//                    v56:DOUBLE
//                                    ),
//                  #b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
//                                ),
//                BVCONCAT(
//                  #x0000000000000000000000000000000000000,
//                  BVADD(
//                    BVMUL(
//                      #b11111111111,
//                      FP_EXPONENT(
//                        v56:DOUBLE
//                                            )
//                                        ),
//                    FP_EXPONENT(
//                      v52:DOUBLE
//                                        )
//                                    )
//                                )
//                            )
//                        )
//                    ),
//          BVCONCAT(
//            BVEXTRACT([33, 0]
//              FP_MANTISSA(
//                v52:DOUBLE
//                            )
//                        ),
//            #b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
//                    )
//                )
//            ),
//      #b1
//        )
//    )
    private static InvariantTree eg2_branch3(){
        VariableNode v52 = new VariableNode("v52", VarType.DOUBLE);
        VariableNode v56 = new VariableNode("v56", VarType.DOUBLE);
        LiteralNode b0 = LiteralNode.getBVLiteral("#b0");
        LiteralNode b1 = LiteralNode.getBVLiteral("#b1");
        LiteralNode b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000 = LiteralNode.getBVLiteral(
                "#b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        LiteralNode b1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111 = LiteralNode.getBVLiteral(
                "#b1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111");
        LiteralNode b11111111111 =
                LiteralNode.getBVLiteral("#b11111111111");
        LiteralNode x0000000000000000000000000000000000000 =
                LiteralNode.getBVLiteral("#b0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");


        InvariantTree result =
                mkNot(
                        mkEq(
                                mkExtract(85,85,
                                        mkBVAdd(
                                                mkBvmul(
                                                        b1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111,
                                                        mkExtract(138, 0,
                                                                mkBvlshr(
                                                                        mkConcat(
                                                                                b0,
                                                                                mkFPMantissa(v56),
                                                                                b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
                                                                        ),
                                                                        mkConcat(
                                                                                x0000000000000000000000000000000000000,
                                                                                mkBVAdd(
                                                                                        mkBvmul(b11111111111,
                                                                                                mkFPExponent(
                                                                                                        v56
                                                                                                )),
                                                                                        mkFPExponent(v52)
                                                                                )
                                                                        )))),
                                                mkConcat(
                                                        mkExtract(33,0,
                                                                mkFPMantissa(v52)),
                                                        b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000))),
                                b1));
        return result;
    }

    private static InvariantTree eg1(){
        return mkAnd(eg1_left_side(), eg1_right_side());
    }
    private static InvariantTree eg1_b11(){
        LiteralNode one = LiteralNode.getIntLiteral(1);
        VariableNode b2_99 = new VariableNode("b2_99", VarType.INTEGER);
        return mkNot(mkEq(b2_99, one));
    }
    private static InvariantTree eg1_b12(){
        VariableNode d0_100_3 = new VariableNode("d0_100_3", VarType.DOUBLE);
        return mkBit2Bool(mkFPMantissa(d0_100_3), 50);
    }
    private static InvariantTree loadAlternating_Step_schedule_loop_invariant(){
        return load("Alternating-Step-ScheduleMain_void_mainJayArray_java_lang_String_Block2");
    }
    private static InvariantTree load(String fileName){
        try {
            InvariantTree tree = ASTHelper.readJsonFromFile("invariantTreeJsons/" + fileName);
            return tree;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) throws IOException {
//        filterTrees();
//        InvariantTree tree = load("Nested-Saturation-Then-Reset_Main_void_mainJayArray_java_lang_String_Block2_2");
//        System.out.println(tree.toPrettyString());
//                runAllJsonTrees();
//        InvariantTree t1 = load("Nested-Saturation-Then-ResetMain_void_mainJayArray_java_lang_String_Block2_1");
//        parse_print(t1);
    }

    private static void runAllJsonTrees() throws IOException {
        String fileName;
        File folder = new File("invariantTreeJsons");

        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    fileName = (file.getName());
                    read_parse_save(fileName);
                }
            }
        }
    }
    private static void filterTrees() throws IOException {
        String fileName;
        File folder = new File("invariantTreeJsons");

        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    fileName = (file.getName());
                    InvariantTree t1 = load(fileName);
                    if (t1 instanceof LiteralNode) {
                        Files.deleteIfExists(Paths.get("invariantTreeJsons/" + fileName));
                        System.out.println("File "+ fileName +" deleted if it existed.");
                    }


                }
            }
        }
    }

    private static void parse_print(InvariantTree t1){
        StringBuilder result = new StringBuilder();
        parse_fillResult(t1, result);
        System.out.println(result);
    }

    private static void parse_fillResult(InvariantTree t1, StringBuilder result) {
        result.append("================== input tree ==================\n\n");
        result.append(t1.toPrettyString()).append('\n');


        t1 = ASTHelper.cleaner(t1);
        ParentedInvariantTree pt1 = PhaseOne.parse(t1);
        pt1 = PhaseTwo.parse(pt1);
        result.append("================== output ==================\n");
        result.append("============================================\n\n");
        result.append("================== tree format ==================\n\n");
        result.append(pt1.toRangedString()).append('\n');
        result.append("\n================== CNF format ==================\n\n");
        result.append(pt1.toRangedCNF());
    }

    private static void read_parse_save(String fileName) throws IOException {
        StringBuilder result = new StringBuilder();


        try {
            InvariantTree t1 = load(fileName);
            parse_fillResult(t1, result);
        }catch (Exception e){
            System.out.println("get exception on "+ fileName+" tree:" + e.getMessage());
        }
        saveResults(result, fileName);
    }

    private static void saveResults(StringBuilder result, String file_name) throws IOException {
        String path = "Invariants_range_result\\" + file_name;
        FileWriter myWriter = new FileWriter(path);
        myWriter.write(result.toString());
        myWriter.close();
    }


}
