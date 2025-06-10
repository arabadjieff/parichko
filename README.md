# Parichko – Personal Finance Tracker
Parichko is a comprehensive Java application designed to help users track their personal income and expenses. It allows users to add, edit, delete, and undo financial transactions, view them in a sortable table, and automatically saves data to a CSV file for future use. A custom bar chart visualizes the overall balance between income and expenses with real-time updates.

## Features
- Add income and expense transactions with full validation
- Edit existing transactions by selecting table rows
- Delete transactions with confirmation
- **Undo functionality** - reverse the last action (add, delete, or update)
- View all transactions in a sortable table
- Data is automatically saved to and loaded from a local CSV file
- Comprehensive data validation (date format, positive amounts, required fields)
- Custom-drawn bar chart showing income vs. expense totals with balance display
- Category-based transaction organization
- Random financial tips displayed at the bottom
- Auto-population of input fields when selecting table rows

## Technologies Used
- Java
- Swing (for the graphical user interface)
- ArrayList and Stack (for managing transaction data and undo history)
- HashMap (for category totals tracking)
- File I/O (CSV format with robust error handling)
- Custom graphics via JPanel's `paintComponent()` method
- GridBagLayout and BorderLayout for responsive UI design

## How to Run
1. Clone the repository: https://github.com/arabadjieff/parichko
2. Open the project in your preferred Java IDE (e.g., IntelliJ, Eclipse, NetBeans).
3. Compile and run the `Parichko.java` file.
4. Use the form to add new transactions. Data will be saved in `transactions.csv` in the same directory.
5. Click on table rows to edit existing transactions, use the prominent orange "Undo" button to reverse actions.

## Project Structure
- `Parichko.java` – Main class containing all functionality in a single file
- Inner classes:
  - `Transaction` – Abstract base class representing a financial entry
  - `IncomeTransaction` – Concrete implementation for income entries
  - `ExpenseTransaction` – Concrete implementation for expense entries
  - `UndoAction` – Tracks actions for undo functionality
  - `StatsPanel` – Custom JPanel that renders a bar chart for visualizing finances

## File Format
Transactions are saved in a CSV file with the following format:
```
Date,Type,Amount,Category
2025-05-27,Income,1000.00,Salary
2025-05-27,Expense,50.00,Food
```

## User Interface
- **Left Panel**: Transaction entry form, action buttons (Add, Delete, Update, Undo), and transaction table
- **Right Panel**: Real-time income vs expense bar chart with balance display
- **Bottom Panel**: Financial summary statistics and rotating helpful tips
- **Responsive Layout**: Resizable split-pane interface with proper component sizing

## Key Improvements
- **Enhanced Undo System**: Tracks and reverses add, delete, and update operations
- **Better User Experience**: Click table rows to auto-populate edit fields
- **Improved Validation**: Comprehensive input checking with user-friendly error messages
- **Visual Enhancements**: Color-coded chart, prominent buttons, organized layout with borders
- **Robust Error Handling**: Graceful handling of file operations and user input errors

## Limitations and Notes
- CSV file is created in the same directory as the application
- Date format must be yyyy-MM-dd
- Custom chart is rendered manually using Java 2D graphics
- All data is stored locally (no cloud synchronization)

## Author
Alexander Arabadjiev – Grade 11 M2 Informatics Final Project
