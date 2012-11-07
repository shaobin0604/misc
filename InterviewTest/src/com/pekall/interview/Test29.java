package com.pekall.interview;



 class Super {  
     public int i = 0; 
 
     public Super(String text) { 
         i = 1;
     }
}
 
 public class Test29 extends Super { 
    public Test29(String text) { 
        i = 2; 
    }
 
    public static void main(String args[]) { 
        Test29 sub = new Test29("Hello"); 
        System.out.println(sub.i); 
    }
}
