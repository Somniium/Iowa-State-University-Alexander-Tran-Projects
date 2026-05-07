# Fashion Manager Terminal System

A Java terminal-based management system created as a COM S 3620 final project at Iowa State University.  
The project simulates the internal operations of a fashion company by separating the application into multiple business departments, each with its own menu, data models, and CSV-backed storage.

## Project Overview

Fashion Manager is a console application that starts with a splash screen and then opens a menu-driven system. From the main menu, users can enter different departments of the company and perform actions related to marketing, logistics, human resources, product development, legal review, and finance.

The main idea of the project is to model how different departments inside a company interact with shared business data. Each department has its own responsibilities, but they all fit into one larger terminal application.

## Main Departments

The application is organized around six major departments:

- Marketing and Sales
- Logistics
- Human Resources
- Product Development
- Legal
- Finance and Accounting

Each department is reachable through the main `FashionManager` menu.

## Features

### Main Menu System

The project uses a reusable menu structure built around the `Menu` abstract class and the `Option` interface.

Users can:

- View numbered menu options
- Select a department
- Enter submenus
- Return to previous menus
- Exit cleanly

This keeps the application organized and makes it easier to add new features as new menu options.

### Marketing and Sales

The Marketing and Sales module manages advertisements and advertising relationships.

Features include:

- Create advertisements
- View advertisements
- Delete advertisements
- Publish advertisements
- View published advertisements
- Cancel published advertisements
- Create advertising relationships
- View advertising relationships
- Send advertisements and relationships through approval workflows

The advertising system supports multiple advertisement types, including:

- TV advertisements
- Radio advertisements
- Magazine advertisements

### Legal

The Legal module handles approval workflows for marketing items that require legal review.

Features include:

- Load pending published advertisements
- Load pending advertising relationships
- Approve or deny advertisements
- Approve or deny advertising relationships
- Store contract file information when approval requires a contract

This module connects closely with Marketing and Sales because published advertisements and advertising relationships can be routed to Legal before becoming fully approved.

### Logistics

The Logistics module manages inventory, orders, and shipments.

Features include:

- Load products from CSV storage
- View available products
- Search products by name
- Create orders
- Add products to orders
- Edit order quantities
- Delete orders
- Create shipments from active orders
- View shipments
- Edit shipments
- Cancel shipments
- Restock inventory

Products track information such as:

- Product name
- SKU
- Size
- Price
- Quantity

Orders and shipments also use status tracking so their progress can be updated over time.

### Human Resources

The Human Resources module manages employee records and employee reviews.

Features include:

- View employees
- Search for an employee by ID
- Add employees
- Fire employees
- Change employee role level
- Change employee salary
- Change employee location
- Add employee reviews
- View reviews left for an employee
- View reviews written by an employee
- Delete reviews written by a specific employee

Employees include information such as:

- Employee ID
- Name
- Role level
- Location
- Job title
- Salary

The review system stores reviewer ID, reviewee ID, comment text, and date.

### Product Development

The Product Development module manages prototype ideas for fashion products.

Features include:

- Create prototypes
- View prototypes
- Approve or reject prototypes
- Delete prototypes
- Filter prototypes by material
- Update prototype materials
- Estimate prototype cost
- Run a simple design contest
- Track contest votes

Prototype data includes:

- UUID
- Concept name
- Materials
- Approval status
- Last actor
- Last note

The cost estimator reads material prices from a CSV file and estimates prototype cost based on the listed materials.

### Finance and Accounting

The Finance and Accounting module manages department budgets and expenses.

Features include:

- View budgets
- Create or update department budgets
- Record expenses
- Transfer funds between departments
- Track remaining budget
- Detect over-budget departments
- Approve budget overruns

The finance system stores budget and expense data in CSV files and includes rollback-style logic if saving fails.

## Technologies Used

- Java
- Object-oriented programming
- Interfaces
- Abstract classes
- Enums
- Java collections
- File I/O
- CSV storage
- Console menus
- Basic input validation

