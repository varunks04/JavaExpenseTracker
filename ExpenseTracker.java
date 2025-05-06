import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List; // Explicit import to resolve ambiguity

public class ExpenseTracker extends JFrame {
    private HashMap<String, java.util.List<Expense>> expenseMap;
    private JTextField amountField;
    private JTextField descriptionField;
    private JComboBox<String> categoryBox;
    private JTextArea summaryArea;
    private JTable expenseTable;
    private DefaultTableModel tableModel;
    private JDateChooser dateChooser;
    private JPanel chartPanel;

    private final String DATA_FILE = "expenses.txt";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public ExpenseTracker() {
        expenseMap = new HashMap<>();
        loadExpensesFromFile();

        setTitle("Expense Tracker");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Create the input panel
        JPanel inputPanel = createInputPanel();

        // Create tabs for different views
        JTabbedPane tabbedPane = new JTabbedPane();

        // Summary tab
        JPanel summaryPanel = createSummaryPanel();
        tabbedPane.addTab("Summary", summaryPanel);

        // Detailed expenses tab
        JPanel detailedPanel = createDetailedPanel();
        tabbedPane.addTab("Detailed View", detailedPanel);

        // Chart tab
        chartPanel = new JPanel();
        tabbedPane.addTab("Charts", chartPanel);

        // Add components to the main frame
        add(inputPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);

        // Save data when window closes
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                saveExpensesToFile();
            }
        });

        // Initial update
        updateExpenseTable();
        updateSummary();
        updateChart();

        setVisible(true);
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Add New Expense"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel amountLabel = new JLabel("Amount:");
        amountField = new JTextField(10);

        JLabel descriptionLabel = new JLabel("Description:");
        descriptionField = new JTextField(20);

        JLabel categoryLabel = new JLabel("Category:");
        String[] categories = {"Food", "Travel", "Shopping", "Bills", "Entertainment", "Health", "Education", "Other"};
        categoryBox = new JComboBox<>(categories);
        categoryBox.setEditable(true);

        JLabel dateLabel = new JLabel("Date:");
        dateChooser = new JDateChooser();
        dateChooser.setDate(new Date());

        JButton addButton = new JButton("Add Expense");
        addButton.addActionListener(e -> addExpense());

        JButton clearButton = new JButton("Clear Fields");
        clearButton.addActionListener(e -> clearFields());

        // Row 0
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(dateLabel, gbc);

        gbc.gridx = 1;
        panel.add(dateChooser, gbc);

        gbc.gridx = 2;
        panel.add(categoryLabel, gbc);

        gbc.gridx = 3;
        panel.add(categoryBox, gbc);

        // Row 1
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(amountLabel, gbc);

        gbc.gridx = 1;
        panel.add(amountField, gbc);

        gbc.gridx = 2;
        panel.add(descriptionLabel, gbc);

        gbc.gridx = 3;
        panel.add(descriptionField, gbc);

        // Row 2
        gbc.gridx = 1; gbc.gridy = 2;
        panel.add(addButton, gbc);

        gbc.gridx = 2;
        panel.add(clearButton, gbc);

        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        summaryArea = new JTextArea();
        summaryArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(summaryArea);

        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton refreshButton = new JButton("Refresh Summary");
        refreshButton.addActionListener(e -> updateSummary());

        JButton exportButton = new JButton("Export Summary");
        exportButton.addActionListener(e -> exportSummary());

        buttonPanel.add(refreshButton);
        buttonPanel.add(exportButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createDetailedPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create table model with columns
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };

        tableModel.addColumn("Date");
        tableModel.addColumn("Category");
        tableModel.addColumn("Description");
        tableModel.addColumn("Amount");

        expenseTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(expenseTable);

        panel.add(tableScrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton deleteButton = new JButton("Delete Selected");
        deleteButton.addActionListener(e -> deleteSelectedExpense());

        JButton filterButton = new JButton("Filter By Category");
        filterButton.addActionListener(e -> filterByCategory());

        buttonPanel.add(deleteButton);
        buttonPanel.add(filterButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void addExpense() {
        try {
            double amount = Double.parseDouble(amountField.getText());
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Please enter a positive amount.");
                return;
            }

            String category = categoryBox.getSelectedItem().toString().trim();
            if (category.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select or enter a category.");
                return;
            }

            String description = descriptionField.getText().trim();
            if (description.isEmpty()) {
                description = "No description";
            }

            Date date = dateChooser.getDate();
            if (date == null) date = new Date();

            Expense expense = new Expense(amount, category, description, date);

            expenseMap.putIfAbsent(category, new ArrayList<>());
            expenseMap.get(category).add(expense);

            JOptionPane.showMessageDialog(this, "Expense added successfully!");

            clearFields();
            updateExpenseTable();
            updateSummary();
            updateChart();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for amount.");
        }
    }

    private void clearFields() {
        amountField.setText("");
        descriptionField.setText("");
        dateChooser.setDate(new Date());
    }

    private void updateSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("EXPENSE SUMMARY\n");
        summary.append("==================================================\n\n");

        double grandTotal = 0;

        Map<String, Double> categorySums = new HashMap<>();

        // Calculate total for each category
        for (Map.Entry<String, java.util.List<Expense>> entry : expenseMap.entrySet()) {
            double total = entry.getValue().stream()
                .mapToDouble(Expense::getAmount)
                .sum();

            categorySums.put(entry.getKey(), total);
            grandTotal += total;
        }

        // Sort categories by amount (descending)
        List<Map.Entry<String, Double>> sortedEntries = new ArrayList<>(categorySums.entrySet());
        sortedEntries.sort(Map.Entry.<String, Double>comparingByValue().reversed());

        // Display sorted results
        for (Map.Entry<String, Double> entry : sortedEntries) {
            double total = entry.getValue();
            double percentage = (grandTotal > 0) ? (total / grandTotal) * 100 : 0;

            summary.append(String.format("%-15s: ₹%-10.2f (%.1f%%)\n", 
                entry.getKey(), total, percentage));
        }

        summary.append("\n==================================================\n");
        summary.append(String.format("TOTAL EXPENSES: ₹%.2f\n", grandTotal));

        // Additional statistics
        if (!expenseMap.isEmpty()) {
            // Calculate date range
            Date earliestDate = null;
            Date latestDate = null;

            for (List<Expense> expenses : expenseMap.values()) {
                for (Expense expense : expenses) {
                    Date date = expense.getDate();
                    if (earliestDate == null || date.before(earliestDate)) {
                        earliestDate = date;
                    }
                    if (latestDate == null || date.after(latestDate)) {
                        latestDate = date;
                    }
                }
            }

            summary.append("\nDate Range: ");
            if (earliestDate != null && latestDate != null) {
                summary.append(dateFormat.format(earliestDate))
                       .append(" to ")
                       .append(dateFormat.format(latestDate));
            }
        }

        summaryArea.setText(summary.toString());
    }

    private void updateExpenseTable() {
        tableModel.setRowCount(0); // Clear the table

        List<Expense> allExpenses = new ArrayList<>();

        // Gather all expenses
        for (List<Expense> expenses : expenseMap.values()) {
            allExpenses.addAll(expenses);
        }

        // Sort by date (newest first)
        allExpenses.sort(Comparator.comparing(Expense::getDate).reversed());

        // Add to table
        for (Expense expense : allExpenses) {
            tableModel.addRow(new Object[] {
                dateFormat.format(expense.getDate()),
                expense.getCategory(),
                expense.getDescription(),
                String.format("₹%.2f", expense.getAmount())
            });
        }
    }

    private void updateChart() {
        chartPanel.removeAll();
        chartPanel.setLayout(new BorderLayout());

        JPanel pieChartPanel = createPieChart();
        chartPanel.add(pieChartPanel, BorderLayout.CENTER);

        chartPanel.revalidate();
        chartPanel.repaint();
    }

    private JPanel createPieChart() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Get total expenses by category
                Map<String, Double> categoryTotals = new HashMap<>();
                double totalAmount = 0;

                for (Map.Entry<String, java.util.List<Expense>> entry : expenseMap.entrySet()) {
                    double sum = entry.getValue().stream()
                        .mapToDouble(Expense::getAmount)
                        .sum();

                    categoryTotals.put(entry.getKey(), sum);
                    totalAmount += sum;
                }

                if (totalAmount == 0) {
                    g2d.drawString("No expenses to display", getWidth() / 2 - 60, getHeight() / 2);
                    return;
                }

                // Define colors for the pie chart
                Color[] colors = {
                    new Color(65, 105, 225),  // Royal Blue
                    new Color(46, 139, 87),   // Sea Green
                    new Color(255, 99, 71),   // Tomato
                    new Color(255, 165, 0),   // Orange
                    new Color(138, 43, 226),  // Blue Violet
                    new Color(0, 128, 128),   // Teal
                    new Color(255, 20, 147),  // Deep Pink
                    new Color(184, 134, 11)   // Dark Goldenrod
                };

                // Draw the pie chart
                int diameter = Math.min(getWidth(), getHeight()) - 80;
                int x = (getWidth() - diameter) / 2;
                int y = (getHeight() - diameter) / 2;

                int startAngle = 0;
                int i = 0;

                // Draw legend
                int legendX = 20;
                int legendY = 30;

                for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
                    double percentage = (entry.getValue() / totalAmount) * 100;
                    int angle = (int) (percentage * 3.6); // 3.6 degrees per percentage point

                    // Draw pie slice
                    g2d.setColor(colors[i % colors.length]);
                    g2d.fillArc(x, y, diameter, diameter, startAngle, angle);

                    // Draw legend item
                    g2d.fillRect(legendX, legendY - 10, 15, 15);
                    g2d.setColor(Color.BLACK);
                    g2d.drawString(String.format("%s: ₹%.2f (%.1f%%)", 
                        entry.getKey(), entry.getValue(), percentage),
                        legendX + 20, legendY);

                    startAngle += angle;
                    i++;
                    legendY += 20;
                }

                // Draw title
                g2d.setFont(new Font("Arial", Font.BOLD, 16));
                g2d.drawString("Expense Distribution", getWidth() / 2 - 80, 20);
            }
        };

        panel.setPreferredSize(new Dimension(500, 400));
        return panel;
    }

    private void deleteSelectedExpense() {
        int selectedRow = expenseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an expense to delete.");
            return;
        }

        String date = (String) tableModel.getValueAt(selectedRow, 0);
        String category = (String) tableModel.getValueAt(selectedRow, 1);
        String description = (String) tableModel.getValueAt(selectedRow, 2);

        if (JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this expense?\n" + 
            "Date: " + date + "\n" +
            "Category: " + category + "\n" +
            "Description: " + description,
            "Confirm Deletion", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

            List<Expense> expenses = expenseMap.get(category);
            if (expenses != null) {
                // Find the expense to remove
                for (Iterator<Expense> it = expenses.iterator(); it.hasNext();) {
                    Expense expense = it.next();
                    try {
                        Date expenseDate = dateFormat.parse(date);
                        if (expense.getCategory().equals(category) && 
                            expense.getDescription().equals(description) && 
                            expense.getDate().equals(expenseDate)) {
                            it.remove();
                            break;
                        }
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }
                }

                // If the category now has no expenses, remove it
                if (expenses.isEmpty()) {
                    expenseMap.remove(category);
                }

                // Update everything
                updateExpenseTable();
                updateSummary();
                updateChart();

                JOptionPane.showMessageDialog(this, "Expense deleted.");
            }
        }
    }

    private void filterByCategory() {
        if (expenseMap.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No expenses to filter.");
            return;
        }

        String[] categories = expenseMap.keySet().toArray(new String[0]);
        String selectedCategory = (String) JOptionPane.showInputDialog(
            this, "Select category to filter:", "Filter", 
            JOptionPane.QUESTION_MESSAGE, null, categories, categories[0]);

        if (selectedCategory != null) {
            tableModel.setRowCount(0);

            List<Expense> expenses = expenseMap.get(selectedCategory);
            expenses.sort(Comparator.comparing(Expense::getDate).reversed());

            for (Expense expense : expenses) {
                tableModel.addRow(new Object[] {
                    dateFormat.format(expense.getDate()),
                    expense.getCategory(),
                    expense.getDescription(),
                    String.format("₹%.2f", expense.getAmount())
                });
            }
        }
    }

    private void exportSummary() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Summary");

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                String path = file.getAbsolutePath();
                if (!path.toLowerCase().endsWith(".txt")) {
                    file = new File(path + ".txt");
                }

                try (PrintWriter writer = new PrintWriter(file)) {
                    writer.println(summaryArea.getText());
                    JOptionPane.showMessageDialog(this, "Summary exported successfully to " + file.getName());
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error exporting summary: " + e.getMessage());
        }
    }

    private void saveExpensesToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE))) {
            for (Map.Entry<String, java.util.List<Expense>> entry : expenseMap.entrySet()) {
                for (Expense expense : entry.getValue()) {
                    // Format: category,amount,description,date
                    writer.write(String.format("%s,%f,%s,%s",
                        expense.getCategory(),
                        expense.getAmount(),
                        expense.getDescription().replace(',', ';'),  // Escape commas in description
                        dateFormat.format(expense.getDate())
                    ));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Error saving file: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error saving expenses: " + e.getMessage());
        }
    }

    private void loadExpensesFromFile() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                try {
                    // Parse CSV-like format
                    String[] parts = line.split(",", 4); // Limit to 4 parts

                    if (parts.length >= 4) {
                        String category = parts[0];
                        double amount = Double.parseDouble(parts[1]);
                        String description = parts[2];
                        Date date = dateFormat.parse(parts[3]);

                        Expense expense = new Expense(amount, category, description, date);

                        expenseMap.putIfAbsent(category, new ArrayList<>());
                        expenseMap.get(category).add(expense);
                    }
                } catch (ParseException | NumberFormatException e) {
                    System.err.println("Error parsing line: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new ExpenseTracker());
    }

    // Inner class to represent an expense
    static class Expense {
        private double amount;
        private String category;
        private String description;
        private Date date;

        public Expense(double amount, String category, String description, Date date) {
            this.amount = amount;
            this.category = category;
            this.description = description;
            this.date = date;
        }

        public double getAmount() {
            return amount;
        }

        public String getCategory() {
            return category;
        }

        public String getDescription() {
            return description;
        }

        public Date getDate() {
            return date;
        }
    }

    // Inner class for date chooser component
    static class JDateChooser extends JPanel {
        private JComboBox<String> dayBox;
        private JComboBox<String> monthBox;
        private JComboBox<String> yearBox;

        public JDateChooser() {
            setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));

            // Days 1-31
            String[] days = new String[31];
            for (int i = 0; i < 31; i++) {
                days[i] = String.valueOf(i + 1);
            }
            dayBox = new JComboBox<>(days);

            // Months 1-12
            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                              "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            monthBox = new JComboBox<>(months);

            // Years (current year and 5 years back)
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            String[] years = new String[6];
            for (int i = 0; i < 6; i++) {
                years[i] = String.valueOf(currentYear - i);
            }
            yearBox = new JComboBox<>(years);

            add(dayBox);
            add(monthBox);
            add(yearBox);

            // Set to current date
            setDate(new Date());
        }

        public void setDate(Date date) {
            if (date == null) return;

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            dayBox.setSelectedIndex(cal.get(Calendar.DAY_OF_MONTH) - 1);
            monthBox.setSelectedIndex(cal.get(Calendar.MONTH));
            yearBox.setSelectedItem(String.valueOf(cal.get(Calendar.YEAR)));
        }

        public Date getDate() {
            try {
                int day = Integer.parseInt((String) dayBox.getSelectedItem());
                int month = monthBox.getSelectedIndex();
                int year = Integer.parseInt((String) yearBox.getSelectedItem());

                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, month);
                cal.set(Calendar.DAY_OF_MONTH, 1); // Set to 1st to avoid invalid date issues
                
                // Get maximum days for the selected month
                int maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
                day = Math.min(day, maxDays);
                
                cal.set(Calendar.DAY_OF_MONTH, day);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);

                return cal.getTime();
            } catch (Exception e) {
                e.printStackTrace();
                return new Date();
            }
        }
    }
}
