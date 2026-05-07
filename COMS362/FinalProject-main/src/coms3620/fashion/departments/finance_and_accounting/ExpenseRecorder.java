package coms3620.fashion.departments.finance_and_accounting;

import coms3620.fashion.util.Stdin;

import java.time.LocalDate;

/**
 * Helper class to create expenses from user input
 * and delegate recording to BudgetManager.
 */
public class ExpenseRecorder {

    private final BudgetManager budgetManager;

    public ExpenseRecorder(BudgetManager budgetManager) {
        this.budgetManager = budgetManager;
    }

    /**
     * Prompts the user for expense details and records
     * the expense against the chosen department.
     */
    public void recordExpenseFromInput() {
        System.out.println("--- Record Expense ---");

        System.out.print("Department name: ");
        String dept = Stdin.nextLine().trim();
        if (dept.isEmpty()) {
            System.out.println("No department entered, cancelling.");
            return;
        }

        System.out.print("Description: ");
        String description = Stdin.nextLine().trim();

        System.out.print("Amount (whole number): ");
        String amountText = Stdin.nextLine().trim();
        int amount;
        try {
            amount = Integer.parseInt(amountText);
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount, cancelling.");
            return;
        }
        if (amount <= 0) {
            System.out.println("Amount must be positive, cancelling.");
            return;
        }

        String date = LocalDate.now().toString();
        budgetManager.recordExpense(dept, description, amount, date);

        int remaining = budgetManager.getRemaining(dept);
        System.out.println("Expense recorded. Remaining budget for "
                + dept + ": " + remaining);
    }
}
