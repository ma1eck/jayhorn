
import org.sosy_lab.sv_benchmarks.Verifier;
public class Main{
	public static void main(String[] args)
	{
//		x = 1E-323;
//		double x1 = 5E-324;

		double x = 1.0;
		double x1 = x / 1.6;

		while (true) {
//			assert(false);
			if (x1 != x) {
				x = x1;
				x1 = x / 1.6;
			} else {
				boolean cond = (x == 0.0);
				assert(cond);
				return;
			}
		}

	}
}