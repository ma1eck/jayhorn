
import org.sosy_lab.sv_benchmarks.Verifier;
public class Main {
	public static void main(String[] args) {
		Double x,y;
		x = 1.0;
		y = x;
		int i = 0;
		while (i < 1){
			i += 1;
			x += 2.0;
			y += 2.0;
		}
		assert (x - y == 0.0);
	}
}

