import org.sosy_lab.sv_benchmarks.Verifier;

public class Main {

	public static void main(String[] args) {
		{
			double y = Verifier.nondetDouble();
			double x = 10.0;
			while (Verifier.nondetBoolean()){
				x /= y;
			}
//			assert(x != 0.42857142857142855);
			assert(x != 1.4285714285714286);
		}
	}
}