
import org.sosy_lab.sv_benchmarks.Verifier;
public class Main {

	public static void main(String[] args) {

		double a=0.0;
		for (int i=0; i<4; i++){
			double d = 0.25;
			a += d;
		}
		assert  a != 1.0;
	}
}