import org.sosy_lab.sv_benchmarks.Verifier;

public class Main {

	public static void main(String[] args) {
		{
			double x = 10.0;
			while (x > 1.0){
				x /= 4.0;
			}
			assert(x == 0.625);
		}
	}
}