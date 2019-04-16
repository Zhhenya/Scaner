public class TestClass {

    int func(int y) {
        return 5 * 5 + 5 * 6 + 5 * 5 + y / 2 - 1;
    }

    int func2(int r) {
        if (r > 1) {
            return r;
        }
        return r - 1;
    }

    int main(int u) {
        int k = func(5);
        int l = func2(6);
    }
}#



/*
public class TestClass {

    int main(int u) {
        int a = 5;
        int b = 5;
        int c = 0;
        if (a > b) {
            c = a - b + 8 * 6 + 8 * 6;
        } else {
            c = a + b;
        }
    }
}#
*/


/*
public class TestClass {

    int fact(int n) {
        if (n == 0) {
            return 0;
        }
        return n * 2;
    }

    int main(int u) {
        int y = y + fact(6);
        return 0;
    }
}#*/
