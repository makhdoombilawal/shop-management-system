package util;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Enterprise Theme Manager for Shop Management System
 * Provides consistent styling, colors, fonts, and component configurations
 * for full HD desktop applications (1920x1080 and higher)
 * 
 * @author Shop Management System
 * @version 2.0 - Enterprise Edition
 */
public class EnterpriseTheme {
    
    // ================================================================
    // SCREEN DIMENSIONS - Full HD Optimization
    // ================================================================
    public static final Dimension FULL_HD = new Dimension(1920, 1080);
    public static final Dimension HD = new Dimension(1280, 720);
    public static final Dimension MAIN_FRAME_SIZE = new Dimension(1800, 1000); // Slightly smaller than full screen
    public static final Dimension POPUP_SMALL = new Dimension(500, 400);
    public static final Dimension POPUP_MEDIUM = new Dimension(700, 600);
    public static final Dimension POPUP_LARGE = new Dimension(1000, 800);
    
    // ================================================================
    // COLOR PALETTE - Modern Professional Theme
    // ================================================================
    
    // Primary Colors
    public static final Color PRIMARY = new Color(37, 99, 235);          // Blue-600
    public static final Color PRIMARY_DARK = new Color(29, 78, 216);     // Blue-700
    public static final Color PRIMARY_LIGHT = new Color(96, 165, 250);   // Blue-400
    public static final Color PRIMARY_LIGHTER = new Color(191, 219, 254); // Blue-200
    
    // Accent Colors
    public static final Color ACCENT = new Color(79, 70, 229);           // Indigo-600
    public static final Color ACCENT_LIGHT = new Color(165, 180, 252);   // Indigo-300
    
    // Status Colors
    public static final Color SUCCESS = new Color(34, 197, 94);          // Green-500
    public static final Color SUCCESS_LIGHT = new Color(134, 239, 172);  // Green-300
    public static final Color WARNING = new Color(251, 146, 60);         // Orange-400
    public static final Color WARNING_LIGHT = new Color(253, 186, 116);  // Orange-300
    public static final Color DANGER = new Color(239, 68, 68);           // Red-500
    public static final Color DANGER_LIGHT = new Color(252, 165, 165);   // Red-300
    public static final Color INFO = new Color(14, 165, 233);            // Sky-500
    public static final Color INFO_LIGHT = new Color(125, 211, 252);     // Sky-300
    
    // Neutral Colors
    public static final Color BACKGROUND = new Color(249, 250, 251);     // Gray-50
    public static final Color PANEL_BG = new Color(255, 255, 255);       // White
    public static final Color SIDEBAR_BG = new Color(31, 41, 55);        // Gray-800
    public static final Color HEADER_BG = new Color(17, 24, 39);         // Gray-900
    public static final Color CARD_BG = new Color(255, 255, 255);        // White
    public static final Color HOVER_BG = new Color(243, 244, 246);       // Gray-100
    public static final Color SELECTED_BG = new Color(219, 234, 254);    // Blue-100
    public static final Color BORDER = new Color(229, 231, 235);         // Gray-200
    public static final Color DIVIDER = new Color(209, 213, 219);        // Gray-300
    
    // Text Colors
    public static final Color TEXT_PRIMARY = new Color(17, 24, 39);      // Gray-900
    public static final Color TEXT_SECONDARY = new Color(107, 114, 128); // Gray-500
    public static final Color TEXT_MUTED = new Color(156, 163, 175);     // Gray-400
    public static final Color TEXT_ON_PRIMARY = new Color(255, 255, 255); // White
    public static final Color TEXT_ON_DARK = new Color(243, 244, 246);   // Gray-100
    
    // Table Colors
    public static final Color TABLE_HEADER_BG = new Color(243, 244, 246); // Gray-100
    public static final Color TABLE_ROW_EVEN = new Color(255, 255, 255);  // White
    public static final Color TABLE_ROW_ODD = new Color(249, 250, 251);   // Gray-50
    public static final Color TABLE_SELECTION = new Color(219, 234, 254); // Blue-100
    public static final Color TABLE_GRID = new Color(229, 231, 235);      // Gray-200
    
    // Special Colors
    public static final Color LOW_STOCK = new Color(254, 226, 226);      // Red-100
    public static final Color OUT_OF_STOCK = new Color(254, 202, 202);   // Red-200
    public static final Color GOOD_STOCK = new Color(220, 252, 231);     // Green-100
    
    // ================================================================
    // FONTS - Professional Typography
    // ================================================================
    
