
package com.pekall.interview;

interface Foo {
    int k = 0;
}

public class Test14 implements Foo {
    public static void main(String args[]) {
        int i;
        Test14 test = new Test14();
        i = test.k;
        i = Test14.k;
        i = Foo.k;
    }

}
