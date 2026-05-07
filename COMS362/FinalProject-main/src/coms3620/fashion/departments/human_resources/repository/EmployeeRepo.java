package coms3620.fashion.departments.human_resources.repository;

import coms3620.fashion.departments.human_resources.Employee;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeRepo {

    private List<Employee> employees = new ArrayList<>();
    private static final String FILE_NAME = "data/human_resources/employees.csv";

    public void loadEmployees() {
        employees.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String line = br.readLine(); // skip header
            if (line == null) return;

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");

int id = Integer.parseInt(data[0]);
                String name = data[1].trim();
                Employee.RoleLevel level = Employee.RoleLevel.valueOf(data[2].trim().toUpperCase());
                String location = data[3].trim();
                String title = data[4].trim();
                int salary = Integer.parseInt(data[5].trim());
                

                employees.add(new Employee(id, name, level, location, title, salary));
            }

            System.out.println("Employees loaded from " + FILE_NAME);

        } catch (IOException e) {
            System.out.println("No existing CSV found. Starting fresh.");
        }
    }

    public void saveEmployees() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME))) {
            pw.println("id,name,level,location,title,salary");

            for (Employee e : employees) {
                pw.println(String.join(",",
                        String.valueOf(e.getId()),
                        e.getName(),
                        e.getRoleLevel().name(),
                        e.getLocation(),
                        e.getTitle(),
                        String.valueOf(e.getSalary())
                ));
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public Employee findEmployeeById(int id) {
        for (Employee e : employees) {
            if (e.getId() == id) {
                return e;
            }
        }
        return null; // not found
    }

}
