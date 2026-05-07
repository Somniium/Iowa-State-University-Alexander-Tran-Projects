package coms3620.fashion.departments.finance_and_accounting;

import java.util.List;
import java.util.Scanner;

/**
 * Console menu for the Finance & Accounting department.
 */
public class FinanceMenu {

    private final BudgetManager manager;
    private final Scanner scanner;

    public FinanceMenu(Scanner scanner) {
        this.scanner = scanner;
        this.manager = new BudgetManager();
    }

    public void showMenu() {
        boolean running = true;

        while (running) {
            System.out.println("\n--- Finance & Accounting Menu ---");
            System.out.println("1. View Budgets");
            System.out.println("2. Record Expense");
            System.out.println("3. Transfer Funds");
            System.out.println("4. Approve Department Budget Overrun");
            System.out.println("0. Return to Main Menu");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    viewBudgets();
                    break;
                case "2":
                    recordExpense();
                    break;
                case "3":
                    transferFunds();
                    break;
                case "4":
                    approveBudgetOverrun();
                    break;
                case "0":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    // ---------------- Existing flows ----------------

    private void viewBudgets() {
        List<Budget> budgets = manager.getBudgets();
        if (budgets.isEmpty()) {
            System.out.println("No budgets available.");
            return;
        }

        for (Budget b : budgets) {
            System.out.println(b);
        }
    }

    private void recordExpense() {
        System.out.print("Department name: ");
        String dept = scanner.nextLine();

        System.out.print("Expense description: ");
        String desc = scanner.nextLine();

        System.out.print("Amount: ");
        int amount = Integer.parseInt(scanner.nextLine());

        System.out.print("Date (YYYY-MM-DD): ");
        String date = scanner.nextLine();

        manager.recordExpense(dept, desc, amount, date);
        System.out.println("Expense recorded.");
    }

    private void transferFunds() {
        System.out.print("From department: ");
        String from = scanner.nextLine();

        System.out.print("To department: ");
        String to = scanner.nextLine();

        System.out.print("Amount: ");
        int amount = Integer.parseInt(scanner.nextLine());

        boolean ok = manager.transferFunds(from, to, amount);
        if (ok) {
            System.out.println("Funds transferred successfully.");
        } else {
            System.out.println("Transfer failed.");
        }
    }

    // ---------------- NEW USE CASE ----------------

    /**
     * Approve Department Budget Overrun
     */
    private void approveBudgetOverrun() {
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
            System.out.println((i + 1) + ". " + b.getDepartmentName()
                    + " (Over by $" + overage + ")");
        }

        System.out.print("Select a department number (or 0 to cancel): ");
        int choice = Integer.parseInt(scanner.nextLine());

        // Alternative flow: actor cancels
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
        String confirm = scanner.nextLine();

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
