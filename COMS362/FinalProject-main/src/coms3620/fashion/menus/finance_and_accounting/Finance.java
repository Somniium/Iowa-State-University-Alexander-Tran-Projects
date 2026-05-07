package coms3620.fashion.menus.finance_and_accounting;

import coms3620.fashion.departments.finance_and_accounting.BudgetManager;
import coms3620.fashion.departments.finance_and_accounting.ExpenseRecorder;
import coms3620.fashion.menus.Menu;
import coms3620.fashion.menus.Option;

/**
 * Top-level Finance & Accounting menu.
 */
public class Finance extends Menu implements Option {

    public Finance() {
        BudgetManager budgetManager = new BudgetManager();
        ExpenseRecorder expenseRecorder = new ExpenseRecorder(budgetManager);

        ManageBudget manageBudget = new ManageBudget(budgetManager);
        RecordExpense recordExpense = new RecordExpense(expenseRecorder);
        TransferFunds transferFunds = new TransferFunds(budgetManager);

        // NEW OPTION
        ApproveBudgetOverrun approveBudgetOverrun = new ApproveBudgetOverrun(budgetManager);

        addOption(manageBudget);
        addOption(recordExpense);
        addOption(transferFunds);
        addOption(approveBudgetOverrun);
    }

    @Override
    public String getName() {
        return "Finance and Accounting";
    }

    @Override
    public void run() {
        enter_menu();
    }
}
