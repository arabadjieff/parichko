import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class Parichko {
    private JFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField dateField, amountField;
    private JComboBox<String> typeBox, categoryBox;
    private JLabel summaryLabel, tipLabel;
    private StatsPanel statsPanel;

    private List<Transaction> transactions = new ArrayList<>();
    private Stack<UndoAction> undoStack = new Stack<>();
    private Map<String, Double> categoryTotals = new HashMap<>();

    private static final String FILE_NAME = "transactions.csv";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Parichko().GUI());
    }

    private void GUI() {
        frame = new JFrame("Parichko - Personal Finance Tracker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLayout(new BorderLayout());

        //input panel - improved layout
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Transaction Entry"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        dateField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()), 12);
        amountField = new JTextField(12);
        typeBox = new JComboBox<>(new String[]{"Income", "Expense"});
        categoryBox = new JComboBox<>(new String[]{"Salary", "Food", "Transport", "Entertainment", "Other"});

        //row 1
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Date (yyyy-MM-dd):"), gbc);
        gbc.gridx = 1;
        inputPanel.add(dateField, gbc);
        gbc.gridx = 2;
        inputPanel.add(new JLabel("Amount:"), gbc);
        gbc.gridx = 3;
        inputPanel.add(amountField, gbc);

        //row 2
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(typeBox, gbc);
        gbc.gridx = 2;
        inputPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 3;
        inputPanel.add(categoryBox, gbc);

        //buttons panel - separate panel for better visibility
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setBorder(BorderFactory.createTitledBorder("Actions"));

        JButton addButton = new JButton("Add Transaction");
        JButton deleteButton = new JButton("Delete Selected");
        JButton updateButton = new JButton("Update Selected");
        JButton undoButton = new JButton("Undo Last Action");

        //make undo button more visible
        undoButton.setBackground(new Color(255, 200, 100));
        undoButton.setFont(undoButton.getFont().deriveFont(Font.BOLD));

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(undoButton);

        //table setup
        tableModel = new DefaultTableModel(new String[]{"Date", "Type", "Amount", "Category"}, 0);
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Transactions"));

        //stats panel
        statsPanel = new StatsPanel();
        statsPanel.setPreferredSize(new Dimension(300, 300));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Income vs Expense Chart"));

        //left panel layout - fixed structure
        JPanel leftPanel = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(inputPanel, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        leftPanel.add(topPanel, BorderLayout.NORTH);
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        //split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, statsPanel);
        splitPane.setDividerLocation(650);
        splitPane.setResizeWeight(0.7);

        //summary and tips
        summaryLabel = new JLabel("Summary: ");
        tipLabel = new JLabel("Tip: " + getRandomTip());

        JPanel bottomPanel = new JPanel(new GridLayout(2, 1));
        bottomPanel.setBorder(BorderFactory.createEtchedBorder());
        bottomPanel.add(summaryLabel);
        bottomPanel.add(tipLabel);

        //add to frame
        frame.add(splitPane, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        //button actions
        addButton.addActionListener(e -> {
            addTransaction();
            clearInputFields();
        });
        deleteButton.addActionListener(e -> deleteTransaction());
        updateButton.addActionListener(e -> updateTransaction());
        undoButton.addActionListener(e -> undoTransaction());

        //table selection listener - populate fields when row selected
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateFieldsFromSelection();
            }
        });

        //load and display data
        loadTransactions();
        sortTransactionsByDate(transactions);
        updateTable();
        updateSummary();

        frame.setVisible(true);
    }

    private void addTransaction() {
        try {
            String date = dateField.getText().trim();
            String type = typeBox.getSelectedItem().toString();
            String amountText = amountField.getText().trim();

            if (date.isEmpty() || amountText.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Date and Amount fields cannot be empty.");
                return;
            }

            if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                JOptionPane.showMessageDialog(frame, "Date must be in format yyyy-MM-dd.");
                return;
            }

            double amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                JOptionPane.showMessageDialog(frame, "Amount must be a positive number.");
                return;
            }

            String category = categoryBox.getSelectedItem().toString();

            Transaction t;
            if ("Income".equals(type)) {
                t = new IncomeTransaction(date, amount, category);
            } else {
                t = new ExpenseTransaction(date, amount, category);
            }

            transactions.add(t);
            undoStack.push(new UndoAction(UndoAction.ActionType.ADD, t, transactions.size() - 1));

            saveTransactions();
            sortTransactionsByDate(transactions);
            updateTable();
            updateSummary();

            JOptionPane.showMessageDialog(frame, "Transaction added successfully!");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Please enter a valid number for amount.");
        }
    }

    private void deleteTransaction() {
        int row = table.getSelectedRow();
        if (row >= 0 && row < transactions.size()) {
            Transaction removed = transactions.remove(row);
            undoStack.push(new UndoAction(UndoAction.ActionType.DELETE, removed, row));

            saveTransactions();
            updateTable();
            updateSummary();
            clearInputFields();

            JOptionPane.showMessageDialog(frame, "Transaction deleted successfully!");
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a transaction to delete.");
        }
    }

    private void updateTransaction() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= transactions.size()) {
            JOptionPane.showMessageDialog(frame, "Please select a transaction to update.");
            return;
        }

        try {
            String date = dateField.getText().trim();
            String type = typeBox.getSelectedItem().toString();
            String amountText = amountField.getText().trim();
            String category = categoryBox.getSelectedItem().toString();

            if (date.isEmpty() || amountText.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Date and Amount fields cannot be empty.");
                return;
            }

            if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                JOptionPane.showMessageDialog(frame, "Date must be in format yyyy-MM-dd.");
                return;
            }

            double amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                JOptionPane.showMessageDialog(frame, "Amount must be a positive number.");
                return;
            }

            Transaction oldTransaction = transactions.get(row);
            undoStack.push(new UndoAction(UndoAction.ActionType.UPDATE, oldTransaction, row));

            Transaction updatedTransaction;
            if ("Income".equals(type)) {
                updatedTransaction = new IncomeTransaction(date, amount, category);
            } else {
                updatedTransaction = new ExpenseTransaction(date, amount, category);
            }

            transactions.set(row, updatedTransaction);

            sortTransactionsByDate(transactions);
            saveTransactions();
            updateTable();
            updateSummary();

            JOptionPane.showMessageDialog(frame, "Transaction updated successfully!");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Please enter a valid number for amount.");
        }
    }

    private void undoTransaction() {
        if (undoStack.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Nothing to undo.");
            return;
        }

        UndoAction action = undoStack.pop();

        switch (action.type) {
            case ADD:
                transactions.remove(action.transaction);
                break;
            case DELETE:
                if (action.index >= 0 && action.index <= transactions.size()) {
                    transactions.add(action.index, action.transaction);
                } else {
                    transactions.add(action.transaction);
                }
                break;
            case UPDATE:
                if (action.index >= 0 && action.index < transactions.size()) {
                    transactions.set(action.index, action.transaction);
                }
                break;
        }

        sortTransactionsByDate(transactions);
        saveTransactions();
        updateTable();
        updateSummary();
        clearInputFields();

        JOptionPane.showMessageDialog(frame, "Last action undone successfully!");
    }

    private void populateFieldsFromSelection() {
        int row = table.getSelectedRow();
        if (row >= 0 && row < transactions.size()) {
            Transaction t = transactions.get(row);
            dateField.setText(t.getDate());
            amountField.setText(String.format("%.2f", t.getAmount()));
            typeBox.setSelectedItem(t.getType());
            categoryBox.setSelectedItem(t.getCategory());
        }
    }

    private void clearInputFields() {
        dateField.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        amountField.setText("");
        typeBox.setSelectedIndex(0);
        categoryBox.setSelectedIndex(0);
    }

    private void loadTransactions() {
        transactions.clear();
        try {
            if (Files.exists(Paths.get(FILE_NAME))) {
                List<String> lines = Files.readAllLines(Paths.get(FILE_NAME));
                for (String line : lines) {
                    String[] parts = line.split(",");
                    if (parts.length == 4) {
                        String date = parts[0];
                        String type = parts[1];
                        double amount = Double.parseDouble(parts[2]);
                        String category = parts[3];
                        Transaction t;
                        if ("Income".equals(type)) {
                            t = new IncomeTransaction(date, amount, category);
                        } else {
                            t = new ExpenseTransaction(date, amount, category);
                        }
                        transactions.add(t);
                    }
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error loading transactions: " + e.getMessage());
        }
    }

    private void saveTransactions() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Transaction t : transactions) {
                pw.println(t.toCSV());
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error saving transactions: " + e.getMessage());
        }
    }

    private void updateTable() {
        tableModel.setRowCount(0);
        for (Transaction t : transactions) {
            tableModel.addRow(new Object[]{
                    t.getDate(),
                    t.getType(),
                    String.format("%.2f", t.getAmount()),
                    t.getCategory()
            });
        }
        statsPanel.repaint();
    }

    private void updateSummary() {
        categoryTotals.clear();

        double income = 0, expense = 0;
        for (Transaction t : transactions) {
            if ("Income".equals(t.getType())) {
                income += t.getAmount();
            } else {
                expense += t.getAmount();
            }

            categoryTotals.put(t.getCategory(),
                    categoryTotals.getOrDefault(t.getCategory(), 0.0) + t.getAmount());
        }

        double balance = income - expense;
        summaryLabel.setText(String.format(
                "Summary: Income = %.2f | Expense = %.2f | Balance = %.2f | Transactions: %d",
                income, expense, balance, transactions.size()));
    }

    private String getRandomTip() {
        String[] tips = {
                "Track every expense to understand your habits :)",
                "Set a monthly budget and stick to it :)",
                "Save at least 10% of your income :)",
                "Use categories to analyze spending patterns :)",
                "Review your finances weekly :)",
                "Click on a transaction row to edit it easily :)",
                "Use the Undo button if you make a mistake :)"
        };
        return tips[new Random().nextInt(tips.length)];
    }

    private void sortTransactionsByDate(List<Transaction> list) {
        list.sort((t1, t2) -> t1.getDate().compareTo(t2.getDate()));
    }

    // Inner classes
    abstract class Transaction {
        private final String date;
        private final double amount;
        private final String category;

        Transaction(String date, double amount, String category) {
            this.date = date;
            this.amount = amount;
            this.category = category;
        }

        public String getDate() { return date; }
        public double getAmount() { return amount; }
        public String getCategory() { return category; }
        public abstract String getType();

        public String toCSV() {
            return String.format("%s,%s,%.2f,%s", date, getType(), amount, category);
        }
    }

    class IncomeTransaction extends Transaction {
        IncomeTransaction(String date, double amount, String category) {
            super(date, amount, category);
        }

        @Override
        public String getType() {
            return "Income";
        }
    }

    class ExpenseTransaction extends Transaction {
        ExpenseTransaction(String date, double amount, String category) {
            super(date, amount, category);
        }

        @Override
        public String getType() {
            return "Expense";
        }
    }

    class UndoAction {
        enum ActionType { ADD, DELETE, UPDATE }
        ActionType type;
        Transaction transaction;
        int index;

        UndoAction(ActionType type, Transaction transaction, int index) {
            this.type = type;
            this.transaction = transaction;
            this.index = index;
        }
    }

    class StatsPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());

            double income = 0, expense = 0;
            for (Transaction t : transactions) {
                if ("Income".equals(t.getType())) {
                    income += t.getAmount();
                } else {
                    expense += t.getAmount();
                }
            }

            double total = income + expense;
            if (total == 0) {
                g.setColor(Color.BLACK);
                g.setFont(new Font("Arial", Font.BOLD, 14));
                g.drawString("No data to display", 20, 30);
                return;
            }

            int width = getWidth() - 40;
            int height = getHeight() - 100;
            int barHeight = 40;
            int x = 20;
            int y = 50;

            int incomeBarLength = (int) (width * (income / total));
            int expenseBarLength = (int) (width * (expense / total));

            //income bar
            g.setColor(new Color(34, 139, 34));
            g.fillRect(x, y, incomeBarLength, barHeight);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, incomeBarLength, barHeight);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString(String.format("Income: %.2f", income), x, y - 5);

            //expense bar
            g.setColor(new Color(220, 20, 60));
            g.fillRect(x, y + 60, expenseBarLength, barHeight);
            g.setColor(Color.BLACK);
            g.drawRect(x, y + 60, expenseBarLength, barHeight);
            g.drawString(String.format("Expense: %.2f", expense), x, y + 55);

            //balance
            double balance = income - expense;
            g.setColor(balance >= 0 ? new Color(34, 139, 34) : new Color(220, 20, 60));
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString(String.format("Balance: %.2f", balance), x, y + 120);
        }
    }
}
