import org.sosy_lab.sv_benchmarks.Verifier;
public class Main {
	public static void main(String[] args) {
		double Y = Verifier.nondetDouble();
		Verifier.assume( Y >= 0.0);
		double S=0.0, E1 = -2.65249474E-315;
		double E0 = -5E-324;
		S = 0.999 * S + E0 - E1 + Y - Y;
//		assert(!(S == -2.652494734E-315));
//		assert(S == 8.572068841523416E301 && S == -2.652494734E-315);
		assert(false);
//		assert (S >= -10000000);
	}
}

