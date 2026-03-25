import org.sosy_lab.sv_benchmarks.Verifier;

public class Main {

	public static void main(String[] args) {
		{
			double x = 10.0;
//			double x = Verifier.nondetDouble();
			double y = Verifier.nondetDouble();
//			double y = 7.0;
//			while (Verifier.nondetBoolean()){
				x /= y;
//			}
			assert(x != 1.4285714285714286);
//			double z = Verifier.nondetDouble();

//			assert(x != z);
		}
	}
}