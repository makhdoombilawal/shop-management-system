package frames;

import models.Session;
import models.entity.CategoryEntity;
import service.CategoryService;
import util.EnterpriseTheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Modern Enterprise Category Management Frame
 * Supports hierarchical categories with parent-child relationships
 */
public class CategoryManagementEnterprise extends BaseFrame {
    
    private final CategoryService categoryService = new CategoryService();
    
    // UI Components
    private JPanel headerPanel;
    private JPanel contentPanel;
    private JPanel formPanel;
    private JPanel tablePanel;
    
    private JTextField txtCategoryId;
    private JTextField txtName;
    private JComboBox<String> cmbParentCategory;
    private JTextArea txtDescription;
    private JCheckBox chkIsActive;
    
    private JTable categoryTable;
    private DefaultTableModel tableModel;
    
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;
    private JButton btnBack;
    private JButton btnRefresh;
    private JButton btnInitDefaults;
    private JButton btnDeactivate;
    
    public CategoryManagementEnterprise() throws Exception {
        super();
        if (!authorized) return;
        
        setTitle("Category Management - Shop Manager");
        setSize(1500, 850);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        initializeComponents();
        setupLayout();
        loadCategories();
        loadParentCategories();
        
        EnterpriseTheme.applyGlobalTheme();
    }
    
    private void initializeComponents() {
        // Header
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(EnterpriseTheme.PRIMARY);
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        JLabel lblTitle = new JLabel("CATEGORY MANAGEMENT");
        lblTitle.setFont(EnterpriseTheme.FONT_TITLE);
        lblTitle.setForeground(EnterpriseTheme.TEXT_ON_PRIMARY);
        
        JLabel lblUser = new JLabel("User: " + Session.getUsername() + " | Role: " + Session.getRole());
        lblUser.setFont(EnterpriseTheme.FONT_BODY);
        lblUser.setForeground(EnterpriseTheme.TEXT_ON_PRIMARY);
        
        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(lblUser, BorderLayout.EAST);
        
        // Content Panel
        contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(EnterpriseTheme.BACKGROUND);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        createFormPanel();
        createTablePanel();
    }
    
