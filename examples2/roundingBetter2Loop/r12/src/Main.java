
import org.sosy_lab.sv_benchmarks.Verifier;

public class Main {
	public static void main(String[] args) {
		double x = Verifier.nondetDouble();
//		double x = 33.333333333333385;
		double y = 10.0;
//		for (int i=0; i<1; i++){
//			y /= 2.0;
//		}
//		Verifier.assume(z < 0.0);
//		assert(z < 1.0);
		assert(x * y != 355.54462009006176);
	}
}
