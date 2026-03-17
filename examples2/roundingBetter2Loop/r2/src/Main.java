
import org.sosy_lab.sv_benchmarks.Verifier;
public class Main {
	public static void main(String[] args) {	
	
			Double x = 1.5937500000000004;
			Double y = 5.399835038746165E-21;
			x /= y;


			assert (x > -1.5);

			x *= y;

			assert (x - 1.5937500000000004 < 0.01 && x - 1.5937500000000004 > -0.01);






	}
}

