package frames;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

/**
 * Blocking full-screen dialog disguised as a server maintenance screen.
 * Prevents unauthorized use after subscription expiry.
 */
public class SubscriptionExpiryDialog extends JDialog {

    private Long subscriptionId;
    private OTPEntryDialog.OTPVerificationCallback callback;

    public SubscriptionExpiryDialog(Long subscriptionId, LocalDate expiryDate) {
        super((Frame) null, "System Temporarily Unavailable", true);
        this.subscriptionId = subscriptionId;
        initializeUI();
    }

    public SubscriptionExpiryDialog(Long subscriptionId, LocalDate expiryDate, OTPEntryDialog.OTPVerificationCallback callback) {
        this(subscriptionId, expiryDate);
        this.callback = callback;
    }

    private void initializeUI() {
        // Undecorated full screen blocking dialog
        setUndecorated(true);
        setResizable(false);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); // Cannot be closed by Alt+F4 easily

        // Set to full screen dimensions
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize);
        setLocation(0, 0);

        // Main Panel (Flex Centered Layout)
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(243, 244, 246)); // Slate gray light background

        JPanel centerCard = new JPanel();
        centerCard.setLayout(new BoxLayout(centerCard, BoxLayout.Y_AXIS));
        centerCard.setBackground(Color.WHITE);
        centerCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true),
                BorderFactory.createEmptyBorder(40, 50, 40, 50)
        ));
        centerCard.setPreferredSize(new Dimension(650, 420));
        centerCard.setMaximumSize(new Dimension(650, 420));

        // 1. Gear/Maintenance Symbol
        JLabel lblIcon = new JLabel("⚙");
        lblIcon.setFont(new Font("Segoe UI", Font.PLAIN, 80));
        lblIcon.setForeground(new Color(59, 130, 246)); // Premium Modern Blue
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerCard.add(lblIcon);
        centerCard.add(Box.createVerticalStrut(20));

        // 2. Title
        JLabel lblTitle = new JLabel("System Temporarily Unavailable");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(17, 24, 39)); // Dark Charcoal
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerCard.add(lblTitle);
        centerCard.add(Box.createVerticalStrut(15));

        // 3. Message
        JLabel lblMessage = new JLabel("<html><center>" +
                "<p style='font-size:13px; line-height:1.5; color:#4b5563;'>" +
                "Server is currently under maintenance.<br>" +
                "Please contact your system administrator to restore access." +
                "</p>" +
                "</center></html>");
        lblMessage.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerCard.add(lblMessage);
        centerCard.add(Box.createVerticalStrut(40));

        // 4. Buttons Panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonsPanel.setBackground(Color.WHITE);

        JButton btnEnterOtp = new JButton("Enter OTP Code");
        btnEnterOtp.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnEnterOtp.setBackground(new Color(59, 130, 246));
        btnEnterOtp.setForeground(Color.WHITE);
        btnEnterOtp.setPreferredSize(new Dimension(180, 45));
        btnEnterOtp.setFocusPainted(false);
        btnEnterOtp.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEnterOtp.addActionListener(e -> showOTPDialog());

        JButton btnExit = new JButton("Exit System");
        btnExit.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnExit.setBackground(new Color(107, 114, 128)); // Neutral gray
        btnExit.setForeground(Color.WHITE);
        btnExit.setPreferredSize(new Dimension(160, 45));
        btnExit.setFocusPainted(false);
        btnExit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExit.addActionListener(e -> System.exit(0));

        buttonsPanel.add(btnEnterOtp);
        buttonsPanel.add(btnExit);

        centerCard.add(buttonsPanel);

        // Center card inside screen container
        mainPanel.add(centerCard);
        setContentPane(mainPanel);
    }

    private void showOTPDialog() {
        OTPEntryDialog.OTPVerificationCallback otpCallback = new OTPEntryDialog.OTPVerificationCallback() {
            @Override
            public void onVerificationSuccess(LocalDate newExpiryDate) {
                if (callback != null) {
                    callback.onVerificationSuccess(newExpiryDate);
                }
                dispose();
                // Close and restart app
                System.exit(0);
            }

            @Override
            public void onVerificationFailed(String errorMessage) {
                if (callback != null) {
                    callback.onVerificationFailed(errorMessage);
                }
                // Keep dialog open for retry
            }
        };

        OTPEntryDialog.showOTPDialog(subscriptionId, otpCallback);
    }

    public static void showExpiryDialog(Long subscriptionId, LocalDate expiryDate) {
        SwingUtilities.invokeLater(() -> {
            SubscriptionExpiryDialog dialog = new SubscriptionExpiryDialog(subscriptionId, expiryDate);
            dialog.setVisible(true);
        });
    }

    public static void showExpiryDialog(Long subscriptionId, LocalDate expiryDate,
                                        OTPEntryDialog.OTPVerificationCallback callback) {
        SwingUtilities.invokeLater(() -> {
            SubscriptionExpiryDialog dialog = new SubscriptionExpiryDialog(subscriptionId, expiryDate, callback);
            dialog.setVisible(true);
        });
    }
}
