import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import com.toedter.calendar.JDateChooser;

public class AddExpenseDialog extends JDialog {
    private JTextField nameField, amountField;
    private JDateChooser dateChooser;
    private JComboBox<String> categoryBox;
    private JButton saveButton, cancelButton;
    private Connection connection;
    private ExpenseTracker parent;
    private Integer expenseId;
    private final String ADD_NEW_CATEGORY = "Add new category...";

    public AddExpenseDialog(ExpenseTracker parent, Connection connection, int userId, Integer expenseId) {
        super(parent, expenseId == null ? "Add Expense" : "Edit Expense", true);
        this.parent = parent;
        this.connection = connection;
        this.expenseId = expenseId;

        setLayout(new GridLayout(6, 2, 10, 10));

        nameField = new JTextField();
        amountField = new JTextField();
        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setDate(new Date()); // Autopopulate with today's date

        categoryBox = new JComboBox<>();
        loadCategories();

        categoryBox.addActionListener(e -> {
            if (categoryBox.getSelectedItem() != null && categoryBox.getSelectedItem().equals(ADD_NEW_CATEGORY)) {
                String newCategory = JOptionPane.showInputDialog(this, "Enter new category name:");
                if (newCategory != null && !newCategory.trim().isEmpty()) {
                    try (PreparedStatement stmt = connection.prepareStatement(
                            "INSERT OR IGNORE INTO categories (name) VALUES (?)")) {
                        stmt.setString(1, newCategory.trim());
                        stmt.executeUpdate();

                        loadCategories();
                        categoryBox.setSelectedItem(newCategory.trim());
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Failed to add new category: " + ex.getMessage());
                    }
                } else {
                    categoryBox.setSelectedIndex(0);
                }
            }
        });

        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");

        add(new JLabel("Name:"));
        add(nameField);
        add(new JLabel("Amount:"));
        add(amountField);
        add(new JLabel("Date (YYYY-MM-DD):"));
        add(dateChooser);
        add(new JLabel("Category:"));
        add(categoryBox);
        add(saveButton);
        add(cancelButton);

        saveButton.addActionListener(e -> saveExpense());
        cancelButton.addActionListener(e -> dispose());

        if (expenseId != null) {
            loadExpenseDetails(); // This will override the default date if editing
        }

        setSize(400, 300);
        setLocationRelativeTo(parent);
    }

    private void loadCategories() {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT name FROM categories ORDER BY name")) {
            ResultSet rs = stmt.executeQuery();
            categoryBox.removeAllItems();
            while (rs.next()) {
                categoryBox.addItem(rs.getString("name"));
            }
            categoryBox.addItem(ADD_NEW_CATEGORY);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load categories: " + e.getMessage());
        }
    }

    private void loadExpenseDetails() {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT * FROM expenses WHERE id = ?")) {
            stmt.setInt(1, expenseId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                nameField.setText(rs.getString("name"));
                amountField.setText(rs.getString("amount"));
                String dateString = rs.getString("date");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date date = sdf.parse(dateString);
                    dateChooser.setDate(date); // override today with actual expense date
                } catch (Exception e) {
                    e.printStackTrace();
                }
                categoryBox.setSelectedItem(rs.getString("category"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load expense: " + e.getMessage());
        }
    }

    private void saveExpense() {
        try {
            String name = nameField.getText().trim();
            String amountStr = amountField.getText().trim();
            Date selectedDate = dateChooser.getDate();
            if (selectedDate == null || name.isEmpty() || amountStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please complete all fields.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            double amount = Double.parseDouble(amountStr);
            String date = new SimpleDateFormat("yyyy-MM-dd").format(selectedDate);
            String category = (String) categoryBox.getSelectedItem();

            if (category == null || category.equals(ADD_NEW_CATEGORY)) {
                JOptionPane.showMessageDialog(this, "Please choose a valid category.");
                return;
            }

            PreparedStatement stmt;
            if (expenseId == null) {
                stmt = connection.prepareStatement(
                        "INSERT INTO expenses (name, amount, date, category) VALUES (?, ?, ?, ?)");
                stmt.setString(1, name);
                stmt.setDouble(2, amount);
                stmt.setString(3, date);
                stmt.setString(4, category);
            } else {
                stmt = connection.prepareStatement(
                        "UPDATE expenses SET name = ?, amount = ?, date = ?, category = ? WHERE id = ?");
                stmt.setString(1, name);
                stmt.setDouble(2, amount);
                stmt.setString(3, date);
                stmt.setString(4, category);
                stmt.setInt(5, expenseId);
            }

            stmt.executeUpdate();
            parent.loadExpenses();  // Refresh the table
            dispose();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Amount must be a valid number.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to save expense: " + e.getMessage());
        }
    }
}
