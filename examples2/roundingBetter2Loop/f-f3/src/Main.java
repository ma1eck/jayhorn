import org.sosy_lab.sv_benchmarks.Verifier;
public class Main {
	public static void main(String[] args) {

		double a=0.0;
		double d = 0.25;
		for (int i=0; i<3; i++){
			a += d;
		}
		assert  a + 0.09375000000000007 != 0.8437500000000001;
	}
}