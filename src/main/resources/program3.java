public class TestClass {
    int result = 0;
    int rez = 0;
    int y = 0;

    int fibonacchi(int n) {
        if (n == 0) {
            return 0;
        }
        if (n == 1) {
            return 1;
        }
        result = fibonacchi(n - 1);
        return result + fibonacchi(n - 2);
    }

    void main(int u) {
        y = fibonacchi(5);
    }
}#