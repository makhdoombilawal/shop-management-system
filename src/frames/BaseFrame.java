package frames;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import models.Session;
import java.awt.*;
import java.awt.event.*;

public class BaseFrame extends JFrame {

    protected static boolean authorized = false;
    private static boolean popupShown = false; 

    // 🎨 Modern Color Palette
    public static final Color PRIMARY_COLOR = new Color(59, 130, 246);      // Modern Blue
    public static final Color PRIMARY_DARK = new Color(37, 99, 235);        // Darker Blue
    public static final Color PRIMARY_LIGHT = new Color(147, 197, 253);     // Light Blue
    public static final Color SUCCESS_COLOR = new Color(34, 197, 94);       // Green
    public static final Color DANGER_COLOR = new Color(239, 68, 68);        // Red
    public static final Color WARNING_COLOR = new Color(251, 146, 60);      // Orange
    public static final Color BACKGROUND = new Color(249, 250, 251);        // Light Gray
    public static final Color PANEL_BG = new Color(255, 255, 255);          // White
    public static final Color TEXT_PRIMARY = new Color(17, 24, 39);         // Dark Gray
    public static final Color TEXT_SECONDARY = new Color(107, 114, 128);    // Medium Gray
    public static final Color BORDER_COLOR = new Color(229, 231, 235);      // Light Border
    public static final Color HOVER_BG = new Color(243, 244, 246);          // Hover Background

    public BaseFrame() {
        // Check login status
        if (!Session.isLoggedIn()) {
            authorized = false;

            // show popup
            if (!popupShown) {
                JOptionPane.showMessageDialog(
                        null,
                        "❌ Unauthorized access! Please login first.",
                        "Access Denied",
                        JOptionPane.ERROR_MESSAGE
                );
                popupShown = true;

                // Open login window
                SwingUtilities.invokeLater(() -> new Login().setVisible(true));
            }

            // Close this frame
            dispose();
            return;
        }

        // Logged in → authorize
        authorized = true;
    }
    
    // ✨ Modern Styling Utilities
    
    /**
     * Apply modern button styling with smooth animations
     */
    protected void styleButton(JButton button, Color bgColor, Color hoverColor) {
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Smooth hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
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
     * Style primary action button
     */
    protected void stylePrimaryButton(JButton button) {
        styleButton(button, PRIMARY_COLOR, PRIMARY_DARK);
    }
    
    /**
     * Style success button
     */
    protected void styleSuccessButton(JButton button) {
        styleButton(button, SUCCESS_COLOR, SUCCESS_COLOR.darker());
    }
    
    /**
     * Style danger button
     */
    protected void styleDangerButton(JButton button) {
        styleButton(button, DANGER_COLOR, DANGER_COLOR.darker());
    }
    
    /**
     * Style warning button
     */
    protected void styleWarningButton(JButton button) {
        styleButton(button, WARNING_COLOR, WARNING_COLOR.darker());
    }
    
    /**
     * Style secondary/neutral button
     */
    protected void styleSecondaryButton(JButton button) {
        button.setBackground(PANEL_BG);
        button.setForeground(TEXT_PRIMARY);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 2),
            BorderFactory.createEmptyBorder(8, 18, 8, 18)
        ));
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(HOVER_BG);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(PANEL_BG);
            }
        });
    }
    
    /**
     * Apply modern text field styling
     */
    protected void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(PRIMARY_COLOR, 2),
                    BorderFactory.createEmptyBorder(7, 11, 7, 11)
                ));
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDER_COLOR, 1),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            }
        });
    }
    
    /**
     * Apply modern text area styling
     */
    protected void styleTextArea(JTextArea area) {
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setForeground(TEXT_PRIMARY);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
    }
    
    /**
     * Apply modern combo box styling
     */
    protected void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setForeground(TEXT_PRIMARY);
        comboBox.setBackground(PANEL_BG);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }
    
    /**
     * Apply modern table styling with smooth scrolling
     */
    protected void styleTable(JTable table) {
        // Table styling
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(40);
        table.setShowGrid(true);
        table.setGridColor(BORDER_COLOR);
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setIntercellSpacing(new Dimension(10, 5));
        
        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(BACKGROUND);
        header.setForeground(TEXT_PRIMARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_COLOR));
        header.setPreferredSize(new Dimension(header.getWidth(), 45));
        
        // Center align cells
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }
    
    /**
     * Apply modern scroll pane styling with smooth scrolling
     */
    protected void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scrollPane.getViewport().setBackground(PANEL_BG);
        
        // Smooth scrolling
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        
        // Custom scrollbar styling
        styleScrollBar(scrollPane.getVerticalScrollBar());
        styleScrollBar(scrollPane.getHorizontalScrollBar());
    }
    
    /**
     * Style scrollbar for modern look
     */
    private void styleScrollBar(JScrollBar scrollBar) {
        scrollBar.setBackground(BACKGROUND);
        scrollBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = TEXT_SECONDARY;
                this.thumbDarkShadowColor = TEXT_SECONDARY;
                this.thumbHighlightColor = TEXT_SECONDARY;
                this.thumbLightShadowColor = TEXT_SECONDARY;
                this.trackColor = BACKGROUND;
            }
            
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }
            
            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }
            
            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                return button;
            }
        });
    }
    
    /**
     * Apply modern label styling
     */
    protected void styleLabel(JLabel label, boolean isTitle) {
        if (isTitle) {
            label.setFont(new Font("Segoe UI", Font.BOLD, 24));
            label.setForeground(TEXT_PRIMARY);
        } else {
            label.setFont(new Font("Segoe UI", Font.BOLD, 14));
            label.setForeground(TEXT_SECONDARY);
        }
    }
    
    /**
     * Apply modern panel styling
     */
    protected void stylePanel(JPanel panel) {
        panel.setBackground(PANEL_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
    }
    
    /**
     * Create a modern card-style panel
     */
    protected JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(PANEL_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        return panel;
    }
    
    /**
     * Apply modern frame background
     */
    protected void styleFrame() {
        getContentPane().setBackground(BACKGROUND);
    }
}
