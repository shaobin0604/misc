package com.pekall.interview;

public class Test2 {

    /**
     * @param args
     */
    public static void main(String[] args) {
        Integer i = new Integer(42); 
        Long l = new Long(42); 
        Double d = new Double(42.0);

//        System.out.println((i == l)); 
//        System.out.println((i == d)); 
//        System.out.println((d == l)); 
        System.out.println(i.equals(d)); 
        System.out.println((d.equals(i))); 
        System.out.println((i.equals(42))); 

    }

}
