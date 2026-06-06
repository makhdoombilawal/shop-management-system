package frames;

import service.OTPService;
import service.SubscriptionService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * OTP entry dialog for subscription renewal.
 * Disguised as a verification prompt with 15-minute cooldown lockout.
 */
public class OTPEntryDialog extends JDialog {

    private JTextField otpField;
    private JLabel attemptsLabel;
    private JButton submitButton;
    private JButton cancelButton;
    private JLabel messageLabel;
    private OTPVerificationCallback callback;

    private Long subscriptionId;
    private int attemptsRemaining;
    private javax.swing.Timer cooldownTimer;

    public OTPEntryDialog(Long subscriptionId) {
        super((Frame) null, "Verification", true);
        this.subscriptionId = subscriptionId;
        initializeUI();
        setupListeners();
        checkAndStartCooldown();
    }

    public OTPEntryDialog(Long subscriptionId, OTPVerificationCallback callback) {
        this(subscriptionId);
        this.callback = callback;
    }

    private void initializeUI() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        // Icon
        JLabel lblIcon = new JLabel("🔒");
        lblIcon.setFont(new Font("Segoe UI", Font.PLAIN, 40));
        lblIcon.setForeground(new Color(59, 130, 246));
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblIcon);
        mainPanel.add(Box.createVerticalStrut(15));

        // Title
        JLabel titleLabel = new JLabel("Security Verification");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(17, 24, 39));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(10));

        // Description
        JLabel descLabel = new JLabel("<html><center>Please enter the 4-digit verification code provided by your administrator to authorize system launch.</center></html>", SwingConstants.CENTER);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(new Color(107, 114, 128));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        descLabel.setMaximumSize(new Dimension(380, 50));
        mainPanel.add(descLabel);
        mainPanel.add(Box.createVerticalStrut(20));

        // Input Panel
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        inputPanel.setBackground(Color.WHITE);

        JLabel otpLabel = new JLabel("Code:");
        otpLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        otpLabel.setForeground(new Color(55, 65, 81));
        inputPanel.add(otpLabel);

        otpField = new JTextField(6);
        otpField.setFont(new Font("Monospaced", Font.BOLD, 22));
        otpField.setHorizontalAlignment(JTextField.CENTER);
        otpField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(209, 213, 219), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        inputPanel.add(otpField);
        mainPanel.add(inputPanel);
        mainPanel.add(Box.createVerticalStrut(15));

        // Attempts Remaining
        attemptsLabel = new JLabel("Attempts remaining: 3/3");
        attemptsLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        attemptsLabel.setForeground(new Color(16, 185, 129)); // Success green
        attemptsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(attemptsLabel);
        mainPanel.add(Box.createVerticalStrut(15));

        // Status Message Panel
        messageLabel = new JLabel(" ", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        messageLabel.setForeground(Color.RED);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(messageLabel);
        mainPanel.add(Box.createVerticalStrut(15));

        // Buttons Panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonsPanel.setBackground(Color.WHITE);

        submitButton = new JButton("Verify");
        submitButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        submitButton.setBackground(new Color(59, 130, 246));
        submitButton.setForeground(Color.WHITE);
        submitButton.setPreferredSize(new Dimension(130, 38));
        submitButton.setFocusPainted(false);
        submitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cancelButton.setBackground(new Color(243, 244, 246));
        cancelButton.setForeground(new Color(55, 65, 81));
        cancelButton.setPreferredSize(new Dimension(100, 38));
        cancelButton.setFocusPainted(false);
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        buttonsPanel.add(submitButton);
        buttonsPanel.add(cancelButton);
        mainPanel.add(buttonsPanel);

        setContentPane(mainPanel);
        pack();
        setSize(new Dimension(420, 360));
        setLocationRelativeTo(null);
    }

    private void setupListeners() {
        submitButton.addActionListener(e -> handleOTPSubmit());
        cancelButton.addActionListener(e -> dispose());
        otpField.addActionListener(e -> handleOTPSubmit());
    }

    private void checkAndStartCooldown() {
        SubscriptionService.SubscriptionInfo info = SubscriptionService.getSubscriptionInfo();
        if (info.isOtpCooldownActive() && info.getOtpLockedUntil() != null) {
            startCooldownTimer(info.getOtpLockedUntil());
        } else {
            submitButton.setEnabled(true);
            otpField.setEnabled(true);
            attemptsRemaining = info.getOtpAttemptsRemaining();
            attemptsLabel.setText("Attempts remaining: " + attemptsRemaining + "/3");
            if (attemptsRemaining <= 1) {
                attemptsLabel.setForeground(Color.RED);
            } else {
                attemptsLabel.setForeground(new Color(16, 185, 129));
            }
        }
    }

    private void startCooldownTimer(LocalDateTime lockedUntil) {
        submitButton.setEnabled(false);
        otpField.setEnabled(false);
        otpField.setText("");

        if (cooldownTimer != null && cooldownTimer.isRunning()) {
            cooldownTimer.stop();
        }

        cooldownTimer = new javax.swing.Timer(1000, e -> {
            java.time.Duration duration = java.time.Duration.between(LocalDateTime.now(), lockedUntil);
            if (duration.isNegative() || duration.isZero()) {
                cooldownTimer.stop();
                submitButton.setEnabled(true);
                otpField.setEnabled(true);
                attemptsLabel.setText("Attempts remaining: 3/3");
                attemptsLabel.setForeground(new Color(16, 185, 129));
                messageLabel.setText("System unlocked. You can try again.");
                messageLabel.setForeground(new Color(16, 185, 129));

                // Force database synchronization pass to clear cooldown state
                SwingWorker<Void, Void> syncWorker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        SubscriptionService.forceSyncStatus();
                        return null;
                    }
                };
                syncWorker.execute();
            } else {
                long minutes = duration.toMinutes();
                long seconds = duration.toSeconds() % 60;
                attemptsLabel.setText("Attempts remaining: Locked");
                attemptsLabel.setForeground(Color.RED);
                messageLabel.setText(String.format("Too many attempts. Try again in %02d:%02d", minutes, seconds));
                messageLabel.setForeground(Color.RED);
            }
        });
        cooldownTimer.start();
    }

    private void handleOTPSubmit() {
        String otp = otpField.getText().trim();

        if (otp.isEmpty()) {
            messageLabel.setText("Please enter the code.");
            messageLabel.setForeground(Color.RED);
            return;
        }

        if (!otp.matches("\\d{4}")) {
            messageLabel.setText("Verification code must be 4 digits.");
            messageLabel.setForeground(Color.RED);
            return;
        }

        // Loading state
        submitButton.setEnabled(false);
        submitButton.setText("Verifying...");
        messageLabel.setText("Validating code...");
        messageLabel.setForeground(new Color(59, 130, 246));

        SwingWorker<OTPService.OTPVerificationResult, Void> worker = new SwingWorker<OTPService.OTPVerificationResult, Void>() {
            @Override
            protected OTPService.OTPVerificationResult doInBackground() throws Exception {
                // Verify via service layer
                return SubscriptionService.validateOTP(subscriptionId, otp);
            }

            @Override
            protected void done() {
                try {
                    OTPService.OTPVerificationResult result = get();
                    submitButton.setText("Verify");

                    if (result.success) {
                        messageLabel.setText("✓ " + result.message);
                        messageLabel.setForeground(new Color(16, 185, 129));

                        if (callback != null) {
                            callback.onVerificationSuccess(result.newExpiryDate);
                        }

                        JOptionPane.showMessageDialog(
                                OTPEntryDialog.this,
                                "Verification Successful!\n\nThe application will now restart.",
                                "Authorized Launch",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        dispose();
                    } else {
                        messageLabel.setText(result.message);
                        messageLabel.setForeground(Color.RED);

                        if (callback != null) {
                            callback.onVerificationFailed(result.message);
                        }

                        // Recheck cooldown status immediately
                        checkAndStartCooldown();
                    }
                } catch (Exception e) {
                    messageLabel.setText("Verification failed: " + e.getMessage());
                    messageLabel.setForeground(Color.RED);
                    submitButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    public interface OTPVerificationCallback {
        void onVerificationSuccess(LocalDate newExpiryDate);
        void onVerificationFailed(String errorMessage);
    }

    public static void showOTPDialog(Long subscriptionId, OTPVerificationCallback callback) {
        SwingUtilities.invokeLater(() -> {
            OTPEntryDialog dialog = new OTPEntryDialog(subscriptionId, callback);
            dialog.setVisible(true);
        });
    }

    public static void showOTPDialog(Long subscriptionId) {
        showOTPDialog(subscriptionId, null);
    }
}
