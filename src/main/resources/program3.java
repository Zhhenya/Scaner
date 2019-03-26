public class TestClass {
    int result = 0;

    int fibonacchi(int n) {
        if (n == 0) {
            return 0;
        }
        if (n == 1) {
            return 1;
        }
        return fibonacchi(n - 2) + fibonacchi(n - 1);
    }

    void main(int u) {
        fibonacchi(5);
    }
}#