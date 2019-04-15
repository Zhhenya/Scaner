public class TestClass {
    int y = 0;

    int fact(int n) {
        if (n == 0) {
            return 0;
        }
        return n * 2;
    }

    int main(int u) {
        y = y + fact(6);
        return 0;
    }
}#