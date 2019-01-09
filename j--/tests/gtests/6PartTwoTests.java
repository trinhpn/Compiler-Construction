// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package pass;

import java.lang.System;

public class PartTwoTests {

    public static void tests() throws IllegalException {
        int[] i = {1,2,3};
        //double d4 = 4.0;
        //float f5 = 5.0F;
        int d4 = 1;
        double d5 = 2.0;
        int y = 5;
        int j = 1;
        int k = 6;
        int m = 2;
        int x = 1;
        if (j > 1) {
            k = 2;
            k = 3;
        }


        for (int j = 0; m > k; j = j + 2) {
            int z = 3;
            //int k = 2;
        }

        for (int j : i) {  // needs to disallow 'int j = 6'
            int z = 10;
            int k = 2;
        }


        switch (j) {
            case  1: z = 3;
                     z = 10;
                     break;
            case  2: z = 4;
                     break;
            default: z = 10;
                     break;
        }

        
        do {
            x = x * x;
        } until (x > 1000);

        do {
            x = x * x;
        } while (x > 1000);

        while (y > 9) {
            y = y * y;
            //y = x * x;
        }

        throw new IllegalArgumentException("wonderful");

        (j > 1) ? m = 3 : z = 7;

        (j > 1); // non-ternary parExpression() check
    }

    public static void main(String[] args) {
        tests();
    }

}
