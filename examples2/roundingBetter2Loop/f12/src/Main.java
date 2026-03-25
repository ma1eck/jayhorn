
import org.sosy_lab.sv_benchmarks.Verifier;

public class Main {
	public static void main(String[] args) {
		double x = Verifier.nondetDouble();
		double y = 0.1;
		assert(x / y != 3.0); // SAFE. 0.3 / 0.1 = 2.999
	}
}
