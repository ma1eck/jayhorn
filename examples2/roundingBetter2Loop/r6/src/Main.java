
import org.sosy_lab.sv_benchmarks.Verifier;
public class Main {
	static double b, c, j;
	static int i;
	public static void main(String[] args) {

		b =  Verifier.nondetDouble();
		c = b;
		j = 2.0;

		i = 1;
		{
			if (i == 2)
				j = 4.0;
			else if (i == 3)
				j = 8.0;
			else if (i == 4)
				j = 16.0;
			c += b / j;
		}
		i = 2;
		{
			if (i == 2)
				j = 4.0;
			else if (i == 3)
				j = 8.0;
			else if (i == 4)
				j = 16.0;
			c += b / j;
		}
//		i = 3;
//		{
//			if (i == 2)
//				j = 4.0;
//			else if (i == 3)
//				j = 8.0;
//			else if (i == 4)
//				j = 16.0;
//			c += b / j;
//		}
//		i = 4;
//		{
//			if (i == 2)
//				j = 4.0;
//			else if (i == 3)
//				j = 8.0;
//			else if (i == 4)
//				j = 16.0;
//			c += b / j;
//		}

		assert (c != 1.9375);
	}
}