// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package pass;

import java.lang.System;

public class PartFiveSixTests {

    public static void tests() {
        int x = 1010;

        //do {                     // why does lessThan not work
        //    x = x + 1;
        //} while (x < 10);

        do {
            x = x - 1;
        } while (x > 1000);

        System.out.print("x: ");
        System.out.println(x);
    }

    public static void main(String[] args) {
        tests();
    }

}
