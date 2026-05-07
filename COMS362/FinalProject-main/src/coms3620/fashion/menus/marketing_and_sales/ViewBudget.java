package coms3620.fashion.menus.marketing_and_sales;

import coms3620.fashion.menus.Option;

public class ViewBudget implements Option {

    @Override
    public String getName() {
        return "View Budget";
    }

    @Override
    public void run() {
        System.out.println("Budget is 0 dollars.");
    }
}
