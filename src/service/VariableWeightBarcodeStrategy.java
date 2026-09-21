package service;

/**
 * Configurable Supermarket Variable-Weight / Scale Barcode Parsing Strategy.
 * Allows configurable prefix, product code bounds, weight/price bounds, and scale divisor.
 *
 * Example: "210012301500" -> Prefix "21", Product Code "00123" (ID 123), Weight "01500" / 1000 = 1.500 kg.
 *
 * @author Shop Management System
 */
public class VariableWeightBarcodeStrategy {

    private boolean enabled = true;
    private String prefix = "21"; // Configurable prefix (e.g., "21", "22", "02")
    private int productCodeStartIndex = 2;
    private int productCodeLength = 5;
    private int valueStartIndex = 7;
    private int valueLength = 5;
    private Mode mode = Mode.WEIGHT_EMBEDDED;
    private double scaleDivisor = 1000.0; // Converts e.g. 1500 grams -> 1.5 kg

    public enum Mode {
        WEIGHT_EMBEDDED,
        PRICE_EMBEDDED
    }

    public VariableWeightBarcodeStrategy() {
    }

    public VariableWeightBarcodeStrategy(String prefix, int productCodeStartIndex, int productCodeLength, int valueStartIndex, int valueLength, Mode mode, double scaleDivisor) {
        this.prefix = prefix;
        this.productCodeStartIndex = productCodeStartIndex;
        this.productCodeLength = productCodeLength;
        this.valueStartIndex = valueStartIndex;
        this.valueLength = valueLength;
        this.mode = mode;
        this.scaleDivisor = scaleDivisor;
    }

    /**
     * Parses a raw barcode string according to the configured scale barcode strategy.
     */
    public VariableWeightResult parse(String barcode) {
        if (!enabled || barcode == null || barcode.length() < (valueStartIndex + valueLength)) {
            return new VariableWeightResult(false, null, 0.0, mode);
        }

        if (!barcode.startsWith(prefix)) {
            return new VariableWeightResult(false, null, 0.0, mode);
        }

        try {
            String productCode = barcode.substring(productCodeStartIndex, productCodeStartIndex + productCodeLength);
            String rawValueStr = barcode.substring(valueStartIndex, valueStartIndex + valueLength);
            double rawValue = Double.parseDouble(rawValueStr);
            double calculatedValue = rawValue / scaleDivisor;

            return new VariableWeightResult(true, productCode, calculatedValue, mode);
        } catch (Exception e) {
            util.LoggerUtil.logWarning(VariableWeightBarcodeStrategy.class, "Failed to parse variable-weight barcode: " + barcode);
            return new VariableWeightResult(false, null, 0.0, mode);
        }
    }

    // Getters and Setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }

    public int getProductCodeStartIndex() { return productCodeStartIndex; }
    public void setProductCodeStartIndex(int productCodeStartIndex) { this.productCodeStartIndex = productCodeStartIndex; }

    public int getProductCodeLength() { return productCodeLength; }
    public void setProductCodeLength(int productCodeLength) { this.productCodeLength = productCodeLength; }

    public int getValueStartIndex() { return valueStartIndex; }
    public void setValueStartIndex(int valueStartIndex) { this.valueStartIndex = valueStartIndex; }

    public int getValueLength() { return valueLength; }
    public void setValueLength(int valueLength) { this.valueLength = valueLength; }

    public Mode getMode() { return mode; }
    public void setMode(Mode mode) { this.mode = mode; }

    public double getScaleDivisor() { return scaleDivisor; }
    public void setScaleDivisor(double scaleDivisor) { this.scaleDivisor = scaleDivisor; }

    /**
     * Parsed Scale Barcode Result DTO.
     */
    public static class VariableWeightResult {
        private final boolean isVariableWeight;
        private final String productCode;
        private final double extractedValue;
        private final Mode mode;

        public VariableWeightResult(boolean isVariableWeight, String productCode, double extractedValue, Mode mode) {
            this.isVariableWeight = isVariableWeight;
            this.productCode = productCode;
            this.extractedValue = extractedValue;
            this.mode = mode;
        }

        public boolean isVariableWeight() { return isVariableWeight; }
        public String getProductCode() { return productCode; }
        public double getExtractedValue() { return extractedValue; }
        public Mode getMode() { return mode; }
    }
}
