
package com.pekall.interview;

public class Test3 {

    private static int j = 0;

    private static boolean methodB(int k) {
        j += k;
        return true;
    }

    public static void methodA(int i) {
        boolean b;
        b = i < 10 | methodB(4);
        b = i < 10 || methodB(8);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        methodA(0);
        System.out.println(j);
    }

}