    private void createFormPanel() {
        formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(EnterpriseTheme.CARD_BG);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(EnterpriseTheme.BORDER, 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        
        // Category ID (hidden from users, auto-generated)
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lblCategoryId = new JLabel("Category ID:");
        lblCategoryId.setFont(EnterpriseTheme.FONT_BODY);
        lblCategoryId.setForeground(EnterpriseTheme.TEXT_SECONDARY);
        formPanel.add(lblCategoryId, gbc);
        
        gbc.gridx = 1;
        txtCategoryId = new JTextField(20);
        txtCategoryId.setFont(EnterpriseTheme.FONT_INPUT);
        txtCategoryId.setEnabled(false);
        formPanel.add(txtCategoryId, gbc);
        row++;
        
        // Category Name
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lblName = new JLabel("Category Name: *");
        lblName.setFont(EnterpriseTheme.FONT_BODY);
        lblName.setForeground(EnterpriseTheme.TEXT_PRIMARY);
        formPanel.add(lblName, gbc);
        
        gbc.gridx = 1;
        txtName = new JTextField(20);
        txtName.setFont(EnterpriseTheme.FONT_INPUT);
        formPanel.add(txtName, gbc);
        row++;
        
        // Parent Category (for hierarchical structure)
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lblParent = new JLabel("Parent Category:");
        lblParent.setFont(EnterpriseTheme.FONT_BODY);
        lblParent.setForeground(EnterpriseTheme.TEXT_PRIMARY);
        formPanel.add(lblParent, gbc);
        
        gbc.gridx = 1;
        cmbParentCategory = new JComboBox<>();
        cmbParentCategory.setFont(EnterpriseTheme.FONT_INPUT);
        cmbParentCategory.addItem("-- None (Top Level) --");
        formPanel.add(cmbParentCategory, gbc);
        row++;
        
        // Description
        gbc.gridx = 0; gbc.gridy = row;
        gbc.anchor = GridBagConstraints.NORTH;
        JLabel lblDescription = new JLabel("Description:");
        lblDescription.setFont(EnterpriseTheme.FONT_BODY);
        lblDescription.setForeground(EnterpriseTheme.TEXT_PRIMARY);
        formPanel.add(lblDescription, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        txtDescription = new JTextArea(3, 20);
        txtDescription.setFont(EnterpriseTheme.FONT_INPUT);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        JScrollPane scrollDescription = new JScrollPane(txtDescription);
        formPanel.add(scrollDescription, gbc);
        row++;
        
        // Is Active Checkbox
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        chkIsActive = new JCheckBox("Active", true);
        chkIsActive.setFont(EnterpriseTheme.FONT_BODY);
        chkIsActive.setBackground(EnterpriseTheme.CARD_BG);
        chkIsActive.setForeground(EnterpriseTheme.TEXT_PRIMARY);
        formPanel.add(chkIsActive, gbc);
        row++;
        
        // Buttons Panel
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 8, 8, 8);
        
        JPanel buttonPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        buttonPanel.setBackground(EnterpriseTheme.CARD_BG);
        
        btnAdd = new JButton("Add");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Delete");
        btnDeactivate = new JButton("Deactivate");
        btnClear = new JButton("Clear");
        btnInitDefaults = new JButton("Init Defaults");
        
        EnterpriseTheme.styleSuccessButton(btnAdd);
        EnterpriseTheme.stylePrimaryButton(btnUpdate);
        EnterpriseTheme.styleDangerButton(btnDelete);
        EnterpriseTheme.styleWarningButton(btnDeactivate);
        EnterpriseTheme.styleWarningButton(btnClear);
        EnterpriseTheme.stylePrimaryButton(btnInitDefaults);
        
        EnterpriseTheme.setStandardButtonSize(btnAdd);
        EnterpriseTheme.setStandardButtonSize(btnUpdate);
        EnterpriseTheme.setStandardButtonSize(btnDelete);
        EnterpriseTheme.setStandardButtonSize(btnDeactivate);
        EnterpriseTheme.setStandardButtonSize(btnClear);
        EnterpriseTheme.setStandardButtonSize(btnInitDefaults);
        
        btnAdd.addActionListener(e -> addCategory());
        btnUpdate.addActionListener(e -> updateCategory());
        btnDelete.addActionListener(e -> deleteCategory());
        btnDeactivate.addActionListener(e -> deactivateCategory());
        btnClear.addActionListener(e -> clearForm());
        btnInitDefaults.addActionListener(e -> initializeDefaults());
        
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnDeactivate);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnInitDefaults);
        
