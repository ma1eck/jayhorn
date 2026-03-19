
import org.sosy_lab.sv_benchmarks.Verifier;
public class Main{
	public static void main(String[] args)
	{
//		double x = 1.0, X = 1.6;
//		x = 1E-323;
//		double x1 = 5E-324;
//		while (x1 != x)
//		{
//			x = x1;
//			x1 = x / X;
//
//		}
//		assert (x != 0.0);

		double x = 1.0, X = 1.6;
		x = 1E-323;
		double x1 = 5E-324;
		for (int i=0; i<1; i+=1){
			x = x1;
			x1 = x / X;
		}
		assert (x == x1);
	}
}