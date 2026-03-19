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
        double a =  Verifier.nondetDouble();
        double b = 1.0714285714285712;
        double c = a + b;
      //  double d =  c + 0.25;//Verifier.nondetDouble();


       /* double e = Verifier.nondetDouble();
        double f = d + e;*/
        assert (/*c != 1.5 &&*/ c - 0.13392857142857162 != 0/*1.2053571428571428*/  /*&& f != 1.875*/);
    }
}