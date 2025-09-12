package com.trip.model;

public class User {
    private int id;
    private String username;
    private String password;
    private String department;
    private String role;
    
    public User() {}
    
    public User(int id, String username, String password, String department, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.department = department;
        this.role = role;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
