# Gitignore Validation Report

This report documents the design and validation of the `.gitignore` file created for the **Shop Management System v2.0** repository.

## 1. Design & Exclusions
The `.gitignore` has been designed to keep the repository lean and secure while preserving all files necessary to compile and run the application.

### Key Rules Checked:
1. **Source Code**: No Java source files (`src/**/*.java`), form layouts (`src/**/*.form`), or resource assets (`src/**/*.xml`, `resources/**/*`) are ignored.
2. **Local Library Dependencies**: All JAR dependency packages stored under `lib/` are explicitly whitelisted (using `!/lib/*.jar` and `!/lib/**/*.jar`) so the project compiles directly after cloning.
3. **Sensitive Configurations**: `SMTPConfig.properties` (containing mail server authentication credentials) is excluded from version control to prevent credential leakage.
4. **Temporary / Build Output**: Directories such as `build/`, `dist/`, `target/`, and compiled class files (`*.class`) are ignored to prevent build pollution.
5. **Runtime Artifacts**: bar code images generated dynamically during runtime are ignored under `/barcodes/*.png`.
