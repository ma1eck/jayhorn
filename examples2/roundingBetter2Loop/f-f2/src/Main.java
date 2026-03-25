import org.sosy_lab.sv_benchmarks.Verifier;

public class Main {

	public static void main(String[] args) {
//		double x = 33.333333333333385;
//		double y = 10.666338602701837;
//		double y = Verifier.nondetDouble();
//		assert(355.5446201198641 - x * y  != 2.9802322387695313E-8);

		double x = 33.333333333333385;
//		double x = Verifier.nondetDouble();
//		double y = 0.3333230813344324;
		double y = Verifier.nondetDouble();

		x *= y;
//		double z = Verifier.nondetDouble();
		assert(9.999752044677731E-3 - x != 5.960464477539063E-8);
//		assert(9.999752044677731E-3 - x != z);
//		assert(z - x != 5.960464477539063E-8);
	}
}