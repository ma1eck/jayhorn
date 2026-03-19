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


      /*  double a=Verifier.nondetDouble();
        Verifier.assume(a > 0.0);*/
        double b=Verifier.nondetDouble();
        Verifier.assume(b > 0.0);
        double a = 1.0;

        if(b > 16.0)
            a = b * 16.000000000000004;
        else {
            a = a + 1.5;
            a += a;
        }

        assert a != 256.09375000000006;

    }
}