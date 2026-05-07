package coms3620.fashion.menus.finance_and_accounting;

import coms3620.fashion.departments.finance_and_accounting.Budget;
import coms3620.fashion.departments.finance_and_accounting.BudgetManager;
import coms3620.fashion.menus.Option;

import java.util.List;
import java.util.Scanner;

/**
 * Use Case: Approve Department Budget Overrun
 * Actor: Finance Manager
 */
public class ApproveBudgetOverrun implements Option {

    private final BudgetManager manager;

    public ApproveBudgetOverrun(BudgetManager manager) {
        this.manager = manager;
    }

    @Override
    public String getName() {
        return "Approve Department Budget Overrun";
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);

        List<Budget> overBudget = manager.getOverBudgetDepartments();

        // Alternative flow: no departments over budget
        if (overBudget.isEmpty()) {
            System.out.println("No over-budget requests exist.");
            return;
        }

        System.out.println("\nDepartments currently over budget:");
        for (int i = 0; i < overBudget.size(); i++) {
            Budget b = overBudget.get(i);
            int overage = b.getSpentToDate() - b.getYearlyBudget();
            System.out.println((i + 1) + ". " + b.getDepartmentName() + " (Over by $" + overage + ")");
        }

        System.out.print("Select a department number (or 0 to cancel): ");
        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return;
        }

        // Alternative flow: cancel
        if (choice == 0) {
            System.out.println("Approval cancelled.");
            return;
        }

        if (choice < 1 || choice > overBudget.size()) {
            System.out.println("Invalid selection.");
            return;
        }

        Budget selected = overBudget.get(choice - 1);
        String dept = selected.getDepartmentName();

        int limit = selected.getYearlyBudget();
        int spent = selected.getSpentToDate();
        int overage = spent - limit;

        System.out.println("\nDepartment: " + dept);
        System.out.println("Budget limit: $" + limit);
        System.out.println("Spent amount: $" + spent);
        System.out.println("Overage amount: $" + overage);

        System.out.print("Approve budget overrun? (y/n): ");
        String confirm = scanner.nextLine().trim();

        if (!confirm.equalsIgnoreCase("y")) {
            System.out.println("Approval cancelled.");
            return;
        }

        boolean ok = manager.approveBudgetOverrun(dept);

        // Alternative flow: file write failure
        if (!ok) {
            System.out.println("ERROR: Approval could not be saved. Budget was not changed.");
        } else {
            System.out.println("Budget overrun approved successfully.");
        }
    }
}
