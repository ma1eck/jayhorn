
import org.sosy_lab.sv_benchmarks.Verifier;
public class Main {
	public static void main(String[] args) {
		double x,  X = 1.6;
//		x = Verifier.nondetDouble();
		long bits = Verifier.nondetLong();
		x = Double.longBitsToDouble(bits);
//		x = x >> 53;
//		x = 5E-324;
		Verifier.assume(0.0 <= x && x <= 2.225073858507202E-308);

		double x1 = x/X;
		assert (x1 != x || x == 0.0);
//		if (x == x1) {
//			assert (x == 0.0);
//		}
	}
}

//  p1(a , b) p2(a,b,c)