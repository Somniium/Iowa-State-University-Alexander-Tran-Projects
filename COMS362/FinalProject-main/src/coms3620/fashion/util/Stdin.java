package coms3620.fashion.util;

import java.util.InputMismatchException;
import java.util.Scanner;

public class Stdin {
    private static final Scanner scnr = new Scanner(System.in);

    public static String nextLine() {
        return scnr.nextLine();
    }

    public static int nextInt() {
        while(true) {
            try {
                int rtn = scnr.nextInt();
                scnr.nextLine();
                return rtn;
            }
            catch (InputMismatchException e) {
                System.out.println("Please enter an integer");
                scnr.nextLine();
            }
            
        }
    }

}
