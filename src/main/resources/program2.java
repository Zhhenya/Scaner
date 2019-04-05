public class TestClass {
    int result = 0;
    int y = 0;

    int fact(int n) {
        if (n == 1) {
            return 1;
        }
        result = n * fact(n - 1);
        return result;
    }

    void main(int u) {
        y = fact(4);
    }
}#