    private static final String[] FONT_FAMILIES = {
        "Segoe UI", "SF Pro Display", "Roboto", "Arial", "Helvetica", "sans-serif"
    };
    
    private static final String FONT_FAMILY = getFontFamily();
    
    public static final Font FONT_HEADER_LARGE = new Font(FONT_FAMILY, Font.BOLD, 28);
    public static final Font FONT_HEADER = new Font(FONT_FAMILY, Font.BOLD, 22);
    public static final Font FONT_SUBHEADER = new Font(FONT_FAMILY, Font.BOLD, 18);
    public static final Font FONT_TITLE = new Font(FONT_FAMILY, Font.BOLD, 16);
    public static final Font FONT_BODY = new Font(FONT_FAMILY, Font.PLAIN, 14);
    public static final Font FONT_BODY_BOLD = new Font(FONT_FAMILY, Font.BOLD, 14);
    public static final Font FONT_SMALL = new Font(FONT_FAMILY, Font.PLAIN, 12);
    public static final Font FONT_SMALL_BOLD = new Font(FONT_FAMILY, Font.BOLD, 12);
    public static final Font FONT_TINY = new Font(FONT_FAMILY, Font.PLAIN, 11);
    public static final Font FONT_INPUT = new Font(FONT_FAMILY, Font.PLAIN, 14);
    public static final Font FONT_BUTTON = new Font(FONT_FAMILY, Font.BOLD, 14);
    public static final Font FONT_TABLE_HEADER = new Font(FONT_FAMILY, Font.BOLD, 13);
    public static final Font FONT_TABLE_CELL = new Font(FONT_FAMILY, Font.PLAIN, 13);
    
    // ================================================================
    // SPACING & SIZING
    // ================================================================
    
    public static final int PADDING_SMALL = 8;
    public static final int PADDING_MEDIUM = 16;
    public static final int PADDING_LARGE = 24;
    public static final int PADDING_XLARGE = 32;
    
    public static final int MARGIN_SMALL = 8;
    public static final int MARGIN_MEDIUM = 16;
    public static final int MARGIN_LARGE = 24;
    
    public static final int BUTTON_HEIGHT = 35;  // Standard button height
    public static final int BUTTON_WIDTH = 120;  // Standard button width
    public static final int INPUT_HEIGHT = 36;
    public static final int COMPONENT_GAP = 12;
    public static final int TABLE_ROW_HEIGHT = 28; // Standard table row height
    
    public static final int BORDER_RADIUS = 8;
    public static final int BORDER_RADIUS_SMALL = 4;
    public static final int BORDER_RADIUS_LARGE = 12;
    
    // ================================================================
    // COMPONENT STYLING METHODS
    // ================================================================
    
