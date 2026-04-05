
import org.sosy_lab.sv_benchmarks.Verifier;
public class Main {
	public static void main(String[] args) {	
	
			Double x = 0.0 * (1.0) / (0.0);
			Double y = Verifier.nondetDouble();

			if (!(x >= y)){
				assert false;
			}
	}
}

