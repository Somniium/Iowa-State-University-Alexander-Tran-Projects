package coms3620.fashion.menus;

import java.util.ArrayList;
import java.util.List;
import coms3620.fashion.util.Stdin;

public abstract class Menu {

    private final List<Option> options = new ArrayList<>();

    public void enter_menu() {
        while(true) {
            if(!selectOption()) {
                break;
            }
        }
    }

    private boolean selectOption() {
        System.out.println();
        System.out.println("0. Exit Menu");
        for(int i = 0; i < options.size(); i++) {
            System.out.println(Integer.toString(i+1) + ". " + options.get(i).getName());
        }
        int user_input;
        while(true) {
            user_input = Stdin.nextInt();
            if(user_input<0 || user_input > options.size()) {
                System.out.println("Invalid option, please enter a number from 0 to " + (options.size()) + ".");
            }
            else {
                break;
            }
        }
        if(user_input == 0) {
            return false;
        }
        options.get(user_input-1).run();
        return true;
    }

    public void addOption(Option option) {
        options.add(option);
    }
}
