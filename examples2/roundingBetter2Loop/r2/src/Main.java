
import org.sosy_lab.sv_benchmarks.Verifier;
public class Main {
	public static void main(String[] args) {
			Double x, y, a, b;
			x = 1.5937500000000004;
			y = 5.399835038746165E-21;
			a = 1.5937500000000004;
			b = 0.099609375;
//			do {
				x /= y;
				a /= b;
//			}while (Verifier.nondetBoolean());

			assert (x > -1.5);
			assert (a > -1.5);

//			x *= y;
//
//			assert (x - 1.5937500000000004 < 0.01 && x - 1.5937500000000004 > -0.01);






	}
}

