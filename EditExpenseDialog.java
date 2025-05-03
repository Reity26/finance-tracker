import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EditExpenseDialog extends JDialog {
    private JTextField nameField, amountField, dateField;
    private JComboBox<String> categoryBox;
    private JButton saveButton, cancelButton;

    private Connection connection;
    private int expenseId;
    private ExpenseTracker parent;

    public EditExpenseDialog(ExpenseTracker parent, Connection connection, int expenseId) {
        super(parent, "Edit Expense", true);
        this.parent = parent;
        this.connection = connection;
        this.expenseId = expenseId;

        setLayout(new GridLayout(5, 2, 10, 10));
        setSize(400, 300);
        setLocationRelativeTo(parent);

        add(new JLabel("Name:"));
        nameField = new JTextField();
        add(nameField);

        add(new JLabel("Amount:"));
        amountField = new JTextField();
        add(amountField);

        add(new JLabel("Category:"));
        categoryBox = new JComboBox<>();
        loadCategories();
        add(categoryBox);

        add(new JLabel("Date (YYYY-MM-DD):"));
        dateField = new JTextField();
        add(dateField);

        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel);

        loadExpenseDetails();

        saveButton.addActionListener(this::saveExpense);
        cancelButton.addActionListener(event -> dispose());
    }

    private void loadCategories() {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT name FROM categories");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                categoryBox.addItem(rs.getString("name"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load categories: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadExpenseDetails() {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM expenses WHERE id = ?")) {
            stmt.setInt(1, expenseId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    nameField.setText(rs.getString("name"));
                    amountField.setText(String.valueOf(rs.getDouble("amount")));
                    categoryBox.setSelectedItem(rs.getString("category"));
                    dateField.setText(rs.getString("date"));
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load expense details: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveExpense(ActionEvent event) {
        String name = nameField.getText();
        String amount = amountField.getText();
        String category = (String) categoryBox.getSelectedItem();
        String date = dateField.getText();

        if (name.isEmpty() || amount.isEmpty() || date.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (PreparedStatement stmt = connection.prepareStatement(
                "UPDATE expenses SET name = ?, amount = ?, category = ?, date = ? WHERE id = ?")) {
            stmt.setString(1, name);
            stmt.setDouble(2, Double.parseDouble(amount));
            stmt.setString(3, category);
            stmt.setString(4, date);
            stmt.setInt(5, expenseId);

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Expense updated successfully.");
            parent.loadExpenses();
            dispose();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to update expense: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
