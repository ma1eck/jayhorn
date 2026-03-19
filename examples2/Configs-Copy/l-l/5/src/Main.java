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

       //double a = 1e20;
        double b =  Verifier.nondetDouble();
      //  Verifier.assume(b == 1.0);
        double c = b;
        double j = 2.0;
        for (int i=1 ; i < 5; i++) {
            if(i == 2)
                j = 4.0;
            else if(i == 3)
                j = 8.0;
            else if(i == 4)
                j = 16.0;


    c +=  b/ j;
   // j *=  2.0;
}

       // double g =  Verifier.nondetDouble();
        //double h = 0.5;
        //double i = g + h;

        assert (c != 1.9375);
    }
}