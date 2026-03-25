import org.sosy_lab.sv_benchmarks.Verifier;

public class Main {

	public static void main(String[] args) {
		{
//			double x = Verifier.nondetDouble();
			double x = 5.5;
			if (Verifier.nondetBoolean()){
				x = 6.5;
			}
			assert(0.0 <= x - 5.000000000003639);
		}
	}
}