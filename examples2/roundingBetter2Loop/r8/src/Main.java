
import org.sosy_lab.sv_benchmarks.Verifier;
public class Main {

	public static void main(String[] args) {

		double a =  Verifier.nondetDouble();
		Verifier.assume(a > 0);
//		double a = 2.0;
		for (int i=0; i <3; i++){
			a += 0.12500000000000025;
		}
		assert (a != 2.3750000000000013 );

	}
}