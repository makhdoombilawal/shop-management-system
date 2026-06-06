# Deployment Package Report

This report reviews the deployment structure and configuration profile setup for the **Shop Management System v2.0**.

## 1. Distribution Package Structure

The deployment target files are packaged under the `dist/` directory, ready to be distributed to target client machines:
```text
dist/
├── lib/                             -- Directory (junction) containing 48 dependency libraries (JAR files)
└── shop-management.jar              -- Main executable application archive (0.39 MB)
```

### Components Details:
- **`shop-management.jar`**: Compiled standalone Java archive containing all controllers, models, views, and core assets (such as default branding graphics and `hibernate.cfg.xml` configuration).
- **`lib/`**: Contains required runtime dependencies (Hibernate, FlatLaf, Webcam capture API, barcode4j, etc.).

---

## 2. Windows Installer Configuration (Inno Setup)

For production systems, the application installer is compiled using **Inno Setup** via the script `installer_setup_enterprise.iss` at the root level.

### Installer Actions:
1. Installs the application files under `C:\Program Files\ShopManager\`.
2. Packages the built JAR, the dependencies `lib/` directory, and runtime icons.
3. Automatically sets up shortcuts on the Desktop and Start Menu.
4. Executes database initialization tasks automatically via `initialize_database.bat` on installation completion.
