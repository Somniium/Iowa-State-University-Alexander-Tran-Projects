package coms3620.fashion.menus.finance_and_accounting;

import coms3620.fashion.departments.finance_and_accounting.ExpenseRecorder;
import coms3620.fashion.menus.Option;

/**
 * Option that records an expense using the ExpenseRecorder helper.
 */
public class RecordExpense implements Option {

    private final ExpenseRecorder expenseRecorder;

    public RecordExpense(ExpenseRecorder expenseRecorder) {
        this.expenseRecorder = expenseRecorder;
    }

    @Override
    public String getName() {
        return "Record Expense";
    }

    @Override
    public void run() {
        expenseRecorder.recordExpenseFromInput();
    }
}
