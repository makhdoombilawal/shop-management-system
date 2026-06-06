package service;

import util.LoggerUtil;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for capturing and hashing device fingerprint info to prevent
 * unauthorized software copying and ensure single-device licensing.
 */
public class DeviceService {

    /**
     * Captures the full device profile (OS, Hostname, Username, MAC Address).
     */
    public static Map<String, String> captureDeviceInfo() {
        Map<String, String> deviceInfo = new HashMap<>();
        try {
            String osName = System.getProperty("os.name", "Unknown");
            String username = System.getProperty("user.name", "Unknown");
            String hostname = "Unknown";
            String macAddress = "Unknown";
            
            try {
                InetAddress localHost = InetAddress.getLocalHost();
                hostname = localHost.getHostName();
                macAddress = getMacAddress(localHost);
            } catch (Exception e) {
                LoggerUtil.logWarning(DeviceService.class, "Could not fetch network info: " + e.getMessage());
            }

            deviceInfo.put("os_name", osName);
            deviceInfo.put("username", username);
            deviceInfo.put("hostname", hostname);
            deviceInfo.put("mac_address", macAddress);
        } catch (Exception e) {
            LoggerUtil.logError(DeviceService.class, "Error capturing device info", e);
        }
        return deviceInfo;
    }

    /**
     * Generates a SHA-256 fingerprint based on the hostname, mac, os, and username.
     */
    public static String generateFingerprint() {
        Map<String, String> info = captureDeviceInfo();
        String rawString = info.get("hostname") + info.get("mac_address") + info.get("os_name") + info.get("username");
        return sha256(rawString);
    }

    private static String getMacAddress(InetAddress ip) {
        try {
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            if (network != null) {
                byte[] mac = network.getHardwareAddress();
                if (mac != null) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                    }
                    return sb.toString();
                }
            } else {
                // Fallback: search for first active non-loopback MAC
                Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
                while (networks.hasMoreElements()) {
                    NetworkInterface n = networks.nextElement();
                    if (!n.isLoopback() && n.isUp()) {
                        byte[] mac = n.getHardwareAddress();
                        if (mac != null) {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < mac.length; i++) {
                                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                            }
                            return sb.toString();
                        }
                    }
                }
            }
        } catch (Exception e) {
            LoggerUtil.logWarning(DeviceService.class, "Failed to get MAC address: " + e.getMessage());
        }
        return "Unknown";
    }

    private static String sha256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            LoggerUtil.logError(DeviceService.class, "Failed to generate SHA-256 hash", ex);
            return base.replaceAll("\\s+", ""); // Fallback
        }
    }
}
