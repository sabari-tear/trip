package com.trip.servlets;

import java.io.IOException;
import java.sql.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import com.trip.util.DBUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.trip.model.TripProposal;

public class AdminServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        if (session.getAttribute("user") == null || !"admin".equals(session.getAttribute("role"))) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        
        String action = request.getParameter("action");
        if (action == null) {
            loadDashboard(request, response);
        } else {
            switch (action) {
                case "updateSettings":
                    updateSettings(request, response);
                    break;
                case "resetProposal":
                    resetProposal(request, response);
                    break;
                case "archiveProposal":
                    archiveProposal(request, response);
                    break;
                case "removeCoordinator":
                    removeCoordinator(request, response);
                    break;
                default:
                    loadDashboard(request, response);
            }
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("addCoordinator".equals(action)) {
            addCoordinator(request, response);
        } else {
            doGet(request, response);
        }
    }

    private void addCoordinator(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String department = request.getParameter("department");
        
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "INSERT INTO Coordinator (username, password, department) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, department);
            stmt.executeUpdate();
            
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
        } catch (SQLException e) {
            request.setAttribute("error", "Failed to add coordinator. Username might be taken.");
            loadDashboard(request, response);
        } finally {
            DBUtil.closeConnection(conn);
        }
    }

    private void removeCoordinator(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        int coordinatorId = Integer.parseInt(request.getParameter("id"));
        
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "DELETE FROM Coordinator WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, coordinatorId);
            stmt.executeUpdate();
            
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
        } catch (SQLException e) {
            request.setAttribute("error", "Failed to remove coordinator.");
            loadDashboard(request, response);
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    private void loadDashboard(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            
            // Get settings
            String sql = "SELECT maxGroupSize FROM Settings LIMIT 1";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                request.setAttribute("maxGroupSize", rs.getInt("maxGroupSize"));
            }
            
            // Get trip proposals
            sql = "SELECT * FROM TripProposal ORDER BY id DESC";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            
            List<TripProposal> proposals = new ArrayList<>();
            while (rs.next()) {
                TripProposal proposal = new TripProposal();
                proposal.setId(rs.getInt("id"));
                proposal.setTitle(rs.getString("title"));
                proposal.setStage(rs.getString("stage"));
                proposal.setArchived(rs.getBoolean("isArchived"));
                proposal.setFinalizedPlaceId(rs.getInt("finalizedPlaceId"));
                proposals.add(proposal);
            }
            request.setAttribute("proposals", proposals);
            
            // Get coordinators
            sql = "SELECT * FROM Coordinator ORDER BY department, username";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            
            List<Map<String, Object>> coordinators = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> coordinator = new HashMap<>();
                coordinator.put("id", rs.getInt("id"));
                coordinator.put("username", rs.getString("username"));
                coordinator.put("department", rs.getString("department"));
                coordinators.add(coordinator);
            }
            request.setAttribute("coordinators", coordinators);
            
            request.getRequestDispatcher("/WEB-INF/jsp/admin.jsp").forward(request, response);
            
        } catch (SQLException e) {
            throw new ServletException("Database error occurred", e);
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    private void updateSettings(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        int maxGroupSize = Integer.parseInt(request.getParameter("maxGroupSize"));
        
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "UPDATE Settings SET maxGroupSize = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, maxGroupSize);
            stmt.executeUpdate();
            
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
        } catch (SQLException e) {
            throw new ServletException("Database error occurred", e);
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    private void resetProposal(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        int proposalId = Integer.parseInt(request.getParameter("id"));
        
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            
            // Reset proposal to Round 1
            String sql = "UPDATE TripProposal SET stage = 'ROUND1', finalizedPlaceId = NULL WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, proposalId);
            stmt.executeUpdate();
            
            // Reset votes
            sql = "UPDATE TripPlace SET votesRound1 = 0, votesRound2 = 0 WHERE proposalId = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, proposalId);
            stmt.executeUpdate();
            
            // Delete votes
            sql = "DELETE FROM Vote WHERE proposalId = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, proposalId);
            stmt.executeUpdate();
            
            // Delete groups
            sql = "DELETE FROM `Group` WHERE proposalId = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, proposalId);
            stmt.executeUpdate();
            
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
        } catch (SQLException e) {
            throw new ServletException("Database error occurred", e);
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    private void archiveProposal(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        int proposalId = Integer.parseInt(request.getParameter("id"));
        boolean archive = Boolean.parseBoolean(request.getParameter("archive"));
        
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "UPDATE TripProposal SET isArchived = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setBoolean(1, archive);
            stmt.setInt(2, proposalId);
            stmt.executeUpdate();
            
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
        } catch (SQLException e) {
            throw new ServletException("Database error occurred", e);
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
}
