
import org.sosy_lab.sv_benchmarks.Verifier;
public class Main {
	public static void main(String[] args) {
		Double y = Verifier.nondetDouble(); // 2.2250738585072014E-308
//		assert y!= 0x1.p-1022;
		assert y!= 1.0;

//			assert(y / 2.0 == 1.1125369292536007E-308);
	}
}
