import org.sosy_lab.sv_benchmarks.Verifier;
public class Main {
	public static void main(String[] args) {
		double Y = Verifier.nondetDouble();
		if (Double.isNaN(Y)) assert  false;
//		float Y = Verifier.nondetFloat();
//		if (Float.isNaN(Y)) assert  false;
	}
}

