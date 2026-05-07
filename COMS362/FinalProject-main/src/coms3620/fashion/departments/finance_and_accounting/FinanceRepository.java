package coms3620.fashion.departments.finance_and_accounting;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for loading and saving Budget and Expense data (CSV file storage only).
 */
public class FinanceRepository {

    private final Path budgetPath;
    private final Path expensePath;

    public FinanceRepository() {
        this.budgetPath = Paths.get("data/finance_and_accounting/budgets.csv");
        this.expensePath = Paths.get("data/finance_and_accounting/expenses.csv");
    }

    // ===================== LOAD =====================

    public List<Budget> loadBudgets() {
        List<Budget> budgets = new ArrayList<>();
        try {
            if (!Files.exists(budgetPath)) return budgets;

            List<String> lines = Files.readAllLines(budgetPath);
            for (String line : lines) {
                if (line == null || line.trim().isEmpty()) continue;

                String[] parts = line.split(",", -1);
                if (parts.length < 4) continue;
                if (!"sii".equals(parts[0])) continue;

                String dept = parts[1];
                int yearly = Integer.parseInt(parts[2].trim());
                int spent = Integer.parseInt(parts[3].trim());

                budgets.add(new Budget(dept, yearly, spent));
            }
        } catch (Exception ignored) {
        }
        return budgets;
    }

    public List<Expense> loadExpenses() {
        List<Expense> expenses = new ArrayList<>();
        try {
            if (!Files.exists(expensePath)) return expenses;

            List<String> lines = Files.readAllLines(expensePath);
            for (String line : lines) {
                if (line == null || line.trim().isEmpty()) continue;

                String[] parts = line.split(",", -1);
                if (parts.length < 5) continue;
                if (!"ssis".equals(parts[0])) continue;

                String dept = parts[1];
                String desc = parts[2];
                int amount = Integer.parseInt(parts[3].trim());
                String date = parts[4];

                expenses.add(new Expense(dept, desc, amount, date));
            }
        } catch (Exception ignored) {
        }
        return expenses;
    }

    // ===================== SAVE (legacy signatures) =====================
    // Keep these if other code calls them. They will just forward to the safe versions.

    public void saveBudgets(List<Budget> budgets) {
        saveBudgetsSafe(budgets);
    }

    public void saveExpenses(List<Expense> expenses) {
        saveExpensesSafe(expenses);
    }

    // ===================== SAVE (safe boolean) =====================

    /**
     * Overwrites budgets.csv with the current in-memory budgets.
     *
     * @return true if file write succeeded; false if it failed.
     */
    public boolean saveBudgetsSafe(List<Budget> budgets) {
        try {
            Path parent = budgetPath.getParent();
            if (parent != null) Files.createDirectories(parent);

            try (BufferedWriter writer = Files.newBufferedWriter(
                    budgetPath,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            )) {
                for (Budget b : budgets) {
                    writer.write("sii," + b.getDepartmentName() + "," + b.getYearlyBudget() + "," + b.getSpentToDate());
                    writer.newLine();
                }
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Overwrites expenses.csv with the current in-memory expenses.
     *
     * @return true if file write succeeded; false if it failed.
     */
    public boolean saveExpensesSafe(List<Expense> expenses) {
        try {
            Path parent = expensePath.getParent();
            if (parent != null) Files.createDirectories(parent);

            try (BufferedWriter writer = Files.newBufferedWriter(
                    expensePath,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            )) {
                for (Expense e : expenses) {
                    writer.write("ssis," + e.getRecordType() + "," + e.getDescription() + "," + e.getAmount() + "," + e.getDate());
                    writer.newLine();
                }
            }
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
}
