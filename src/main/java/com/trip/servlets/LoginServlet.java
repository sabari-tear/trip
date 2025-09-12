package com.trip.servlets;

import java.io.IOException;
import java.sql.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import com.trip.util.DBUtil;
import com.trip.model.User;

public class LoginServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        if ("register".equals(action)) {
            handleRegistration(request, response);
        } else {
            handleLogin(request, response);
        }
    }
    
    private void handleLogin(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            User user = authenticateUser(conn, username, password);
            
            if (user != null) {
                System.out.println("User authenticated: " + user.getUsername() + " with role: " + user.getRole());
                HttpSession session = request.getSession();
                session.setAttribute("user", user);
                session.setAttribute("role", user.getRole());
                
                String redirectUrl = "";
                switch (user.getRole()) {
                    case "admin":
                        redirectUrl = "admin/dashboard";
                        break;
                    case "coordinator":
                        redirectUrl = "coordinator/dashboard";
                        break;
                    case "student":
                        redirectUrl = "student/dashboard";
                        break;
                    default:
                        System.out.println("Unknown role: " + user.getRole());
                        request.setAttribute("error", "Invalid user role");
                        request.getRequestDispatcher("/login.jsp").forward(request, response);
                        return;
                }
                System.out.println("Redirecting to: " + request.getContextPath() + "/" + redirectUrl);
                response.sendRedirect(request.getContextPath() + "/" + redirectUrl);
            } else {
                System.out.println("Authentication failed for username: " + username);
                request.setAttribute("error", "Invalid username or password");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
            }
        } catch (SQLException e) {
            throw new ServletException("Database error occurred", e);
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    private void handleRegistration(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String name = request.getParameter("name");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String department = request.getParameter("department");
        
        System.out.println("Attempting to register student: " + username + " from department: " + department);
        
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            if (registerStudent(conn, name, username, password, department)) {
                System.out.println("Student registration successful for: " + username);
                request.setAttribute("success", "Registration successful! Please login.");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
            } else {
                System.out.println("Student registration failed for: " + username);
                request.setAttribute("error", "Registration failed. Username might be taken.");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
            }
        } catch (SQLException e) {
            throw new ServletException("Database error occurred", e);
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    private User authenticateUser(Connection conn, String username, String password) throws SQLException {
        // Try admin table
        String sql = "SELECT id, 'admin' as role FROM Admin WHERE username = ? AND password = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, username);
        stmt.setString(2, password);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            return new User(rs.getInt("id"), username, password, null, "admin");
        }
        
        // Try coordinator table
        sql = "SELECT id, department FROM Coordinator WHERE username = ? AND password = ?";
        stmt = conn.prepareStatement(sql);
        stmt.setString(1, username);
        stmt.setString(2, password);
        rs = stmt.executeQuery();
        
        if (rs.next()) {
            return new User(rs.getInt("id"), username, password, rs.getString("department"), "coordinator");
        }
        
        // Try student table
        sql = "SELECT id, department FROM Student WHERE username = ? AND password = ?";
        stmt = conn.prepareStatement(sql);
        stmt.setString(1, username);
        stmt.setString(2, password);
        rs = stmt.executeQuery();
        
        if (rs.next()) {
            return new User(rs.getInt("id"), username, password, rs.getString("department"), "student");
        }
        
        return null;
    }
    
    private boolean registerStudent(Connection conn, String name, String username, String password, String department) 
            throws SQLException {
        String sql = "INSERT INTO Student (name, username, password, department) VALUES (?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, name);
        stmt.setString(2, username);
        stmt.setString(3, password);
        stmt.setString(4, department);
        
        try {
            return stmt.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            return false;
        }
    }
}
