import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.*;

public class ManageCategoriesDialog extends JDialog {
    private JTextField categoryField;
    private JButton addButton, deleteButton;
    private JList<String> categoryList;
    private DefaultListModel<String> listModel;
    private Connection connection;

    public ManageCategoriesDialog(ExpenseTracker parent, Connection connection, int userId) {
        super(parent, "Manage Categories", true);
        this.connection = connection;

        setLayout(new BorderLayout(10, 10));
        setSize(600, 400);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(new Color(44, 62, 80));

        listModel = new DefaultListModel<>();
        categoryList = new JList<>(listModel);
        categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryList.setBackground(new Color(236, 240, 241));
        categoryList.setFont(new Font("SansSerif", Font.PLAIN, 14));
        loadCategories();

        JScrollPane scrollPane = new JScrollPane(categoryList);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        JPanel panel = new JPanel(new FlowLayout());
        panel.setBackground(new Color(52, 73, 94));
        categoryField = new JTextField(15);
        categoryField.setFont(new Font("SansSerif", Font.PLAIN, 14));

        addButton = createStyledButton("Add");
        deleteButton = createStyledButton("Delete");

        panel.add(new JLabel("New Category:"));
        panel.add(categoryField);
        panel.add(addButton);
        panel.add(deleteButton);

        add(panel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addCategory());
        deleteButton.addActionListener(e -> deleteCategory());
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(52, 152, 219));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(120, 40));
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

    private void loadCategories() {
        listModel.clear();
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT name FROM categories ORDER BY name")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                listModel.addElement(rs.getString("name"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load categories: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addCategory() {
        String category = categoryField.getText().trim();
        if (category.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a category name.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (listModel.contains(category)) {
            JOptionPane.showMessageDialog(this, "Category already exists!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO categories (name) VALUES (?)")) {
            stmt.setString(1, category);
            stmt.executeUpdate();
            loadCategories();
            categoryField.setText("");
            categoryList.setSelectedValue(category, true);
            JOptionPane.showMessageDialog(this, "Category added successfully!");
        } catch (SQLException e) {
            if (e.getMessage().toLowerCase().contains("unique")) {
                JOptionPane.showMessageDialog(this, "Category already exists in the database!", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add category: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteCategory() {
        String selectedCategory = categoryList.getSelectedValue();
        if (selectedCategory == null) {
            JOptionPane.showMessageDialog(this, "Select a category to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this category?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM categories WHERE name = ?")) {
            stmt.setString(1, selectedCategory);
            stmt.executeUpdate();
            loadCategories();
            JOptionPane.showMessageDialog(this, "Category deleted successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to delete category: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
