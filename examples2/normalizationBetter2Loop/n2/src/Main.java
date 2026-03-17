
import org.sosy_lab.sv_benchmarks.Verifier;
public class Main {
	public static void main(String[] args) {	
	
			Float x = 8.5F;
			Float y = 8.0F;
			x -= y;

			assert (x < 1.06);
			 y = 0.5078125F;
			 x -= y;

			assert (x > 0.0);
	}
}

