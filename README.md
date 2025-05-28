# Parichko – Personal Finance Tracker

Parichko is a simple Java application designed to help users track their personal income and expenses. It allows users to add financial entries, view them in a table, and automatically saves data to a CSV file for future use. A bar chart visualizes the overall balance between income and expenses.

## Features

- Add income and expense transactions
- View all transactions in a table
- Data is saved to and loaded from a local CSV file
- Basic data validation
- Custom-drawn bar chart showing income vs. expense totals

## Technologies Used

- Java (JDK 8 or later)
- Swing (for the graphical user interface)
- ArrayList (for managing transaction data)
- File I/O (CSV format)
- Custom graphics via JPanel's `paintComponent()` method

## How to Run

1. Clone the repository: https://github.com/arabadjieff/parichko
2. Open the project in your preferred Java IDE (e.g., IntelliJ, Eclipse, NetBeans).

3. Compile and run the `Parichko.java` file.

4. Use the form to add new transactions. Data will be saved in `transactions.csv` in the same directory.

## Project Structure

- `Parichko.java` – Main class, GUI logic, file reading/writing, and transaction list management
- Inner classes:
- `Transaction` – Represents a financial entry
- `StatsPanel` – Custom JPanel that renders a bar chart for visualizing finances

## File Format

Transactions are saved in a CSV file with the following format:

Date,Type,Amount,Category
2025-05-27,Income,1000,Salary
2025-05-27,Expense,50,Food

markdown
Copy
Edit

## Limitations and Notes

- Currently supports only adding transactions (editing and deleting can be added in future versions).
- CSV file must be in the same directory as the application.
- Custom chart is rendered manually using Java 2D graphics.

## Author

Alexander Arabadjiev – Grade 11 M2 Informatics Final Project
