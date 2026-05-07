package coms3620.fashion.departments.human_resources;
import coms3620.fashion.departments.human_resources.service.ManageEmployees;
import coms3620.fashion.departments.human_resources.repository.EmployeeRepo;
import coms3620.fashion.departments.human_resources.service.ManageReviews;

import java.util.Scanner;

public class HRMain {
    public static void runHR() {
        ManageEmployees sm = new ManageEmployees();
        sm.loadEmployees();
        ManageReviews sm2 = new ManageReviews();  // 👈 create once
        sm2.loadReviews();
        Scanner sc = new Scanner(System.in);

        while (true) {
    System.out.println("\n===== HR MENU =====");
    System.out.println("1. View Employees");
    System.out.println("2. Find Employee by Id");
    System.out.println("3. Add Employee");
    System.out.println("4. Fire Employee");
    System.out.println("5. Make a change to existing employee");
    System.out.println("6. Go to Reviews");
    System.out.println("7. Save & Exit");
    System.out.print("Choose option: ");
    int choice = sc.nextInt();
    sc.nextLine(); // clear newline

    switch (choice) {
        case 1 -> sm.showEmployees();
        case 2 -> {
            System.out.print("Enter Employee Id: ");
            sm.getEmployee(sc.nextInt());
        }
        case 3 -> {
            System.out.print("Enter new employee ID: ");
            int id = sc.nextInt(); sc.nextLine();
            System.out.print("Enter new employee name: ");
            String name = sc.nextLine();
            System.out.println("Enter new employee level:");
            System.out.println("1. Junior");
            System.out.println("2. Senior");
            System.out.println("3. Manager");

            int chosenLevel = sc.nextInt();
            sc.nextLine(); // clear buffer

            Employee.RoleLevel newLevel;

            switch (chosenLevel) {
                case 1: newLevel = Employee.RoleLevel.JUNIOR; break;
                case 2: newLevel = Employee.RoleLevel.SENIOR; break;
                case 3: newLevel = Employee.RoleLevel.MANAGER; break;
                default:
                    System.out.println("Invalid level.");
                    return;
            }

            System.out.print("Enter new employee location: ");
            String location = sc.nextLine();
            System.out.print("Enter new employee title: ");
            String title = sc.nextLine();
            System.out.print("Enter new employee salary: ");
            int salary = sc.nextInt();

            sm.addEmployee(id, name, newLevel, location, title, salary);
        }
        case 4 -> {
            System.out.print("Enter employee ID to fire: ");
            int id = sc.nextInt(); sc.nextLine();
            System.out.print("Enter reason for termination: ");
            String reason = sc.nextLine();
            sm.fireEmployee(id, reason);
        }
        case 5 -> {
            System.out.println("Enter employee ID to make a change to existing employee: ");
            int id = sc.nextInt(); sc.nextLine();

            if (!sm.employeeExists(id)) {
                System.out.println("Error: Employee with that ID does not exist.");
            } else {
                System.out.println("What would like to change for the employee: ");
                System.out.println("1. Role Level");
                System.out.println("2. Salary");
                System.out.println("3. Location");
                int changed = sc.nextInt();
                sc.nextLine();

                switch (changed) {
                    case 1:
                        System.out.println("Enter the employee's new role level:");
                        System.out.println("1. Junior");
                        System.out.println("2. Senior");
                        System.out.println("3. Manager");


                        int chosenLevel;

                        // Input validation loop for integers **between 1 and 3**
                        while (true) {
                            System.out.print("Choose option (1–3): ");

                            if (sc.hasNextInt()) {
                                chosenLevel = sc.nextInt();
                                sc.nextLine(); // clear buffer

                                if (chosenLevel >= 1 && chosenLevel <= 3) {
                                    break;  // valid choice → exit loop
                                } else {
                                    System.out.println("Invalid choice. Please enter 1, 2, or 3.");
                                }

                            } else {
                                System.out.println("Invalid input. Please enter a number (1–3).");
                                sc.next(); // clear invalid token
                            }
                        }

                        // Convert number → enum
                        Employee.RoleLevel newLevel = switch (chosenLevel) {
                            case 1 -> Employee.RoleLevel.JUNIOR;
                            case 2 -> Employee.RoleLevel.SENIOR;
                            case 3 -> Employee.RoleLevel.MANAGER;
                            default -> Employee.RoleLevel.JUNIOR; // never reached but required
                        };

                        sm.changeRoleLevel(id, newLevel);
                        break;


                    case 3:
                        String location;

                        while (true) {
                            System.out.print("Enter the employee's new location: ");
                            location = sc.nextLine().trim();

                            if (!location.isEmpty()) {
                                break;  // valid string
                            } else {
                                System.out.println("Location cannot be empty. Please enter a valid location.");
                            }
                        }

                        sm.changeLocation(id, location);
                        break;



                }
            }


        }
        case 6 -> {
            System.out.println("What would like to do with the Reviews?");
            System.out.println("1. Add employee Review ");
            System.out.println("2. View Reviews left for an Employee ");
            System.out.println("3. View Reviews that an employee has made ");
            System.out.println("4. Back ");
            System.out.print("Choose Option: ");
            int choice3 = sc.nextInt();
            sc.nextLine();

            switch (choice3) {
                case 1 -> {

                    int id;
                    while (true) {
                        System.out.print("Enter your employee ID: ");
                        id = sc.nextInt();
                        sc.nextLine();

                        if (sm.employeeExists(id)) {
                            break;  // valid → exit loop
                        }

                        sm.noIDMessage();  // invalid → show error and repeat
                    }


                    int id2;
                    while (true) {
                        System.out.print("Enter the employee's ID: ");
                        id2 = sc.nextInt();
                        sc.nextLine();

                        if (sm.employeeExists(id2)) {
                            break;
                        }

                        sm.noIDMessage();
                    }

                    String review;
                    while (true) {
                        System.out.print("Enter the review: ");
                        review = sc.nextLine();

                        if (!review.trim().isEmpty()) {  // valid input
                            break;
                        }

                        System.out.println("Review cannot be empty. Please enter a valid comment.");
                    }
                    sm2.addReview(id, id2, review);
                    break;



                }
                case 2 -> {
                    System.out.print("Enter employee ID: ");
                    int id = sc.nextInt();
                    sc.nextLine();
                    sm2.printReviewsByEmployeeId(id, sm);
                    break;
                }

                case 3 -> {

                    int id;
                    while (true) {
                        System.out.print("Enter your employee ID: ");
                        id = sc.nextInt();
                        sc.nextLine();

                        if (sm.employeeExists(id)) {
                            break;  // valid → exit loop
                        }

                        sm.noIDMessage();  // invalid → show error and repeat
                    }

                    // Show all reviews they left, with dates + names
                    sm2.printReviewsMadeByEmployeeID(id, sm);

                    // Ask if they want to delete one
                    System.out.print("Would you like to delete one of these reviews? (y/n): ");
                    String answer = sc.nextLine().trim().toLowerCase();

                    if (answer.equals("y")) {
                        System.out.print("Enter the review number (ID) to delete: ");
                        int reviewIdToDelete = sc.nextInt();
                        sc.nextLine();

                        boolean deleted = sm2.deleteReviewByIdForReviewer(id, reviewIdToDelete);

                        if (deleted) {
                            System.out.println("Review #" + reviewIdToDelete + " has been deleted.");
                        } else {
                            System.out.println("No review with that ID was found for your account.");
                        }
                    }

                    break;
                }



                case 4 -> {
                    break;
                }
            }


        }
        case 7 -> {
            sm.saveEmployees();
            System.out.println("Exiting HR module...");
            return;
        }
        default -> System.out.println("Invalid choice. Try again.");
    }
}

    }

    public static void main(String[] args) {
        runHR();
    }
}
