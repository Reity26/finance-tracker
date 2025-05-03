import java.awt.*;
import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;


{setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(false);
        setTitle("Expense Tracker");
        setSize(1100, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
        initializeUserDatabase(userId);
        loadExpenses();
    }
    public class ExpenseTracker extends JFrame {
        private JTable table;
        private DefaultTableModel model;
        private Connection connection;
        private JLabel totalExpenseLabel;
        private static final double EXPENSE_LIMIT = 15000.0;
        private int userId;

        public ExpenseTracker(int userId) {
            this.userId = userId;

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            dispose();
            SwingUtilities.invokeLater(() -> new LoginSignup().setVisible(true));
        }
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(44, 62, 80));
        setContentPane(mainPanel);
    
        model = new DefaultTableModel(new String[]{"ID", "Name", "Amount", "Category", "Date"}, 0);
        table = new JTable(model);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.setBackground(new Color(236, 240, 241));
        table.setRowHeight(25);
        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);
    
        // === Bottom Panel with Logout, Center Button, and Total ===
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(52, 73, 94));
    
        // Logout button
        JButton logoutButton = createStyledButton("Logout");
        logoutButton.setPreferredSize(new Dimension(120, 40));
        logoutButton.addActionListener(e -> logout());
        bottomPanel.add(logoutButton, BorderLayout.WEST);
    
        // Center panel for dashboard button
        JPanel centerBottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerBottomPanel.setBackground(new Color(52, 73, 94));
    
        JButton dashboardButton = new JButton("Go to Dashboard");
        dashboardButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        dashboardButton.setBackground(new Color(41, 128, 185));
        dashboardButton.setForeground(Color.WHITE);
        dashboardButton.setFocusPainted(false);
        centerBottomPanel.add(dashboardButton);
    
        // On click: dispose tracker and open dashboard
        dashboardButton.addActionListener(e -> {
            dispose(); // Close current JFrame
            JFrame dashboardFrame = new JFrame("Dashboard");
            dashboardFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            dashboardFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            dashboardFrame.setUndecorated(false);
            dashboardFrame.setSize(1000, 700);
            dashboardFrame.setLocationRelativeTo(null);
            dashboardFrame.setContentPane(new DashboardPanel(connection, userId));
            dashboardFrame.setVisible(true);
        });
    
        bottomPanel.add(centerBottomPanel, BorderLayout.CENTER);
    
        // Total expense label
        totalExpenseLabel = new JLabel("Total Expense: $0.00", SwingConstants.RIGHT);
        totalExpenseLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        totalExpenseLabel.setForeground(Color.WHITE);
        totalExpenseLabel.setOpaque(true);
        totalExpenseLabel.setBackground(new Color(52, 152, 219));
        totalExpenseLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 20));
        bottomPanel.add(totalExpenseLabel, BorderLayout.EAST);
    
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
    
        // === Top Panel with Centered Buttons ===
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(41, 128, 185));
    
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(new Color(41, 128, 185));
    
        JButton addButton = createStyledButton("Add Expense");
        JButton editButton = createStyledButton("Edit Expense");
        JButton deleteButton = createStyledButton("Delete Expense");
        JButton filterDateButton = createStyledButton("Filter by Date Range");
        JButton manageCategoriesButton = createStyledButton("Manage Categories");
        JButton pieChartButton = createStyledButton("Show Pie Chart");
    
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(filterDateButton);
        buttonPanel.add(manageCategoriesButton);
        buttonPanel.add(pieChartButton);
    
        topPanel.add(buttonPanel, BorderLayout.CENTER);
        mainPanel.add(topPanel, BorderLayout.NORTH);
    
        // === Action Listeners ===
        addButton.addActionListener(e -> new AddExpenseDialog(this, connection, userId, null).setVisible(true));
        editButton.addActionListener(e -> editSelectedExpense());
        deleteButton.addActionListener(e -> deleteSelectedExpense());
        filterDateButton.addActionListener(e -> showDateRangeDialog());
        manageCategoriesButton.addActionListener(e -> new ManageCategoriesDialog(this, connection, userId).setVisible(true));
        pieChartButton.addActionListener(e -> showPieChart());
    }
    
    
    
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(52, 152, 219));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(180, 40));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(41, 128, 185));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(52, 152, 219));
            }
        });
        return button;
    }

    private void initializeUserDatabase(int userId) {
        try {
            String dbName = "user_" + userId + ".db";
            File dbFile = new File(dbName);
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS expenses (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT, " +
                        "amount REAL, " +
                        "category TEXT, " +
                        "date TEXT)");
                stmt.execute("CREATE TABLE IF NOT EXISTS categories (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT UNIQUE)");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to initialize user database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadExpenses() {
        model.setRowCount(0);
        double totalExpense = 0.0;
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM expenses")) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    double amount = rs.getDouble("amount");
                    totalExpense += amount;
                    model.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("name"),
                            amount,
                            rs.getString("category"),
                            rs.getString("date")
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load expenses: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        totalExpenseLabel.setText("Total Expense: " + "\u20B9" + String.format("%.2f", totalExpense));
        if (totalExpense > EXPENSE_LIMIT) {
            JOptionPane.showMessageDialog(this, "Warning: Expense limit exceeded!", "Alert", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void showDateRangeDialog() {
        JDialog dialog = new JDialog(this, "Select Date Range", true);
        dialog.setSize(400, 200);
        dialog.setLayout(new GridLayout(3, 2, 10, 10));
        dialog.setLocationRelativeTo(this);

        JLabel fromLabel = new JLabel("From:");
        JLabel toLabel = new JLabel("To:");

        com.toedter.calendar.JDateChooser fromDateChooser = new com.toedter.calendar.JDateChooser();
        com.toedter.calendar.JDateChooser toDateChooser = new com.toedter.calendar.JDateChooser();

        Calendar cal = Calendar.getInstance();
        Date today = cal.getTime();
        toDateChooser.setDate(today);
        cal.add(Calendar.MONTH, -1);
        fromDateChooser.setDate(cal.getTime());

        JButton filterButton = new JButton("Filter");
        filterButton.addActionListener(e -> {
            Date fromDate = fromDateChooser.getDate();
            Date toDate = toDateChooser.getDate();

            if (fromDate == null || toDate == null) {
                JOptionPane.showMessageDialog(dialog, "Please select both start and end dates.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (fromDate.after(toDate)) {
                JOptionPane.showMessageDialog(dialog, "Start date must be before end date.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            dialog.dispose();
            showExpensesBetween(fromDate, toDate);
        });

        dialog.add(fromLabel);
        dialog.add(fromDateChooser);
        dialog.add(toLabel);
        dialog.add(toDateChooser);
        dialog.add(new JLabel());
        dialog.add(filterButton);

        dialog.setVisible(true);
    }

    private void showExpensesBetween(Date fromDate, Date toDate) {
        JFrame rangeFrame = new JFrame("Filtered Expenses");
        rangeFrame.setSize(800, 400);
        rangeFrame.setLocationRelativeTo(this);

        DefaultTableModel rangeModel = new DefaultTableModel(new String[]{"ID", "Name", "Amount", "Category", "Date"}, 0);
        JTable rangeTable = new JTable(rangeModel);
        rangeTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        rangeTable.setRowHeight(25);
        rangeTable.setBackground(new Color(236, 240, 241));

        double total = 0.0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT * FROM expenses WHERE date BETWEEN ? AND ?")) {
            stmt.setString(1, sdf.format(fromDate));
            stmt.setString(2, sdf.format(toDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    double amount = rs.getDouble("amount");
                    total += amount;
                    rangeModel.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("name"),
                            amount,
                            rs.getString("category"),
                            rs.getString("date")
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to filter expenses: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JLabel totalLabel = new JLabel("Total: $" + String.format("%.2f", total), SwingConstants.RIGHT);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 20));

        JButton backButton = new JButton("Back");
        backButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        backButton.setBackground(new Color(231, 76, 60));
        backButton.setForeground(Color.WHITE);
        backButton.addActionListener(e -> rangeFrame.dispose());

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(totalLabel, BorderLayout.CENTER);
        bottomPanel.add(backButton, BorderLayout.EAST);

        rangeFrame.add(new JScrollPane(rangeTable), BorderLayout.CENTER);
        rangeFrame.add(bottomPanel, BorderLayout.SOUTH);

        rangeFrame.setVisible(true);
    }

    private void editSelectedExpense() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an expense to edit.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int expenseId = (int) model.getValueAt(selectedRow, 0);
        new AddExpenseDialog(this, connection, userId, expenseId).setVisible(true);
    }

    private void deleteSelectedExpense() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an expense to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int expenseId = (int) model.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this expense?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM expenses WHERE id = ?")) {
                stmt.setInt(1, expenseId);
                stmt.executeUpdate();
                loadExpenses();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Failed to delete expense: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void refreshExpenses() {
        loadExpenses();
    }

    public Connection getConnection() {
        return connection;
    }

    private void showPieChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
    
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT category, SUM(amount) as total FROM expenses GROUP BY category")) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String category = rs.getString("category");
                    double total = rs.getDouble("total");
                    dataset.setValue(category, total);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load chart data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        JFreeChart chart = ChartFactory.createPieChart(
                "Expense Distribution by Category",
                dataset,
                true, true, false
        );
    
        ChartPanel chartPanel = new ChartPanel(chart);
        JFrame chartFrame = new JFrame("Expense Pie Chart");
        chartFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        chartFrame.setSize(600, 400);
        chartFrame.add(chartPanel);
        chartFrame.setLocationRelativeTo(this);
        chartFrame.setVisible(true);
    }
    
}
