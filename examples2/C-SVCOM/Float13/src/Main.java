//import org.sosy_lab.sv_benchmarks.Verifier;
public class Main
{



	public static void main(String[] args)
	{
		final float plus_infinity = 1.0f / 0.0f;
		final float minus_infinity = 0.0f / -0.0f;
		final float NaN = 0.0f * (1.0f / 0.0f);

		Boolean temp;

	  // NaN compared to anything should yield false
	  temp = NaN < plus_infinity;

	  if (!(temp == null))
	  {
		  assert false;
	  }

//	  temp = NaN < minus_infinity;
//	  if (!(temp == null))
//	  {
//		  assert false;
//	  }
//
//	  temp = NaN <= NaN;
//	  if (!(temp == null))
//	  {
//		  assert false;
//	  }

//	  temp = NaN >= NaN;
//	  if (!(temp == null))
//	  {
//		  assert false;
//	  }
//
//	  temp = NaN > plus_infinity;
//	  if (!(temp == null))
//	  {
//		  assert false;
//	  }
//
//	  temp = NaN > minus_infinity;
//	  if (!(temp == null))
//	  {
//		  assert false;
//	  }
//
//	  temp = NaN > 0.0;
//	  if (!(temp == null))
//	  {
//		  assert false;
//	  }
//
//	  temp = NaN < 0.0;
//	  if (!(temp == null))
//	  {
//		  assert false;
//	  }


	}
}