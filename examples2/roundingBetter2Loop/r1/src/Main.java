
import org.sosy_lab.sv_benchmarks.Verifier;
public class Main {
	public static void main(String[] args) {	
	
			Double x = Verifier.nondetDouble();
			Double y = 1e-53;
			for (int i = 0; i<100; i++){
				x += y;
			}
			if (x < 10.0){
				assert (x <= 10.0);
			}
	}
}

