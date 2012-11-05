
package com.pekall.interview;

public class Test5 {
    public static void main(String[] args) {
        StringBuffer a = new StringBuffer("A");
        StringBuffer b = new StringBuffer("B");
        operate(a, b);
        System.out.println(a + "," + b);
    }

    static void operate(StringBuffer x, StringBuffer y) {
        x.append(y);
        y = x;
    }
}
