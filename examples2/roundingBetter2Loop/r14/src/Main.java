
import org.sosy_lab.sv_benchmarks.Verifier;

public class Main {
	public static void main(String[] args) {
//		double x = Verifier.nondetDouble();
		double x = 7.4;
//		double y = 43.4375;
		double y = 43.46875;

		while (40.0 < y ){
			y /= x;
//			if (y = 5.0) y = 43.4375;
		}

//		assert(y != 5.8699324324324325);
		assert(y != 5.874155405405405);

	}
}
