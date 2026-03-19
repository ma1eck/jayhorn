/*
 * Origin of the benchmark:
 *     license: MIT (see /java/jayhorn-recursive/LICENSE)
 *     repo: https://github.com/jayhorn/cav_experiments.git
 *     branch: master
 *     root directory: benchmarks/recursive
 * The benchmark was taken from the repo: 24 January 2018
 */
import org.sosy_lab.sv_benchmarks.Verifier;

import java.io.Console;
import java.math.BigInteger;
import java.math.BigDecimal;

public class Main {


    public static void main(String[] args) {

        double a = Verifier.nondetDouble();
        Verifier.assume(a < 39.0);
       double b = a + 1.0;

        while (Verifier.nondetBoolean())
            b += b;

        assert (b != 40.0);

       /* double b = a + 1.0;
        Verifier.assume(b == 2.5);

        double c = b + 2.5;
        Verifier.assume(c == 5.0);

        double d = c+5.0;
        Verifier.assume(d == 10.0);

        double e = d+10.0;
        Verifier.assume(e == 20.0);

        double f = e+20.0;
        assert (f != 40.0);*/
       /* Verifier.assume(f == 40.0);



        double g = f+40.0;
        Verifier.assume(g == 80.0);

        double i = g+80.0;

        Verifier.assume(i == 160.0);

        double h = i+160.0;
        Verifier.assume(h == 320.0);

        double  j = h+320.0;
        Verifier.assume(j == 640.0);

        assert (j+640 != 1280.0);*/

    }
}