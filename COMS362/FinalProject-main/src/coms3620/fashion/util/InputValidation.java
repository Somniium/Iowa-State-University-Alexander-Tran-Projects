package coms3620.fashion.util;

import java.util.List;

public class InputValidation {

    public static int IntegerRangeInput(int min, int max) {
        int rtn;
        while(true) {
            rtn = Stdin.nextInt();
            if(rtn >= min && rtn <= max) {
                break;
            }
            System.out.println("Invalid input, value must be between " + min + " and " + max);
        }
        return rtn;
    }

    public static int IntegerMinInput(int min) {
        int rtn;
        while(true) {
            rtn = Stdin.nextInt();
            if(rtn >= min) {
                break;
            }
            System.out.println("Invalid input, value must be more than " + min);
        }
        return rtn;
    }

    public static int OptionsInput(List<String> options) {
        return OptionsInput((String[])options.toArray());
    }

    public static int OptionsInput(String[] options) {
        for(int i = 0; i < options.length; i++) {
            System.out.println(i +": " + options[i]);
        }
        return IntegerRangeInput(0, options.length-1);
    }
}
