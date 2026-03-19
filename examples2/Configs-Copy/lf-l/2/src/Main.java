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



        double a = Verifier.nondetDouble();
//        Verifier.assume(a > 103000.0);


        double b =  0.09999999999854482;
        a+= b;
//        for(int i = 0; i < 2; i++) {

            b += 0.00000000000000002;
            a += b;
//            b += 0.00000000000000002;
//            a += b;
//        }

      /*  double c = b + 0.00000000000000002;
        double d = c + 0.00000000000000002;*/


      assert a + 857.0 != 104857.90000000001;

      /*  double a = Verifier.nondetDouble();



        double b = 0.09999999998581191;*/
       // double c = 0.09999999999854484;
      /*  double a = Verifier.nondetDouble();



        double b = 0.09999999998672138;

        assert (a+b)  != 104857.7;*/




    }
}