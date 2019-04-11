public class TestClass {
    int y = 9;
    int o = 0;

    int fact(int c) {
        if (c == 1) {
            return 1;
        }
        int r = c * fact(c - 1);
        return r;
    }

    int fibonacchi(int n) {
        if (n == 0) {
            return 0;
        }
        if (n == 1) {
            return 1;
        }
        int result = fibonacchi(n - 1);
        int rez = fibonacchi(n - 2);
        int all = result + rez;
        return all;
    }

    void main(int u) {
        o = fact(2);
        y = fibonacchi(o);
    }
}#