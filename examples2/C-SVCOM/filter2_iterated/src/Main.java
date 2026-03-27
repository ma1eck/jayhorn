public class Main {

    static boolean INIT1, INIT2;
    static float X, P;

    // For filter1
    static float E0 = 0.0f, E1 = 0.0f;
    static float S0 = 0.0f, S1 = 0.0f;

    // For filter2
    static float E20 = 0.0f, E21 = 0.0f;
    static float S20 = 0.0f, S21 = 0.0f;

  
    static void filter1() {
        if (INIT1) {
            S0 = X;
            P = X;
            E0 = X;
            E1 = 0.0f;
            S1 = 0.0f;
        } else {
            P = (float)(0.5f * X - 0.7f * E0 + 0.4f * E1 + 1.5f * S0 - 0.7f * S1);
            E1 = E0;
            E0 = X;
            S1 = S0;
            S0 = P;
            X = (float)(P / 6.0f + S1 / 5.0f);
        }
    }

    static void filter2() {
        if (INIT2) {
            S20 = (float)(0.5f * X);
            P = X;
            E20 = (float)(0.8f * X);
            E21 = 0.0f;
            S21 = 0.0f;
        } else {
            P = (float)(0.3f * X - 0.2f * E20 + 1.4f * E21 + 0.5f * S20 - 1.7f * S21);
            E21 = (float)(0.5f * E20);
            E20 = (float)(2.0f * X);
            S21 = S20 + 10.0f;
            S20 = (float)(P / 2.0f + S21 / 3.0f);
            X = (float)(P / 8.0f + S21 / 10.0f);
        }
    }

    public static void main(String[] args) {
        X = 0.0f;
        INIT1 = true;
        INIT2 = true;

        while (X >= -1155.0f && X <= 4251.0f) {
            X = (float)(0.98 * X + 85.0);

            if (X >= -400.0f && X <= 400.0f) {
                filter1();
                X += 100.0f;
                INIT1 = false;
            } else if (X >= -800.0f && X <= 800.0f) {
                filter2();
                X -= 50.0f;
                INIT2 = false;
            }
        }
        assert false;
    }
}