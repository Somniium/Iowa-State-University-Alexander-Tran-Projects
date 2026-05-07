package coms3620.fashion.departments.finance_and_accounting;

import java.util.ArrayList;
import java.util.List;

/**
 * Main logic class for Finance & Accounting.
 * Handles budgets, expenses, simple fund transfers, and approving budget overruns.
 */
public class BudgetManager {

    private final FinanceRepository repo;
    private final List<Budget> budgets;
    private final List<Expense> expenses;

    public BudgetManager() {
        this.repo = new FinanceRepository();
        this.budgets = repo.loadBudgets();
        this.expenses = repo.loadExpenses();
    }

    // --------- Budget helpers ---------

    public List<Budget> getBudgets() {
        return new ArrayList<>(budgets);
    }

    public Budget getOrCreateBudget(String deptName) {
        for (Budget b : budgets) {
            if (b.getDepartmentName().equalsIgnoreCase(deptName)) {
                return b;
            }
        }
        Budget b = new Budget(deptName, 0, 0);
        budgets.add(b);
        // Use safe save (added in FinanceRepository next).
        repo.saveBudgetsSafe(budgets);
        return b;
    }

    public void setBudgetLimit(String deptName, int newLimit) {
        Budget b = getOrCreateBudget(deptName);
        int oldLimit = b.getYearlyBudget();

        b.setYearlyBudget(newLimit);

        boolean ok = repo.saveBudgetsSafe(budgets);
        if (!ok) {
            b.setYearlyBudget(oldLimit);
            System.out.println("ERROR: Budget update could not be saved.");
        }
    }

    public int getRemaining(String deptName) {
        Budget b = getOrCreateBudget(deptName);
        return b.getRemainingBudget();
    }

    // --------- Expense helpers ---------

    public void recordExpense(String deptName, String description, int amount, String date) {
        if (amount <= 0) return;

        Budget b = getOrCreateBudget(deptName);

        int oldSpent = b.getSpentToDate();
        b.recordExpense(amount);

        expenses.add(new Expense(deptName, description, amount, date));

        boolean budgetsOk = repo.saveBudgetsSafe(budgets);
        boolean expensesOk = repo.saveExpensesSafe(expenses);

        if (!budgetsOk || !expensesOk) {
            // Roll back (restore previous budget values + remove the appended expense)
            b.setSpentToDate(oldSpent);
            expenses.remove(expenses.size() - 1);
            System.out.println("ERROR: Expense could not be saved. Changes were reverted.");
        }
    }

    // --------- Transfer funds ---------

    /**
     * Transfers funds between two departments by adjusting their limits.
     *
     * @return true if transfer succeeded, false if not enough remaining in source or save failed.
     */
    public boolean transferFunds(String fromDept, String toDept, int amount) {
        if (amount <= 0 || fromDept.equalsIgnoreCase(toDept)) return false;

        Budget from = getOrCreateBudget(fromDept);
        Budget to = getOrCreateBudget(toDept);

        if (from.getRemainingBudget() < amount) {
            return false;
        }

        int fromOld = from.getYearlyBudget();
        int toOld = to.getYearlyBudget();

        from.setYearlyBudget(fromOld - amount);
        to.setYearlyBudget(toOld + amount);

        boolean ok = repo.saveBudgetsSafe(budgets);
        if (!ok) {
            from.setYearlyBudget(fromOld);
            to.setYearlyBudget(toOld);
            return false;
        }

        return true;
    }

    // --------- NEW USE CASE: Approve Budget Overrun ---------

    /**
     * Returns a list of departments currently over budget (spent > limit).
     */
    public List<Budget> getOverBudgetDepartments() {
        List<Budget> over = new ArrayList<>();
        for (Budget b : budgets) {
            if (b.getSpentToDate() > b.getYearlyBudget()) {
                over.add(b);
            }
        }
        return over;
    }

    /**
     * Approves a budget overrun by increasing the department's budget limit to cover the overage.
     * Implements rollback if saving fails.
     *
     * @param deptName Department to approve
     * @return true if approved and saved successfully; false if not over budget or save failed.
     */
    public boolean approveBudgetOverrun(String deptName) {
        Budget b = getOrCreateBudget(deptName);

        int overage = b.getSpentToDate() - b.getYearlyBudget();
        if (overage <= 0) {
            return false; // not over budget
        }

        int oldLimit = b.getYearlyBudget();
        b.setYearlyBudget(oldLimit + overage);

        boolean ok = repo.saveBudgetsSafe(budgets);
        if (!ok) {
            // File write failure: restore previous budget values
            b.setYearlyBudget(oldLimit);
            return false;
        }

        return true;
    }
}
