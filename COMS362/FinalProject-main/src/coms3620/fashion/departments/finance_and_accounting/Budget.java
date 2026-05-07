package coms3620.fashion.departments.finance_and_accounting;

/**
 * Represents a yearly budget for a single department.
 * Tracks total allocation and how much has been spent.
 */
public class Budget {

    private String departmentName;
    private int yearlyBudget;
    private int spentToDate;

    /**
     * Creates a new Budget.
     *
     * @param departmentName name of the department
     * @param yearlyBudget   total budget for the year
     * @param spentToDate    amount already spent
     */
    public Budget(String departmentName, int yearlyBudget, int spentToDate) {
        this.departmentName = departmentName;
        this.yearlyBudget = yearlyBudget;
        this.spentToDate = spentToDate;
    }

    /**
     * @return name of the department this budget belongs to
     */
    public String getDepartmentName() {
        return departmentName;
    }

    /**
     * @return total yearly budget allocated
     */
    public int getYearlyBudget() {
        return yearlyBudget;
    }

    /**
     * Sets the yearly budget to a new value.
     *
     * @param yearlyBudget new yearly budget amount
     */
    public void setYearlyBudget(int yearlyBudget) {
        this.yearlyBudget = yearlyBudget;
    }

    /**
     * @return total amount spent so far
     */
    public int getSpentToDate() {
        return spentToDate;
    }

    /**
     * Sets spentToDate directly.
     * This is used for rollback if saving to file fails.
     *
     * @param spentToDate new spentToDate amount
     */
    public void setSpentToDate(int spentToDate) {
        if (spentToDate < 0) {
            this.spentToDate = 0;
        } else {
            this.spentToDate = spentToDate;
        }
    }

    /**
     * Adds an expense to the amount spent so far.
     *
     * @param amount amount to add to spentToDate
     */
    public void recordExpense(int amount) {
        if (amount <= 0) {
            return;
        }
        this.spentToDate += amount;
    }

    /**
     * @return remaining budget (yearlyBudget - spentToDate)
     */
    public int getRemainingBudget() {
        return yearlyBudget - spentToDate;
    }
}
