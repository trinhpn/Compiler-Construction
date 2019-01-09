// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package pass;

import java.lang.System;

public class PartFiveElevenTests {

    public static void tests() {
        int x = 0;

        (x > 1) ? x = 3 : x = 7;

        System.out.print("x: ");
        System.out.println(x);
    }

    public static void main(String[] args) {
        tests();
    }

}
