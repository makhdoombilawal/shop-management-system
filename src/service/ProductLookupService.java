package service;

import dao.BarcodeHibernateDAO;
import dao.ProductHibernateDAO;
import models.entity.BarcodeEntity;
import models.entity.ProductEntity;
import util.BarcodeValidationUtil;

import java.util.Optional;

/**
 * Centralized Supermarket Product Lookup Engine.
 * Resolves Primary Barcodes, Alternate Barcodes, SKUs, Product IDs, and Scale Barcodes.
 *
 * @author Shop Management System
 */
public class ProductLookupService {

    private final BarcodeHibernateDAO barcodeDAO;
    private final ProductHibernateDAO productDAO;
    private VariableWeightBarcodeStrategy variableWeightStrategy;

    public ProductLookupService() {
        this.barcodeDAO = new BarcodeHibernateDAO();
        this.productDAO = new ProductHibernateDAO();
        this.variableWeightStrategy = new VariableWeightBarcodeStrategy();
    }

    public ProductLookupService(BarcodeHibernateDAO barcodeDAO, ProductHibernateDAO productDAO) {
        this.barcodeDAO = barcodeDAO;
        this.productDAO = productDAO;
        this.variableWeightStrategy = new VariableWeightBarcodeStrategy();
    }

    /**
     * Set a custom scale barcode strategy if required by the store.
     */
    public void setVariableWeightStrategy(VariableWeightBarcodeStrategy strategy) {
        if (strategy != null) {
            this.variableWeightStrategy = strategy;
        }
    }

    public VariableWeightBarcodeStrategy getVariableWeightStrategy() {
        return variableWeightStrategy;
    }

    /**
     * Central resolution method for barcode scanners and search text fields.
     */
    public ProductLookupResult resolveProduct(String rawIdentifier) {
        String normalized = BarcodeValidationUtil.normalizeBarcode(rawIdentifier);

        if (normalized.isEmpty()) {
            return new ProductLookupResult(false, "Input barcode or search term cannot be empty.", null, null, 1.0, null, LookupType.NONE);
        }

        // Step 1: Check Variable-Weight / Scale Barcode Parsing
        if (variableWeightStrategy != null && variableWeightStrategy.isEnabled()) {
            VariableWeightBarcodeStrategy.VariableWeightResult scaleResult = variableWeightStrategy.parse(normalized);
            if (scaleResult.isVariableWeight()) {
                String productCode = scaleResult.getProductCode();
                ProductEntity product = null;

                try {
                    int pId = Integer.parseInt(productCode);
                    Optional<ProductEntity> pOpt = productDAO.findById(pId);
                    if (pOpt.isPresent()) product = pOpt.get();
                } catch (NumberFormatException ignored) {}

                if (product == null) {
                    product = productDAO.findBySku(productCode);
                }

                if (product != null) {
                    double quantity = 1.0;
                    Double priceOverride = null;

                    if (scaleResult.getMode() == VariableWeightBarcodeStrategy.Mode.WEIGHT_EMBEDDED) {
                        quantity = scaleResult.getExtractedValue();
                    } else if (scaleResult.getMode() == VariableWeightBarcodeStrategy.Mode.PRICE_EMBEDDED) {
                        priceOverride = scaleResult.getExtractedValue();
                    }

                    return new ProductLookupResult(true, "Scale Barcode resolved successfully.", product, normalized, quantity, priceOverride, LookupType.VARIABLE_WEIGHT);
                }
            }
        }

        // Step 2: Check Active Barcode Records (Primary or Alternate)
        BarcodeEntity barcodeEntity = barcodeDAO.findByBarcodeNumber(normalized);
        if (barcodeEntity != null && barcodeEntity.getProduct() != null) {
            ProductEntity product = barcodeEntity.getProduct();

            if (!barcodeEntity.isActivated()) {
                return new ProductLookupResult(false, "⚠️ Scanned barcode [" + normalized + "] is INACTIVE.", null, normalized, 1.0, null, LookupType.BARCODE);
            }

            if ("damaged".equalsIgnoreCase(barcodeEntity.getStatus())) {
                return new ProductLookupResult(false, "⚠️ Scanned barcode [" + normalized + "] is marked as DAMAGED.", null, normalized, 1.0, null, LookupType.BARCODE);
            }

            LookupType type = (barcodeEntity.getIsPrimary() != null && barcodeEntity.getIsPrimary())
                    ? LookupType.PRIMARY_BARCODE
                    : LookupType.ALTERNATE_BARCODE;

            return new ProductLookupResult(true, "Product resolved via barcode.", product, normalized, 1.0, null, type);
        }

        // Step 3: Check Product SKU
        ProductEntity productBySku = productDAO.findBySku(normalized);
        if (productBySku != null) {
            return new ProductLookupResult(true, "Product resolved via SKU.", productBySku, normalized, 1.0, null, LookupType.SKU);
        }

        // Step 4: Check Product ID (if input is numeric)
        if (normalized.matches("^[0-9]{1,8}$")) {
            try {
                int productId = Integer.parseInt(normalized);
                Optional<ProductEntity> productById = productDAO.findById(productId);
                if (productById.isPresent()) {
                    return new ProductLookupResult(true, "Product resolved via Product ID.", productById.get(), normalized, 1.0, null, LookupType.PRODUCT_ID);
                }
            } catch (NumberFormatException ignored) {}
        }

        // Not Found
        return new ProductLookupResult(false, "Barcode not registered: " + normalized, null, normalized, 1.0, null, LookupType.NONE);
    }

    /**
     * Enumeration of lookup resolution sources.
     */
    public enum LookupType {
        PRIMARY_BARCODE,
        ALTERNATE_BARCODE,
        BARCODE,
        SKU,
        PRODUCT_ID,
        VARIABLE_WEIGHT,
        NONE
    }

    /**
     * Product Resolution Result DTO.
     */
    public static class ProductLookupResult {
        private final boolean success;
        private final String message;
        private final ProductEntity product;
        private final String scannedBarcode;
        private final double quantity;
        private final Double priceOverride;
        private final LookupType lookupType;

        public ProductLookupResult(boolean success, String message, ProductEntity product, String scannedBarcode, double quantity, Double priceOverride, LookupType lookupType) {
            this.success = success;
            this.message = message;
            this.product = product;
            this.scannedBarcode = scannedBarcode;
            this.quantity = quantity;
            this.priceOverride = priceOverride;
            this.lookupType = lookupType;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public ProductEntity getProduct() { return product; }
        public String getScannedBarcode() { return scannedBarcode; }
        public double getQuantity() { return quantity; }
        public Double getPriceOverride() { return priceOverride; }
        public LookupType getLookupType() { return lookupType; }
    }
}
