
import org.sosy_lab.sv_benchmarks.Verifier;

public class Main {
	public static void main(String[] args) {
		float f = Verifier.nondetFloat();
		double x = (double) f;
		double y = 0.4;
		for (int i=0; i<2; i++){
			y /= 2.0;
		}
		assert(x + y != 1.1);
	}
}