## Project Structure

```text
FinalProject-main/
├── data/
│   ├── finance_and_accounting/
│   ├── human_resources/
│   ├── logistics/
│   ├── marketing_and_sales/
│   └── product_development/
│
└── src/
    └── coms3620/
        └── fashion/
            ├── Main.java
            ├── FashionManager.java
            ├── departments/
            ├── menus/
            └── util/
```

## Important Classes

### `Main`

Starts the application, displays the splash screen, creates a `FashionManager`, and enters the main menu.

### `FashionManager`

The top-level application menu. It connects all major departments into one terminal interface.

### `Menu`

An abstract reusable menu class. It stores a list of options, prints them, validates user selection, and runs the selected option.

### `Option`

An interface used by menu items. Each option provides a display name and a `run()` method.

### `DataReader` and `DataWriter`

Utility classes used for reading and writing CSV-style files. These help the different departments persist data between program runs.

### `InputValidation`

A utility class for validating integer input and menu option selection.

## Data Storage

The application uses CSV files stored in the `data/` folder. Each department has its own data directory.

Examples:

```text
data/finance_and_accounting/budgets.csv
data/finance_and_accounting/expenses.csv
data/human_resources/employees.csv
data/human_resources/review.csv
data/logistics/products.csv
data/marketing_and_sales/adverts.csv
data/marketing_and_sales/publishedAdverts.csv
data/marketing_and_sales/advertisingRelationships.csv
data/product_development/prototypes.csv
data/product_development/material_costs.csv
data/product_development/contest_votes.csv
```

Because the program uses relative file paths, it should be run from the project root so the `data/` folder can be found correctly.

## How to Run

This project does not use Maven or Gradle. It can be compiled and run directly with `javac` and `java`.

From the project root:

```bash
mkdir -p out
find src -name "*.java" > java_sources.txt
javac -d out @java_sources.txt
java -cp out coms3620.fashion.Main
```

On Windows, the easiest option is usually to open the project in IntelliJ IDEA or Eclipse, mark `src` as the source folder, and run:

```text
coms3620.fashion.Main
```

## Example User Flow

1. Start the program.
2. Press Enter on the splash screen.
3. Choose a department from the main menu.
4. Select a submenu option.
5. Create, view, update, or delete records.
6. Return to the main menu or exit.

Example main menu departments:

```text
1. Marketing and Sales
2. Logistics
3. Human Resources
4. Product Development
5. Legal
6. Finance and Accounting
```

## What I Learned

This project helped me practice building a larger Java application made of many smaller parts. Instead of writing one class for one assignment, this project required multiple packages, department-specific classes, shared utility classes, and a reusable menu system.

I also gained more experience with object-oriented design. The project uses interfaces, abstract classes, repositories, services, managers, and models to separate responsibilities across the application.

Another important part of the project was working with persistent data. Each department saves and loads information from CSV files, which made it important to think about file paths, data formatting, and keeping in-memory data consistent with saved data.

## Challenges

One challenge was keeping the menu system organized as more departments and submenu options were added. The `Menu` and `Option` structure helped solve this by making each menu item responsible for its own behavior.

Another challenge was managing data across multiple departments. Marketing and Legal share advertising approval data, Finance tracks budgets and expenses, Logistics modifies product inventory, and Product Development stores prototypes and material costs.

File storage was also a challenge because the program depends on correctly formatted CSV files and relative paths from the project root.

## Skills Demonstrated

- Java application design
- Package organization
- Console UI design
- Menu-driven workflows
- Object-oriented programming
- Interface-based design
- Repository and manager classes
- CSV file persistence
- Input validation
- Cross-module coordination
- Debugging larger Java projects

## Notes

- This is a console-based Java application.
- The project uses only standard Java libraries.
- The program should be run from the project root so the `data/` directory is accessible.
- The included CSV files act as the application's local storage.
- I verified that the project compiles successfully with `javac`.

## Author

Alexander Tran  
COM S 3620 Final Project  
Iowa State University
