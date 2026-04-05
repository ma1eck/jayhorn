
import org.sosy_lab.sv_benchmarks.Verifier;
public class Main {
	public static void main(String[] args) {	
	
		Double x = Verifier.nondetDouble() * 0.5;
//		Double y = Verifier.nondetDouble();
		double y = 0x0.0000000000001p-1022;
		if (x == 0x0.8000000000000p-1022) {
//			assert  false;
			assert y - x != -0x0.7ffffffffffffp-1022;
//			assert x - y != 0x0.7ffffffffffffp-1022;
		}
	}
}

