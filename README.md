# JavaExpenseTracker
# Expense Tracker

A Java Swing application for tracking and visualizing personal expenses.

![Expense Tracker Screenshot]
![image](https://github.com/user-attachments/assets/16e0b34f-d57e-45d4-8bd1-b55b1824b9ab)
![image](https://github.com/user-attachments/assets/f9a055ca-8c05-4dcb-a2e6-40d9a468322a)
![image](https://github.com/user-attachments/assets/81cfceb7-b760-4179-bf09-bc2a5db76629)



## Features

- **Add Expenses**: Enter expense details including amount, description, category, and date
- **Multiple Views**:
  - **Summary View**: Get a clear overview of your spending patterns with percentages
  - **Detailed View**: See all your expenses in a tabular format
  - **Chart View**: Visualize expense distribution with an interactive pie chart
- **Expense Management**:
  - Filter expenses by category
  - Delete unwanted expenses
  - Automatic sorting by date (newest first)
- **Data Persistence**: All expense data is automatically saved to a file
- **Export Functionality**: Export summaries to text files for record-keeping

## Requirements

- Java Runtime Environment (JRE) 8 or higher
- Swing (included in standard Java installation)

## Installation

1. Download the Expense Tracker JAR file from the releases section
2. Ensure you have Java installed on your system
3. Run the application using:
   ```
   java -jar ExpenseTracker.jar
   ```

## Usage Guide

### Adding an Expense

1. Enter the expense amount in the "Amount" field
2. Choose or type a category in the "Category" dropdown
3. Provide a description (optional)
4. Select the date of the expense using the date chooser
5. Click "Add Expense" to record it
6. Use "Clear Fields" to reset the form

### Viewing Expenses

Navigate between different views using the tabs:

- **Summary**: Shows total expenses by category with percentages
- **Detailed View**: Displays all expenses in a sortable table
- **Charts**: Visualizes expense distribution in a pie chart

### Managing Expenses

- **Delete**: Select an expense in the detailed view and click "Delete Selected"
- **Filter**: Click "Filter By Category" to view expenses from a specific category
- **Export**: Click "Export Summary" to save your expense summary as a text file

## Data Storage

The application automatically saves your expenses to a file named `expenses.txt` in the application directory. This file is loaded each time you start the application and updated when you close it.

## Building from Source

To build the application from source:

1. Clone the repository:
   ```
   git clone https://github.com/username/expense-tracker.git
   ```
2. Open the project in your Java IDE (Eclipse, IntelliJ IDEA, etc.)
3. Build the project using your IDE's build command or with:
   ```
   javac ExpenseTracker.java
   ```
4. Run the compiled application:
   ```
   java ExpenseTracker
   ```

## Customization

You can customize the application by:

- Modifying the categories in the `categories` array
- Changing the currency symbol (currently ₹) in the formatter strings
- Adjusting the UI colors in the chart generation code

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Built with Java Swing for the user interface
- Uses simple file I/O for data persistence
- Custom chart rendering for expense visualization
