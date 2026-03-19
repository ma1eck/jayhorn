
import org.sosy_lab.sv_benchmarks.Verifier;
public class Main {
	public static void main(String[] args) {


//		double a = Verifier.nondetDouble();
		double b =  1.0;
		while (Verifier.nondetBoolean()){
			b += 1.249000902703301E-16;
		}
//		a += b;
		assert b != 0.0;

	}
}

