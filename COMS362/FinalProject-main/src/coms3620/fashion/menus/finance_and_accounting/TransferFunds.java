package coms3620.fashion.menus.finance_and_accounting;

import coms3620.fashion.departments.finance_and_accounting.BudgetManager;
import coms3620.fashion.menus.Option;
import coms3620.fashion.util.Stdin;

/**
 * Option that performs a simple fund transfer between departments.
 */
public class TransferFunds implements Option {

    private final BudgetManager budgetManager;

    public TransferFunds(BudgetManager budgetManager) {
        this.budgetManager = budgetManager;
    }

    @Override
    public String getName() {
        return "Transfer Funds";
    }

    @Override
    public void run() {
        System.out.println("--- Transfer Funds ---");

        System.out.print("Source department: ");
        String from = Stdin.nextLine().trim();

        System.out.print("Destination department: ");
        String to = Stdin.nextLine().trim();

        System.out.print("Amount to transfer (whole number): ");
        String text = Stdin.nextLine().trim();
        int amount;
        try {
            amount = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount, cancelling.");
            return;
        }

        boolean success = budgetManager.transferFunds(from, to, amount);
        if (success) {
            System.out.println("Transfer completed.");
        } else {
            System.out.println("Transfer failed: insufficient remaining budget or invalid input.");
        }
    }
}