    /**
     * Get available font family from system
     */
    private static String getFontFamily() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] availableFonts = ge.getAvailableFontFamilyNames();
        
        for (String preferredFont : FONT_FAMILIES) {
            for (String availableFont : availableFonts) {
                if (availableFont.equalsIgnoreCase(preferredFont)) {
                    return preferredFont;
                }
            }
        }
        return "SansSerif"; // Fallback
    }
    
    /**
     * Style primary action button
     */
    public static void stylePrimaryButton(JButton button) {
        styleButton(button, PRIMARY, PRIMARY_DARK, TEXT_ON_PRIMARY);
    }
    
    /**
     * Style success button (e.g., Save, Submit)
     */
    public static void styleSuccessButton(JButton button) {
        styleButton(button, SUCCESS, SUCCESS.darker(), TEXT_ON_PRIMARY);
    }
    
    /**
     * Style danger button (e.g., Delete, Cancel)
     */
    public static void styleDangerButton(JButton button) {
        styleButton(button, DANGER, DANGER.darker(), TEXT_ON_PRIMARY);
    }
    
    /**
     * Style warning button
     */
    public static void styleWarningButton(JButton button) {
        styleButton(button, WARNING, WARNING.darker(), TEXT_ON_PRIMARY);
    }
    
    /**
     * Style secondary button (outlined)
     */
    public static void styleSecondaryButton(JButton button) {
        button.setBackground(PANEL_BG);
        button.setForeground(PRIMARY);
        button.setFont(FONT_BUTTON);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setBorder(new LineBorder(PRIMARY, 2, true));
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        setStandardButtonSize(button);
        
        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(PRIMARY_LIGHTER);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(PANEL_BG);
            }
        });
    }
    
    /**
     * Set standard button dimensions (120px x 35px)
     */
    public static void setStandardButtonSize(JButton button) {
        button.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        button.setMinimumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        button.setMaximumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
    }
    
    /**
     * Set standard table row height (28px)
     */
    public static void setStandardTableRowHeight(JTable table) {
        table.setRowHeight(TABLE_ROW_HEIGHT);
    }
    
    /**
     * Generic button styling with custom colors
     */
    public static void styleButton(JButton button, Color bgColor, Color hoverColor, Color textColor) {
        button.setBackground(bgColor);
        button.setForeground(textColor);
        button.setFont(FONT_BUTTON);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        setStandardButtonSize(button);
        
        // Smooth hover effect
        button.addMouseListener(new MouseAdapter() {
            Color originalColor = bgColor;
            
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(originalColor);
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                button.setBackground(hoverColor.darker());
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                button.setBackground(hoverColor);
            }
        });
    }
    
    /**
     * Style text field
     */
    public static void styleTextField(JTextField textField) {
        textField.setFont(FONT_INPUT);
        textField.setForeground(TEXT_PRIMARY);
        textField.setBackground(PANEL_BG);
        textField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        textField.setPreferredSize(new Dimension(textField.getPreferredSize().width, INPUT_HEIGHT));
        
        // Focus effect
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                textField.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(PRIMARY, 2, true),
                    BorderFactory.createEmptyBorder(7, 11, 7, 11)
                ));
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                textField.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDER, 1, true),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            }
        });
    }
    
    /**
     * Style text area
     */
    public static void styleTextArea(JTextArea textArea) {
        textArea.setFont(FONT_BODY);
        textArea.setForeground(TEXT_PRIMARY);
        textArea.setBackground(PANEL_BG);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
    }
    
    /**
     * Style combo box
     */
    public static void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(FONT_INPUT);
        comboBox.setForeground(TEXT_PRIMARY);
        comboBox.setBackground(PANEL_BG);
        comboBox.setPreferredSize(new Dimension(comboBox.getPreferredSize().width, INPUT_HEIGHT));
    }
    
    /**
     * Style label
     */
    public static void styleLabel(JLabel label, Font font, Color color) {
        label.setFont(font);
        label.setForeground(color);
    }
    
    /**
     * Style header label
     */
    public static void styleHeaderLabel(JLabel label) {
        styleLabel(label, FONT_HEADER, TEXT_PRIMARY);
    }
    
    /**
     * Style subheader label
     */
    public static void styleSubheaderLabel(JLabel label) {
        styleLabel(label, FONT_SUBHEADER, TEXT_PRIMARY);
    }
    
    /**
     * Style body label
     */
    public static void styleBodyLabel(JLabel label) {
        styleLabel(label, FONT_BODY, TEXT_SECONDARY);
    }
    
    /**
     * Style panel with card appearance
     */
    public static void styleCard(JPanel panel) {
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM)
        ));
    }
    
    /**
     * Style panel with rounded border
     */
    public static void styleRoundedPanel(JPanel panel, Color bgColor) {
        panel.setBackground(bgColor);
        panel.setBorder(new RoundedBorder(BORDER_RADIUS, BORDER));
    }
    
    /**
     * Style table with modern appearance
     */
    public static void styleTable(JTable table) {
        // Table appearance
        table.setFont(FONT_TABLE_CELL);
        table.setForeground(TEXT_PRIMARY);
        table.setBackground(TABLE_ROW_EVEN);
        table.setSelectionBackground(TABLE_SELECTION);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setGridColor(TABLE_GRID);
        setStandardTableRowHeight(table);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setAutoCreateRowSorter(true); // Enable sorting
        
        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_TABLE_HEADER);
        header.setForeground(TEXT_PRIMARY);
        header.setBackground(TABLE_HEADER_BG);
        header.setBorder(new LineBorder(TABLE_GRID));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 40));
        header.setReorderingAllowed(false);
        
        // Alternating row colors
        table.setDefaultRenderer(Object.class, new AlternatingRowRenderer());
        
        // Center align numeric columns if possible
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
    }
    
    /**
     * Style scroll pane
     */
    public static void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(new LineBorder(BORDER, 1, true));
        scrollPane.getViewport().setBackground(PANEL_BG);
    }
    
    /**
     * Create titled border
     */
    public static TitledBorder createTitledBorder(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(
            new LineBorder(BORDER, 1, true),
            title,
            TitledBorder.LEFT,
            TitledBorder.TOP,
            FONT_TITLE,
            TEXT_PRIMARY
        );
        return border;
    }
    
    /**
     * Create metric panel (for dashboard)
     */
    public static JPanel createMetricPanel(String title, String value, Color accentColor) {
        JPanel panel = new JPanel(new BorderLayout(0, PADDING_SMALL));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(PADDING_LARGE, PADDING_LARGE, PADDING_LARGE, PADDING_LARGE)
        ));
        
        // Title label
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_SMALL);
        titleLabel.setForeground(TEXT_SECONDARY);
        
        // Value label
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(FONT_HEADER_LARGE);
        valueLabel.setForeground(accentColor);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Show success notification
     */
    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Show error notification
     */
    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Show warning notification
     */
    public static void showWarning(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }
    
    /**
     * Show confirmation dialog
     */
    public static boolean showConfirm(Component parent, String message) {
        int result = JOptionPane.showConfirmDialog(parent, message, "Confirm", 
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }
    
    /**
     * Center frame on screen
     */
    public static void centerOnScreen(Window window) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - window.getWidth()) / 2;
        int y = (screenSize.height - window.getHeight()) / 2;
        window.setLocation(x, y);
    }
    
    /**
     * Maximize frame to screen (with taskbar consideration)
     */
    public static void maximizeFrame(JFrame frame) {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        frame.setMaximizedBounds(env.getMaximumWindowBounds());
        frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
    }
    
    /**
     * Set frame to main size and center
     */
    public static void prepareMainFrame(JFrame frame, String title) {
        frame.setTitle(title);
        frame.setSize(MAIN_FRAME_SIZE);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        centerOnScreen(frame);
    }
    
    /**
     * Set frame to full screen
     */
    public static void prepareFullScreenFrame(JFrame frame, String title) {
        frame.setTitle(title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        maximizeFrame(frame);
    }
    
    /**
     * Prepare popup frame
     */
    public static void preparePopupFrame(JFrame frame, String title, Dimension size) {
        frame.setTitle(title);
        frame.setSize(size);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        centerOnScreen(frame);
    }
    
    /**
     * Prepare popup dialog (modal)
     */
    public static void preparePopupDialog(JDialog dialog, String title, Dimension size) {
        dialog.setTitle(title);
        dialog.setSize(size);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setModal(true);
        centerOnScreen(dialog);
    }
    
    // ================================================================
    // CUSTOM COMPONENTS
    // ================================================================
    
    /**
     * Rounded border implementation
     */
    static class RoundedBorder extends AbstractBorder {
        private int radius;
        private Color borderColor;
        
        public RoundedBorder(int radius, Color borderColor) {
            this.radius = radius;
            this.borderColor = borderColor;
        }
        
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(borderColor);
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2d.dispose();
        }
        
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM);
        }
    }
    
    /**
     * Alternating row renderer for tables
     */
    static class AlternatingRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (!isSelected) {
                if (row % 2 == 0) {
                    c.setBackground(TABLE_ROW_EVEN);
                } else {
                    c.setBackground(TABLE_ROW_ODD);
                }
                c.setForeground(TEXT_PRIMARY);
            } else {
                c.setBackground(TABLE_SELECTION);
                c.setForeground(TEXT_PRIMARY);
            }
            
            ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            
            return c;
        }
    }
    
    /**
     * Create search field with icon
     */
    public static JTextField createSearchField(String placeholder) {
        JTextField searchField = new JTextField(placeholder);
        styleTextField(searchField);
        searchField.setForeground(TEXT_MUTED);
        
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals(placeholder)) {
                    searchField.setText("");
                    searchField.setForeground(TEXT_PRIMARY);
                }
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText(placeholder);
                    searchField.setForeground(TEXT_MUTED);
                }
            }
        });
        
        return searchField;
    }
    
    /**
     * Apply global look and feel
     */
    public static void applyGlobalTheme() {
        try {
            // Try to use system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Customize UI defaults
            UIManager.put("Button.font", FONT_BUTTON);
            UIManager.put("Label.font", FONT_BODY);
            UIManager.put("TextField.font", FONT_INPUT);
            UIManager.put("TextArea.font", FONT_BODY);
            UIManager.put("ComboBox.font", FONT_INPUT);
            UIManager.put("Table.font", FONT_TABLE_CELL);
            UIManager.put("TableHeader.font", FONT_TABLE_HEADER);
            
            UIManager.put("Button.background", PRIMARY);
            UIManager.put("Button.foreground", TEXT_ON_PRIMARY);
            UIManager.put("Panel.background", BACKGROUND);
            
        } catch (Exception e) {
            util.LoggerUtil.logError("Failed to set look and feel: " + e.getMessage(), null);
        }
    }
}
