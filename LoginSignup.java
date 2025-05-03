import java.awt.*;
import java.io.File;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class LoginSignup extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private Connection connection;

    public LoginSignup() {
        setTitle("Login / Signup");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Gradient background panel
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                Color color1 = new Color(44, 62, 80);
                Color color2 = new Color(52, 152, 219);
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        backgroundPanel.setLayout(new GridBagLayout());

        // Glassy form panel
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(new Color(255, 255, 255, 60));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(50, 60, 50, 60));
        contentPanel.setLayout(new BorderLayout(20, 20));
        contentPanel.setMaximumSize(new Dimension(500, 420));

        // Title
        JLabel titleLabel = new JLabel("Welcome to Expense Tracker", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        contentPanel.add(titleLabel, BorderLayout.NORTH);

        // Form fields panel
        JPanel formPanel = new JPanel();
        formPanel.setOpaque(false);
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Username field
        JLabel userLabel = new JLabel("Username:");
        styleFormLabel(userLabel);
        usernameField = createRoundedTextField();
        formPanel.add(userLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(usernameField);
        formPanel.add(Box.createVerticalStrut(15));

        // Password field
        JLabel passLabel = new JLabel("Password:");
        styleFormLabel(passLabel);
        passwordField = createRoundedPasswordField();
        formPanel.add(passLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(passwordField);

        contentPanel.add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.setOpaque(false);
        JButton dashboardButton = createStyledButton("Login to Dashboard");
        JButton expenseButton = createStyledButton("Login to Expenses");
        JButton signupButton = createStyledButton("Signup");
        buttonPanel.add(dashboardButton);
        buttonPanel.add(expenseButton);
        buttonPanel.add(signupButton);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        backgroundPanel.add(contentPanel);
        add(backgroundPanel);

        // Database setup
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:main.db");
            initializeDatabase();
        } catch (SQLException e) {
            showStyledMessage(this, "Database connection failed!", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        // Button actions
        dashboardButton.addActionListener(e -> login("dashboard"));
        expenseButton.addActionListener(e -> login("expenses"));
        signupButton.addActionListener(e -> signup());

        setVisible(true);
    }

    private void styleFormLabel(JLabel label) {
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        label.setForeground(Color.WHITE);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private JTextField createRoundedTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("SansSerif", Font.PLAIN, 16));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        return field;
    }

    private JPasswordField createRoundedPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("SansSerif", Font.PLAIN, 16));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        return field;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(41, 128, 185));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(31, 97, 141));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(41, 128, 185));
            }
        });
        return button;
    }

    private void initializeDatabase() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL);");
        } catch (SQLException e) {
            showStyledMessage(this, "Database initialization error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void login(String destination) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            showStyledMessage(this, "Please enter both username and password", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (PreparedStatement stmt = connection.prepareStatement("SELECT id FROM users WHERE username = ? AND password = ?")) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("id");
                showStyledMessage(this, "Login successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                if (destination.equals("dashboard")) {
                    openDashboard(userId);
                } else {
                    new ExpenseTracker(userId).setVisible(true);
                }
            } else {
                showStyledMessage(this, "Invalid username or password", "Login Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            showStyledMessage(this, "Login failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void signup() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            showStyledMessage(this, "Please enter both username and password", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int userId = keys.getInt(1);
                createUserDatabase(userId);
                showStyledMessage(this, "Signup successful! You can now login.", "Signup Success", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE")) {
                showStyledMessage(this, "Username already exists!", "Signup Error", JOptionPane.ERROR_MESSAGE);
            } else {
                showStyledMessage(this, "Signup failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openDashboard(int userId) {
        this.dispose();
        String dbName = "user_" + userId + ".db";
        try {
            Connection userConn = DriverManager.getConnection("jdbc:sqlite:" + dbName);
            JFrame frame = new JFrame("Dashboard");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setUndecorated(false);
            frame.setSize(1000, 700);
            frame.setLocationRelativeTo(null);
            frame.add(new DashboardPanel(userConn, userId));
            frame.setVisible(true);
        } catch (SQLException e) {
            showStyledMessage(this, "Could not load dashboard: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createUserDatabase(int userId) {
        String dbName = "user_" + userId + ".db";
        File dbFile = new File(dbName);
        try (Connection userConn = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
             Statement stmt = userConn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS expenses (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT, " +
                    "amount REAL, " +
                    "category TEXT, " +
                    "date TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS categories (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT UNIQUE)");

        } catch (SQLException e) {
            showStyledMessage(this, "Failed to create user database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showStyledMessage(Component parent, String message, String title, int messageType) {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel label = new JLabel("<html><div style='text-align: center;'>" + message + "</div></html>");
        label.setFont(new Font("SansSerif", Font.PLAIN, 16));
        label.setForeground(new Color(52, 73, 94));
        panel.add(label);

        UIManager.put("OptionPane.messageForeground", new Color(52, 73, 94));
        UIManager.put("Panel.background", Color.WHITE);
        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("Button.background", new Color(41, 128, 185));
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.font", new Font("SansSerif", Font.BOLD, 14));
        UIManager.put("Button.border", BorderFactory.createEmptyBorder(5, 15, 5, 15));

        JOptionPane.showMessageDialog(parent, panel, title, messageType);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginSignup::new);
    }
}
