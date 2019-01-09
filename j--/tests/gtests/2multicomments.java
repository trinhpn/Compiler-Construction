package pass;

import java.lang.System;

public class MultiLineComments {

    public static String message() {
        /** Basic test. **/
        int a = 3;
        /****/
        a = 4;
        /***/
        a = 5;
        /**/
        a = 6;
        /** multi-
             line
             test
                    **/
        return "Hello, World!";
    }

    public static void main(String[] args) {
        System.out.println(MultiLineComments.message());
    }

}
