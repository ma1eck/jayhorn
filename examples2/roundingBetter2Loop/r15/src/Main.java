
import org.sosy_lab.sv_benchmarks.Verifier;

public class Main {
	public static void main(String[] args) {
//		double y = Verifier.nondetDouble();
		double x = 33.333333333333385;
 		double y = 0.3333230813344324;
		 while (y > 0.2){
			 y *= x;
		 }
		assert(y != 9.999692440032956E-3);

	}
}
