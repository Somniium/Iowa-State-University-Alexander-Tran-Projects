package coms3620.fashion.departments.finance_and_accounting;

/**
 * Represents a single financial record (expense entry).
 * For this iteration, recordType is used as the department name.
 */
public class Expense {

    private String recordType;   // department name for now
    private String description;
    private int amount;
    private String date;

    /**
     * Creates a new Expense record.
     *
     * @param recordType  type or department the expense belongs to
     * @param description short description of the expense
     * @param amount      amount spent (whole number)
     * @param date        date string (e.g., YYYY-MM-DD)
     */
    public Expense(String recordType, String description, int amount, String date) {
        this.recordType = recordType;
        this.description = description;
        this.amount = amount;
        this.date = date;
    }

    /**
     * @return record type (currently used as department name)
     */
    public String getRecordType() {
        return recordType;
    }

    /**
     * @return description of the expense
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return amount of the expense
     */
    public int getAmount() {
        return amount;
    }

    /**
     * @return date of the expense
     */
    public String getDate() {
        return date;
    }
}
