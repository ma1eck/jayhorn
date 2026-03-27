
import org.sosy_lab.sv_benchmarks.Verifier;
public class Main {
	public static void main(String[] args) {



//		float f1 = Verifier.nondetFloat();
//		float f2 = 1.1754944E-38f;
//		if (f1 == 1.1754944E-38f){
//			assert f1 * f2 == 0.0f;
//		}
		float f1 = Verifier.nondetFloat();
		float f2 = -3.3881318E-19f;
//		if (f1 == 5.0821977E-21f){
			assert f1 * f2 != 1.721916E-39f;
//		}


	}
}

