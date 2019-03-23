public class TestClass {
    int result;

    int fact(int n) {
        if (n == 1) {
            return 1;
        }
        result = n * fact(n - 1);
        return result;
    }

    void main(int u) {
        fact(5);
    }
}#