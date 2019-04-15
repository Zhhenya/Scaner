public class TestClass {
    int y = 9;

    int fibonacchi(int n) {
        if (n == 0) {
            return 0;
        }
        if (n == 1) {
            return 1;
        }
        return fibonacchi(n - 1) + fibonacchi(n - 2);
    }

    int main(int u) {
        y = y + fibonacchi(6);
    }
}#