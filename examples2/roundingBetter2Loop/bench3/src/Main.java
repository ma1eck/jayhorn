
import org.sosy_lab.sv_benchmarks.Verifier;
public class Main {
	public static void main(String[] args) {	
	
			Double x = 1.0625;
			Double y = 3.0531133177191805E-16;
			x -= y;


			assert (x <= 1.0);
			 x = 1.0625;
			 y = 3.0531133177191805E-16;
			x -= y;


			assert (x <= 1.0);
	}
}

