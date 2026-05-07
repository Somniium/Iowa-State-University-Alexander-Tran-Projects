package coms3620.fashion.departments.human_resources.service;
import coms3620.fashion.departments.human_resources.Employee;
import coms3620.fashion.departments.human_resources.repository.EmployeeRepo;

import java.io.*;
import java.util.*;

public class ManageEmployees {
    private List<Employee> employees = new ArrayList<>();
    private EmployeeRepo employeeRepo = new EmployeeRepo();
    private static final String FILE_NAME = "data/human_resources/employees.csv";


    public void saveEmployees() {
        employeeRepo.saveEmployees();
    }

    public void loadEmployees() {
        employeeRepo.loadEmployees();
    }

    public void showEmployees() {
        employeeRepo.loadEmployees(); // loads into repo’s internal list

        List<Employee> list = employeeRepo.getEmployees(); // get list from repo

        if (list.isEmpty()) {
            System.out.println("No employees found.");
            return;
        }

        System.out.println("\n--- Employee List ---");
        for (Employee e : list) {
            System.out.println(e.getId() + " - " + e.getName());
        }
    }

    public void fireEmployee(int id, String reason) {
        boolean removed = false;

        Iterator<Employee> iterator = employees.iterator();
        while (iterator.hasNext()) {
            Employee e = iterator.next();
            if (e.getId() == id) {
                System.out.println("Firing employee: " + e.getName() + " (Reason: " + reason + ")");
                iterator.remove(); // removes from the list
                removed = true;
                break;
            }
        }

        if (removed) {
            saveEmployees(); // rewrite CSV without that employee
            System.out.println("Employee ID " + id + " has been deleted from the system.");
        } else {
            System.out.println(" Employee with ID " + id + " not found.");
        }
    }

    public void addEmployee(int id, String name, Employee.RoleLevel level, String location, String title, int salary) {
        // Check if employee already exists
        for (Employee e : employees) {
            if (e.getId() == id) {
                System.out.println("Employee with ID " + id + " already exists.");
                return;
            }
        }

        Employee newEmp = new Employee(id, name, level, location, title, salary);
        employees.add(newEmp);
        saveEmployees(); // Save to CSV right away
        System.out.println("Added new employee: " + name + " (ID: " + id + ")");
    }

    public boolean employeeExists(int id) {
        return employeeRepo.findEmployeeById(id) != null;
    }


    public void changeSalary(int id, int salary) {

        Employee target = employeeRepo.findEmployeeById(id);

        if (target == null) {
            System.out.println("Employee with ID " + id + " doesn’t exist.");
            return;
        }
        target.setSalary(salary);
        System.out.println(target.getName() + " salary has been changed to " + salary);
        saveEmployees();
    }

    public void changeRoleLevel(int id, Employee.RoleLevel level) {

        Employee target = employeeRepo.findEmployeeById(id);

        if (target == null) {
            System.out.println("Employee with ID " + id + " doesn’t exist.");
            return;
        }

        target.setRoleLevel(level);
        System.out.println(target.getName() + " role level has been changed to " + level);
        saveEmployees();
    }

    public void changeLocation(int id, String location) {
        Employee target = employeeRepo.findEmployeeById(id);

        if (target == null) {
            System.out.println("Employee with ID " + id + " doesn’t exist.");
            return;
        }

        target.setLocation(location);
        System.out.println(target.getName() + " location has been changed to " + location);
        saveEmployees();
    }

    public void getEmployee(int id) {

        Employee target = employeeRepo.findEmployeeById(id);
        List<Employee> list = employeeRepo.getEmployees();

        if (target == null) {
            System.out.println("Employee with ID " + id + " doesn’t exist.");
            return;
        }

        for (Employee e : list) {
            if (e.getId() == id) {
                System.out.println(e.getId() + " - " + e.getName() + " - " + e.getSalary() + " - " + e.getLocation() + " - " + e.getRoleLevel());
            }
        }
    }

    public Employee getEmployeeForReview(int id) {
        return employeeRepo.findEmployeeById(id);
    }

    public void noIDMessage() {
        System.out.println("No reviews found for employee ID");
    }


}
