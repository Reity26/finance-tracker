// All imports remain unchanged
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

public class DashboardPanel extends JPanel {
    private Connection connection;
    private int userId;
    private double monthlyTotal;
    private double budgetLimit = 10000.0; // default budget

    public DashboardPanel(Connection connection, int userId) {
        this.connection = connection;
        this.userId = userId;
        setLayout(new BorderLayout());
        setBackground(new Color(34, 49, 63));

        // -------------------- TOP PANEL --------------------
        JPanel topPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        topPanel.setBackground(new Color(34, 49, 63));
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        topPanel.add(createStatCard("Total This Month", "₹" + getMonthlyTotal()));
        topPanel.add(createStatCard("Top Category", getTopCategory()));
        topPanel.add(createStatCard("Avg Daily Spend", "₹" + getAverageDailySpend()));
        topPanel.add(createStatCard("Most Used Category", getMostUsedCategory()));

        add(topPanel, BorderLayout.NORTH);

        // -------------------- CENTER PANEL --------------------
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        centerPanel.setBackground(new Color(34, 49, 63));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        centerPanel.add(createBarChart());
        centerPanel.add(createPieChart());

        add(centerPanel, BorderLayout.CENTER);

        // -------------------- BOTTOM PANEL --------------------
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(34, 49, 63));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

        JTextArea transactionsArea = new JTextArea(getRecentTransactions());
        transactionsArea.setEditable(false);
        transactionsArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        transactionsArea.setMargin(new Insets(10, 10, 10, 10));
        transactionsArea.setBorder(BorderFactory.createTitledBorder("Recent Transactions"));
        bottomPanel.add(new JScrollPane(transactionsArea), BorderLayout.CENTER);

        // -------------------- BOTTOM RIGHT PANEL --------------------
        JPanel bottomRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomRightPanel.setBackground(new Color(34, 49, 63));

        JLabel budgetLabel = new JLabel("Within Budget: ₹" + monthlyTotal + " / ₹" + budgetLimit);
        budgetLabel.setForeground(Color.WHITE);
        budgetLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        JButton openExpenseTrackerBtn = new JButton("Open Expense Tracker");
        openExpenseTrackerBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        openExpenseTrackerBtn.setBackground(new Color(39, 174, 96));
        openExpenseTrackerBtn.setForeground(Color.WHITE);
        openExpenseTrackerBtn.setFocusPainted(false);
        openExpenseTrackerBtn.addActionListener(e -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            frame.dispose();
            new ExpenseTracker(userId).setVisible(true);
        });

        JButton editBudgetBtn = new JButton("Edit Budget");
        editBudgetBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        editBudgetBtn.setBackground(new Color(241, 196, 15));
        editBudgetBtn.setForeground(Color.BLACK);
        editBudgetBtn.setFocusPainted(false);
        editBudgetBtn.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, "Enter Monthly Budget:", budgetLimit);
            try {
                if (input != null) {
                    budgetLimit = Double.parseDouble(input);
                    budgetLabel.setText("Within Budget: ₹" + monthlyTotal + " / ₹" + budgetLimit);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid input. Please enter a valid number.");
            }
        });

        bottomRightPanel.add(editBudgetBtn);
        bottomRightPanel.add(budgetLabel);
        bottomRightPanel.add(openExpenseTrackerBtn);

        // -------------------- BOTTOM LEFT PANEL --------------------
        JPanel bottomLeftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomLeftPanel.setBackground(new Color(34, 49, 63));

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        logoutButton.setBackground(new Color(231, 76, 60));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            frame.dispose();
            new LoginSignup();
        });

        bottomLeftPanel.add(logoutButton);

        // Add both left and right panels to bottom
        JPanel bottomSouthPanel = new JPanel(new BorderLayout());
        bottomSouthPanel.setBackground(new Color(34, 49, 63));
        bottomSouthPanel.add(bottomLeftPanel, BorderLayout.WEST);
        bottomSouthPanel.add(bottomRightPanel, BorderLayout.EAST);

        bottomPanel.add(bottomSouthPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // ---------------------------- SUPPORTING METHODS ----------------------------

    private JPanel createStatCard(String title, String value) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(new Color(52, 152, 219));
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        card.setPreferredSize(new Dimension(150, 80));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        valueLabel.setForeground(Color.WHITE);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private double getMonthlyTotal() {
        monthlyTotal = 0.0;
        try (PreparedStatement stmt = connection.prepareStatement("SELECT SUM(amount) FROM expenses WHERE strftime('%Y-%m', date) = strftime('%Y-%m', 'now')")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                monthlyTotal = rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return monthlyTotal;
    }

    private String getTopCategory() {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT category, SUM(amount) as total FROM expenses GROUP BY category ORDER BY total DESC LIMIT 1")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("category");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "N/A";
    }

    private String getMostUsedCategory() {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT category, COUNT(*) as count FROM expenses GROUP BY category ORDER BY count DESC LIMIT 1")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("category");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "N/A";
    }

    private String getAverageDailySpend() {
        double avg = 0.0;
        try (PreparedStatement stmt = connection.prepareStatement("SELECT AVG(daily_total) FROM (SELECT date, SUM(amount) as daily_total FROM expenses GROUP BY date)")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                avg = rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return String.format("%.2f", avg);
    }

    private JPanel createBarChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        try (PreparedStatement stmt = connection.prepareStatement("SELECT strftime('%Y-%m', date) as month, SUM(amount) FROM expenses GROUP BY month ORDER BY month DESC LIMIT 6")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                dataset.addValue(rs.getDouble(2), "Expenses", rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Expenses Over Time", "Month", "Amount",
                dataset, PlotOrientation.VERTICAL, false, true, false);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setRangeGridlinePaint(Color.GRAY);

        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(new Dimension(400, 250));
        return panel;
    }

    private JPanel createPieChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();

        try (PreparedStatement stmt = connection.prepareStatement("SELECT category, SUM(amount) FROM expenses GROUP BY category")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                dataset.setValue(rs.getString(1), rs.getDouble(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JFreeChart chart = ChartFactory.createPieChart("Category Wise Spend", dataset, true, true, false);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {1} ({2})"));
        plot.setBackgroundPaint(Color.white);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(400, 250));
        return chartPanel;
    }

    private String getRecentTransactions() {
        StringBuilder sb = new StringBuilder();
        try (PreparedStatement stmt = connection.prepareStatement("SELECT name, amount, date FROM expenses ORDER BY date DESC LIMIT 5")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                sb.append(rs.getString("name"))
                        .append(" - ₹")
                        .append(rs.getDouble("amount"))
                        .append(" on ")
                        .append(rs.getString("date"))
                        .append("\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
