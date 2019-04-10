public class TestClass {
    int y = 9;

    public class A {
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
    }

    void main(int u) {
        A.fibonacchi(8);
        y = y + fibonacchi(6);
    }
}#