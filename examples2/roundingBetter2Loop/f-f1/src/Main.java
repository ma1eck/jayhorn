import org.sosy_lab.sv_benchmarks.Verifier;

public class Main {

	public static void main(String[] args) {
		double a = Verifier.nondetDouble();
//		double a = 140737488355371.47;
//		double b = Verifier.nondetDouble();
		double b = 140737488355328.0;
//		double a = 15.4;
//		double b = 8.0;
		double y = a - b;
//		double x = Verifier.nondetDouble();
		double x = 7.4;
		assert(y != 43.46875 || y / x != 5.874155405405405);
//		if
//		assert(y / x == 5.874155405405405);
//			assert(y == 43.46875);
//		}
//		if (y == 43.46875){
//		if (x == 7.4){
//			assert(y / x != 5.874155405405405);
//		}
//		double y = 0.3333230813344324;



	}
}