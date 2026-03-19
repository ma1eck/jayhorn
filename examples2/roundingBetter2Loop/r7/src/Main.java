
import org.sosy_lab.sv_benchmarks.Verifier;
public class Main {
	static double b, c, j;
	static int i;
	public static void main(String[] args) {

		b =  Verifier.nondetDouble();
//		c = 1.0714284522192845;
//		c = 1.07142835855484;
		c = 1.0714283635218937;
		float f = (float) c;
		assert (f == 1.0714284181594849F);

	}
}