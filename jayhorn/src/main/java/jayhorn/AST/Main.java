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
//                = "Nested-Saturation-Then-ResetMain_void_mainJayArray_java_lang_String_Block2_1";

//        ASTHelper.convertFloatBitmaskToIntervals(new FloatingPointLiteralValue(53, 11));

//        InvariantTree t1 = loadAlternating_Step_schedule_loop_invariant();
//        InvariantTree t1 = load("Batch-Conveyor-CounterMain_void_mainJayArray_java_lang_String_Block2_1");
//        InvariantTree t1 = load("Bounded-Proportional-UpdateMain_void_mainJayArray_java_lang_String_Block2_1");
//        InvariantTree t1 = load("Bounded-Reset-Linear-GrowthMain_void_mainJayArray_java_lang_String_Block2_1");
//        InvariantTree t1 = load("Clamped-Triangular-DriftMain_void_mainJayArray_java_lang_String_Block2_1");
//        InvariantTree t1 = load("Inner-Retry-Until-OKMain_void_mainJayArray_java_lang_String_Block5");
//        InvariantTree t1 = load("Inner-Retry-Until-OKMain_void_mainJayArray_java_lang_String_Block2");
//        InvariantTree t1 = load("Nested-PingPong-with-CapsMain_void_mainJayArray_java_lang_String_Block2_1");
//            read_parse_save("Inner-Retry-Until-OKMain_void_mainJayArray_java_lang_String_Block5");

        runAllJsonTrees();
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

    private static void read_parse_save(String fileName) throws IOException {
        StringBuilder result = new StringBuilder();

        InvariantTree t1 = load(fileName);
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

        saveResults(result, fileName);
    }

    private static void saveResults(StringBuilder result, String file_name) throws IOException {
        String path = "Invariants_range_result\\" + file_name;
        FileWriter myWriter = new FileWriter(path);
        myWriter.write(result.toString());
        myWriter.close();
    }


}
