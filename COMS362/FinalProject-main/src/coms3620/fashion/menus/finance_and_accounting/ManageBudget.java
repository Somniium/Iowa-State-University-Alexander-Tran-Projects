package coms3620.fashion.menus.finance_and_accounting;

import coms3620.fashion.departments.finance_and_accounting.Budget;
import coms3620.fashion.departments.finance_and_accounting.BudgetManager;
import coms3620.fashion.menus.Menu;
import coms3620.fashion.menus.Option;
import coms3620.fashion.util.InputValidation;
import coms3620.fashion.util.Stdin;

import java.util.List;

/**
 * Menu for viewing and updating department budgets.
 */
public class ManageBudget extends Menu implements Option {

    private final BudgetManager budgetManager;

    public ManageBudget(BudgetManager budgetManager) {
        this.budgetManager = budgetManager;
    }

    @Override
    public String getName() {
        return "Manage Budget";
    }

    @Override
    public void run() {
        boolean keepRunning = true;
        while (keepRunning) {
            System.out.println("\n--- Manage Budget ---");
            System.out.println("1) View all department budgets");
            System.out.println("2) Set budget limit for department");
            System.out.println("0) Back");
            int choice = InputValidation.IntegerRangeInput(0, 2);

            if (choice == 1) {
                showBudgets();
            } else if (choice == 2) {
                setBudgetLimit();
            } else {
                keepRunning = false;
            }
        }
    }

    private void showBudgets() {
        List<Budget> budgets = budgetManager.getBudgets();
        if (budgets.isEmpty()) {
            System.out.println("No budgets recorded yet.");
            return;
        }
        System.out.println("Department, Yearly Budget, Spent, Remaining");
        for (Budget b : budgets) {
            System.out.println(b.getDepartmentName() + ", " +
                    b.getYearlyBudget() + ", " +
                    b.getSpentToDate() + ", " +
                    b.getRemainingBudget());
        }
    }

    private void setBudgetLimit() {
        System.out.print("Enter department name: ");
        String dept = Stdin.nextLine().trim();
        System.out.print("Enter new yearly budget (whole number): ");
        String text = Stdin.nextLine().trim();
        try {
            int value = Integer.parseInt(text);
            budgetManager.setBudgetLimit(dept, value);
            System.out.println("Budget updated for " + dept + ".");
        } catch (NumberFormatException e) {
            System.out.println("Invalid number. Budget not changed.");
        }
    }
}
