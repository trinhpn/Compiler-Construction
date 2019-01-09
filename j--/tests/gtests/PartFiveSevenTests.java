// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package pass;

import java.lang.System;

public class PartFiveSevenTests {

    public static void main(String[] args) {
        int x = 5;
        int i = 3;

        for (int j = 3; j > 0; j--) {
            x+=1;
            System.out.print("x: ");
            System.out.println(x);
        }

        System.out.println("--------");

        for (; i > 0; i--) {
            x+=1;
            System.out.print("x: ");
            System.out.println(x);
        }

        System.out.println("--------");

        for (;i > -3;) {
            x+=1;
            System.out.print("x: ");
            System.out.println(x);
            i--;
        }

        // for (;;) {
        //     x+=1;
        //     System.out.print("x: ");
        //     System.out.println(x);
        //     i--;
        //     if (i == -5) {
        //         break;
        //     }
        // }
    }

}
