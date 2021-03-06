class Test {

    public int test;

    public int m() {return 1;}

    public int foo() {
        this = new Test(); /* error - can't assign to this */
        return 1;
    }

    public static void main(String[] args) {

        int x = 5; /* ok, assignment to lvalue */
        Test t = new Test();
        int y = t.m();
        boolean b = true;
        boolean[] bb = new boolean[123];
        (new boolean[123])[0] = true; /* ok ? (at least I think so ) */
        (new Test()).foo();
        (new Test()).test = 5;

        t.m() = 5; /* error - assigment to rvalue */
        x + y = 5;
        x - y = 5;
        x * y = 5;
        x / y = 5;
        x % y = 5;
        -x = 5;
        !b = false;
        new Test() = new Test();
        new int[5] = new int[4];
        (new Test()).test; /* error not a statement */


    }
}