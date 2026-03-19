
import org.sosy_lab.sv_benchmarks.Verifier;
public class Main {
	public static void main(String[] args) {


		double a = Verifier.nondetDouble();
		double b =  0.09999999999854482;
		a+= b;
		b += 0.00000000000000002;
		a += b;
		assert a != 0.0;

	}
}

