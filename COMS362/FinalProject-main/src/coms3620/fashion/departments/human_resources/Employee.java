package coms3620.fashion.departments.human_resources;

public class Employee {
    private int id;
    private String name;
    private RoleLevel roleLevel;
    private String location;
    private String title;
    private int salary;


public Employee(int id, String name, RoleLevel roleLevel, String location, String title, int salary) {
    this.id = id;
    this.name = name;
    this.roleLevel = roleLevel;
    this.location = location;
    this.title = title;
    this.salary = salary;
}

public enum RoleLevel {
    JUNIOR,
    SENIOR,
    MANAGER
    }







    public int getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public String getTitle() { return title; }
    public int getSalary() { return salary; }
    public void setSalary(int salary) { this.salary = salary; }
    public RoleLevel getRoleLevel(){ return roleLevel; }
    public void setRoleLevel(RoleLevel roleLevel){ this.roleLevel = roleLevel; }
    public void setLocation(String location) { this.location = location; }

    public void fire(String reason) {
        System.out.println("Employee " + name + " has been fired for: " + reason);
    }

    @Override
    public String toString() {
        return id + "," + name;
    }

    
}

/*
id,name,roleLevel,salary,location
1,Ethan Duong,JUNIOR,45000,Des Moines
2,Emma Johnson,SENIOR,72000,Ames
3,Michael Smith,MANAGER,92000,Chicago
4,Isabella Martinez,JUNIOR,43000,Minneapolis
5,Liam Nguyen,SENIOR,76000,Kansas City
6,Olivia Brown,JUNIOR,41000,Omaha
7,Noah Wilson,MANAGER,88000,Des Moines
8,Ava Davis,SENIOR,70000,Ames
9,James Thompson,JUNIOR,46000,Chicago
10,Sophia Lee,SENIOR,75000,San Francisco


 */