        formPanel.add(buttonPanel, gbc);
    }
    
    private void createTablePanel() {
        tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(EnterpriseTheme.CARD_BG);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(EnterpriseTheme.BORDER, 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Table Header
        JPanel tableHeaderPanel = new JPanel(new BorderLayout());
        tableHeaderPanel.setBackground(EnterpriseTheme.CARD_BG);
        tableHeaderPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JLabel lblTableTitle = new JLabel("Existing Categories");
        lblTableTitle.setFont(EnterpriseTheme.FONT_SUBHEADER);
        lblTableTitle.setForeground(EnterpriseTheme.TEXT_PRIMARY);
        
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setBackground(EnterpriseTheme.CARD_BG);
        
        btnRefresh = new JButton("Refresh");
        btnBack = new JButton("Back");
        
        EnterpriseTheme.stylePrimaryButton(btnRefresh);
        EnterpriseTheme.styleSecondaryButton(btnBack);
        
        EnterpriseTheme.setStandardButtonSize(btnRefresh);
        EnterpriseTheme.setStandardButtonSize(btnBack);
        
        btnRefresh.addActionListener(e -> refreshTable());
        btnBack.addActionListener(e -> {
            dispose();
            try {
                new DashboardEnterprise().setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error opening dashboard: " + ex.getMessage());
            }
        });
        
        actionPanel.add(btnRefresh);
        actionPanel.add(btnBack);
        
        tableHeaderPanel.add(lblTableTitle, BorderLayout.WEST);
        tableHeaderPanel.add(actionPanel, BorderLayout.EAST);
        
        // Table
        String[] columns = {"ID", "Name", "Parent", "Description", "Active", "# Products"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        categoryTable = new JTable(tableModel);
        categoryTable.setFont(EnterpriseTheme.FONT_TABLE_CELL);
        categoryTable.setForeground(EnterpriseTheme.TEXT_PRIMARY);
        categoryTable.setBackground(Color.WHITE);
        categoryTable.setSelectionBackground(EnterpriseTheme.PRIMARY.brighter());
        categoryTable.setSelectionForeground(Color.WHITE);
        categoryTable.setGridColor(EnterpriseTheme.BORDER);
        categoryTable.setShowGrid(true);
        categoryTable.getTableHeader().setFont(EnterpriseTheme.FONT_TABLE_HEADER);
        categoryTable.getTableHeader().setBackground(EnterpriseTheme.PRIMARY);
        categoryTable.getTableHeader().setForeground(EnterpriseTheme.TEXT_ON_PRIMARY);
        
        EnterpriseTheme.setStandardTableRowHeight(categoryTable);
        
        categoryTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 1) {
                    int row = categoryTable.getSelectedRow();
                    if (row >= 0) {
                        loadCategoryToForm(row);
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(categoryTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(EnterpriseTheme.BORDER));
        
        tablePanel.add(tableHeaderPanel, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.NORTH);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.35;
        gbc.weighty = 1.0;
        contentPanel.add(formPanel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.65;
        contentPanel.add(tablePanel, gbc);
        
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private void loadCategories() {
        try {
            List<CategoryEntity> categories = categoryService.getAllCategories();
            tableModel.setRowCount(0);
            
            for (CategoryEntity category : categories) {
                String parentName = category.getParentCategory() != null 
                    ? category.getParentCategory().getName() 
                    : "-- Top Level --";
                
                Long productCount = categoryService.countProductsInCategory(category.getCategoryId());
                
                Object[] row = {
                    category.getCategoryId(),
                    category.getName(),
                    parentName,
                    category.getDescription(),
                    category.getIsActive() ? "Yes" : "No",
                    productCount
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading categories: " + e.getMessage(), 
                "Load Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadParentCategories() {
        try {
            cmbParentCategory.removeAllItems();
            cmbParentCategory.addItem("-- None (Top Level) --");
            
            List<String> categoryNames = categoryService.getAllCategoryNames();
            for (String name : categoryNames) {
                cmbParentCategory.addItem(name);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading parent categories: " + e.getMessage(), 
                "Load Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadCategoryToForm(int row) {
        txtCategoryId.setText(tableModel.getValueAt(row, 0).toString());
        txtName.setText(tableModel.getValueAt(row, 1).toString());
        
        String parentName = tableModel.getValueAt(row, 2).toString();
        cmbParentCategory.setSelectedItem(parentName);
        
        Object description = tableModel.getValueAt(row, 3);
        txtDescription.setText(description != null ? description.toString() : "");
        
        String activeStatus = tableModel.getValueAt(row, 4).toString();
        chkIsActive.setSelected("Yes".equals(activeStatus));
    }
    
    private void addCategory() {
        try {
            if (!validateCategoryInput()) {
                return;
            }
            
            CategoryEntity category = new CategoryEntity();
            category.setName(txtName.getText().trim());
            category.setDescription(txtDescription.getText().trim());
            category.setIsActive(chkIsActive.isSelected());
            
            // Set parent category if selected
            String selectedParent = (String) cmbParentCategory.getSelectedItem();
            if (selectedParent != null && !selectedParent.contains("None")) {
                categoryService.getCategoryByName(selectedParent).ifPresent(category::setParentCategory);
            }
            
            categoryService.createCategory(category);
            
            JOptionPane.showMessageDialog(this, 
                "Category added successfully!", 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
            
            clearForm();
            refreshTable();
            loadParentCategories(); // Reload parent dropdown
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, 
                e.getMessage(), 
                "Validation Error", 
                JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error adding category: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateCategory() {
        try {
            if (txtCategoryId.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please select a category to update", 
                    "Validation Error", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (!validateCategoryInput()) {
                return;
            }
            
            CategoryEntity category = new CategoryEntity();
            category.setCategoryId(Integer.parseInt(txtCategoryId.getText().trim()));
            category.setName(txtName.getText().trim());
            category.setDescription(txtDescription.getText().trim());
            category.setIsActive(chkIsActive.isSelected());
            
            // Set parent category if selected
            String selectedParent = (String) cmbParentCategory.getSelectedItem();
            if (selectedParent != null && !selectedParent.contains("None")) {
                categoryService.getCategoryByName(selectedParent).ifPresent(category::setParentCategory);
            }
            
            categoryService.updateCategory(category);
            
            JOptionPane.showMessageDialog(this, 
                "Category updated successfully!", 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
            
            clearForm();
            refreshTable();
            loadParentCategories(); // Reload parent dropdown
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, 
                e.getMessage(), 
                "Validation Error", 
                JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error updating category: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteCategory() {
        try {
            if (txtCategoryId.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please select a category to delete", 
                    "Validation Error", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to permanently delete this category?\n" +
                "Note: Categories with subcategories or products cannot be deleted.", 
                "Confirm Delete", 
                JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                Integer categoryId = Integer.parseInt(txtCategoryId.getText().trim());
                categoryService.deleteCategory(categoryId);
                
                JOptionPane.showMessageDialog(this, 
                    "Category deleted successfully!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                clearForm();
                refreshTable();
                loadParentCategories(); // Reload parent dropdown
            }
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, 
                e.getMessage(), 
                "Deletion Error", 
                JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error deleting category: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deactivateCategory() {
        try {
            if (txtCategoryId.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please select a category to deactivate", 
                    "Validation Error", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Deactivate this category?\nIt will be hidden but not deleted.", 
                "Confirm Deactivation", 
                JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                Integer categoryId = Integer.parseInt(txtCategoryId.getText().trim());
                categoryService.deactivateCategory(categoryId);
                
                JOptionPane.showMessageDialog(this, 
                    "Category deactivated successfully!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                clearForm();
                refreshTable();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error deactivating category: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void clearForm() {
        txtCategoryId.setText("");
        txtName.setText("");
        cmbParentCategory.setSelectedIndex(0);
        txtDescription.setText("");
        chkIsActive.setSelected(true);
    }
    
    private void refreshTable() {
        loadCategories();
        JOptionPane.showMessageDialog(this, 
            "Table refreshed!", 
            "Success", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void initializeDefaults() {
        try {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Initialize 11 default categories?\n" +
                "(Electronics, Food & Beverages, Clothing, etc.)\n" +
                "Existing categories will not be affected.", 
                "Confirm Initialization", 
                JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                categoryService.initializeDefaultCategories();
                
                JOptionPane.showMessageDialog(this, 
                    "Default categories initialized successfully!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                refreshTable();
                loadParentCategories(); // Reload parent dropdown
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error initializing defaults: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private boolean validateCategoryInput() {
        // Check name
        if (txtName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Category name is required", 
                "Validation Error", 
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        if (txtName.getText().trim().length() < 2) {
            JOptionPane.showMessageDialog(this, 
                "Category name must be at least 2 characters", 
                "Validation Error", 
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        if (txtName.getText().trim().length() > 100) {
            JOptionPane.showMessageDialog(this, 
                "Category name cannot exceed 100 characters", 
                "Validation Error", 
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        // Check description length
        if (txtDescription.getText().trim().length() > 255) {
            JOptionPane.showMessageDialog(this, 
                "Description cannot exceed 255 characters", 
                "Validation Error", 
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        return true;
    }
}
