/*
 * Origin of the benchmark:
 *     license: MIT (see /java/jayhorn-recursive/LICENSE)
 *     repo: https://github.com/jayhorn/cav_experiments.git
 *     branch: master
 *     root directory: benchmarks/recursive
 * The benchmark was taken from the repo: 24 January 2018
 */
import org.sosy_lab.sv_benchmarks.Verifier;
public class Main {
    public static void main(String[] args) {


     /*   double a=Verifier.nondetDouble();//1.0714285714285712;
        Verifier.assume(a > 0.0);
        double b = 0.0;
       while (Verifier.nondetBoolean())
        {
            a-= 0.1;
            b = a;
        }
        b += 0.1;
        assert b != 0.3;*/

      /*  double a = 1.0;
       for(int i = 0; i < 4; i++) {
            double b = 1.0;
            while (Verifier.nondetBoolean()) {
                b *= 2.0;
            }
            a += b;
        }
        assert  a != 15.0;*/
       /* double a=0.5000000000000006;
        for (int i=0; i < 6; i++)
            a += 0.06250000000000007;



        assert a*0.8125  != 0.710937500000001;*/

        double a=Verifier.nondetDouble();//0.0625;
//        Verifier.assume(a > 0);
        for (int i=0; i < 5; i++)
            a *= 2.0;
        assert a+2.0  != 4.0;





    }
}