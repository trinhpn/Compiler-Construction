// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package pass;

import java.lang.System;

public class PartFiveTwelveTests {

    public static void tests() {
        boolean x = false;
        x = true && true && true;

        boolean y = false;
        y = true || false;    // wrong

        boolean z = false;
        z = false || false;                                          //2nd

        boolean a = false;
        a = true || true;                                            //2nd

        boolean b = false;
        b = (true && false) || false;   // wrong

        boolean c = false;
        c = false || (true && true);                                  //2nd

        System.out.print("true x: "); // true
        System.out.println(x);

        System.out.print("true y: "); // true
        System.out.println(y);

        System.out.print("false z: "); // false
        System.out.println(z);

        System.out.print("true a: "); // true
        System.out.println(a);

        System.out.print("false b: "); // false
        System.out.println(b);

        System.out.print("true c: "); // true
        System.out.println(c);
    }

    public static void main(String[] args) {
        tests();
    }

}
