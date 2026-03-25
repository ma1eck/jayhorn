import org.sosy_lab.sv_benchmarks.Verifier;

public class Main {

	public static void main(String[] args) {
		{
			double x = 1.0;
			while (Verifier.nondetBoolean()){
				x *= 3.1;
			}
			assert(x != 9.610000000000001);
		}
	}
}