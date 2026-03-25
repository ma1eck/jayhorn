import org.sosy_lab.sv_benchmarks.Verifier;

public class Main {

	public static void main(String[] args) {
		{
			double x = Verifier.nondetDouble();
//			double x = 32.0;
//			Verifier.assume( 30.0 <= x && x <= 40.0);
			double y = 1.000000000000001;
//			double y = 2.225073858507204E-308;
//			double y = Verifier.nondetDouble();
			double z = 31.0;


			assert(x - y != z);
		}
	}
}