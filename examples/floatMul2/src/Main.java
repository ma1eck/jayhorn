
import org.sosy_lab.sv_benchmarks.Verifier;
public class Main {
	public static void main(String[] args) {



		// double a = 2.0;
		//Verifier.assume(a >= 0.0);
		//double b = a;
		double j = 2.0;
		while (j <= 2.0){

			// a +=  j;
			j *= 2.0;

		}

		// assert (a != j || a != 32.0);
		assert j == 4.0;

	}
}

//  p1(a , b) p2(a,b,c)