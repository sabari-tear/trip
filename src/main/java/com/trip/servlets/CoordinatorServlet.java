package com.trip.servlets;



import java.io.IOException;
import java.sql.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import com.trip.util.DBUtil;
import com.trip.model.TripProposal;
import com.trip.model.User;
import java.util.*;

public class CoordinatorServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        if (session.getAttribute("user") == null || !"coordinator".equals(session.getAttribute("role"))) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        
        String action = request.getParameter("action");
        if (action == null) {
            loadDashboard(request, response);
        } else {
            switch (action) {
                case "moveToRound2":
                    moveToRound2(request, response);
                    break;
                case "finalize":
                    finalizeProposal(request, response);
                    break;
                case "viewGroups":
                    viewGroups(request, response);
                    break;
                default:
                    loadDashboard(request, response);
            }
        }
    }
    
    private void loadDashboard(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        User coordinator = (User) request.getSession().getAttribute("user");
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            
            // Get department vote statistics
            String sql = "SELECT tp.id as proposalId, tp.title, tpl.id as placeId, " +
                        "tpl.placeName, COUNT(v.id) as totalVotes, s.department " +
                        "FROM TripProposal tp " +
                        "JOIN TripPlace tpl ON tp.id = tpl.proposalId " +
                        "LEFT JOIN Vote v ON tpl.id = v.placeId " +
                        "LEFT JOIN Student s ON v.studentId = s.id " +
                        "WHERE s.department = ? " +
                        "GROUP BY tp.id, tpl.id " +
                        "ORDER BY tp.id, totalVotes DESC";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, coordinator.getDepartment());
            ResultSet rs = stmt.executeQuery();
            
            Map<Integer, List<Map<String, Object>>> departmentVotes = new HashMap<>();
            while (rs.next()) {
                int proposalId = rs.getInt("proposalId");
                departmentVotes.computeIfAbsent(proposalId, k -> new ArrayList<>());
                
                if (departmentVotes.get(proposalId).size() < 5) {
                    Map<String, Object> placeInfo = new HashMap<>();
                    placeInfo.put("title", rs.getString("title"));
                    placeInfo.put("placeName", rs.getString("placeName"));
                    placeInfo.put("totalVotes", rs.getInt("totalVotes"));
                    departmentVotes.get(proposalId).add(placeInfo);
                }
            }
            request.setAttribute("departmentVotes", departmentVotes);
            
            // Get active proposals with vote counts
            sql = "SELECT p.*, pl.id as placeId, pl.placeName, pl.votesRound1, pl.votesRound2 " +
                        "FROM TripProposal p " +
                        "LEFT JOIN TripPlace pl ON p.id = pl.proposalId " +
                        "WHERE p.isArchived = false AND p.stage != 'FINALIZED' " +
                        "ORDER BY p.id DESC, pl.votesRound1 + pl.votesRound2 DESC";
                        
            Statement stm = conn.createStatement();
            rs = stm.executeQuery(sql);
            
            Map<Integer, TripProposal> proposals = new LinkedHashMap<>();
            Map<Integer, List<Map<String, Object>>> placesByProposal = new HashMap<>();
            
            while (rs.next()) {
                int proposalId = rs.getInt("id");
                
                if (!proposals.containsKey(proposalId)) {
                    TripProposal proposal = new TripProposal();
                    proposal.setId(proposalId);
                    proposal.setTitle(rs.getString("title"));
                    proposal.setStage(rs.getString("stage"));
                    proposal.setArchived(rs.getBoolean("isArchived"));
                    proposal.setFinalizedPlaceId(rs.getInt("finalizedPlaceId"));
                    proposals.put(proposalId, proposal);
                    placesByProposal.put(proposalId, new ArrayList<>());
                }
                
                int placeId = rs.getInt("placeId");
                boolean placeWasNull = rs.wasNull();
                if (!placeWasNull) {
                    Map<String, Object> place = new HashMap<>();
                    place.put("id", placeId);
                    place.put("name", rs.getString("placeName"));
                    place.put("votesRound1", rs.getInt("votesRound1"));
                    place.put("votesRound2", rs.getInt("votesRound2"));
                    placesByProposal.get(proposalId).add(place);
                }
            }
            
            request.setAttribute("proposals", proposals.values());
            request.setAttribute("placesByProposal", placesByProposal);
            
            request.getRequestDispatcher("/WEB-INF/jsp/coordinator.jsp").forward(request, response);
            
        } catch (SQLException e) {
            throw new ServletException("Database error occurred", e);
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    private void moveToRound2(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        int proposalId = Integer.parseInt(request.getParameter("id"));
        
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            
            // Update proposal stage
            String sql = "UPDATE TripProposal SET stage = 'ROUND2' WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, proposalId);
            stmt.executeUpdate();
            
            // Get top 2 places by votes
            sql = "SELECT id FROM TripPlace WHERE proposalId = ? ORDER BY votesRound1 DESC LIMIT 2";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, proposalId);
            ResultSet rs = stmt.executeQuery();
            
            List<Integer> topPlaces = new ArrayList<>();
            while (rs.next()) {
                topPlaces.add(rs.getInt("id"));
            }
            
            // Delete places not in top 2
            if (!topPlaces.isEmpty()) {
                sql = "DELETE FROM TripPlace WHERE proposalId = ? AND id NOT IN (" + 
                      String.join(",", Collections.nCopies(topPlaces.size(), "?")) + ")";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, proposalId);
                for (int i = 0; i < topPlaces.size(); i++) {
                    stmt.setInt(i + 2, topPlaces.get(i));
                }
                stmt.executeUpdate();

                // Reset Round 2 votes when moving into Round 2
                sql = "UPDATE TripPlace SET votesRound2 = 0 WHERE proposalId = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, proposalId);
                stmt.executeUpdate();
            }
            
            conn.commit();
            response.sendRedirect(request.getContextPath() + "/coordinator/dashboard");
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new ServletException("Database error occurred", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private void finalizeProposal(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        int proposalId = Integer.parseInt(request.getParameter("id"));
        
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            
            // Get winning place
            String sql = "SELECT id FROM TripPlace WHERE proposalId = ? " +
                        "ORDER BY (votesRound1 + votesRound2) DESC LIMIT 1";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, proposalId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int winningPlaceId = rs.getInt("id");
                
                // Update proposal
                sql = "UPDATE TripProposal SET stage = 'FINALIZED', finalizedPlaceId = ? WHERE id = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, winningPlaceId);
                stmt.setInt(2, proposalId);
                stmt.executeUpdate();
                
                // Get max group size
                sql = "SELECT maxGroupSize FROM Settings LIMIT 1";
                Statement settingsStmt = conn.createStatement();
                rs = settingsStmt.executeQuery(sql);
                int maxGroupSize = rs.next() ? rs.getInt("maxGroupSize") : 5;
                settingsStmt.close();
                
                // Get all students in the system (grouping is for all students, not only voters)
                sql = "SELECT id FROM Student ORDER BY id";
                Statement studentStmt = conn.createStatement();
                rs = studentStmt.executeQuery(sql);

                List<Integer> students = new ArrayList<>();
                while (rs.next()) {
                    students.add(rs.getInt("id"));
                }
                studentStmt.close();
                
                // Create groups
                int groupNumber = 1;
                for (int i = 0; i < students.size(); i += maxGroupSize) {
                    for (int j = i; j < Math.min(i + maxGroupSize, students.size()); j++) {
                        sql = "INSERT INTO `Group` (proposalId, groupNumber, studentId) VALUES (?, ?, ?)";
                        stmt = conn.prepareStatement(sql);
                        stmt.setInt(1, proposalId);
                        stmt.setInt(2, groupNumber);
                        stmt.setInt(3, students.get(j));
                        stmt.executeUpdate();
                    }
                    groupNumber++;
                }
            }
            
            conn.commit();
            response.sendRedirect(request.getContextPath() + "/coordinator/dashboard");
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new ServletException("Database error occurred", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private void viewGroups(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            
            String sql = "SELECT p.id, p.title, g.groupNumber, s.name as studentName " +
                        "FROM TripProposal p " +
                        "JOIN `Group` g ON p.id = g.proposalId " +
                        "JOIN Student s ON g.studentId = s.id " +
                        "WHERE p.stage = 'FINALIZED' " +
                        "ORDER BY p.id, g.groupNumber, s.name";
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            Map<Integer, String> proposalTitles = new HashMap<>();
            Map<Integer, Map<Integer, List<String>>> groupsByProposal = new HashMap<>();
            
            while (rs.next()) {
                int proposalId = rs.getInt("id");
                String title = rs.getString("title");
                int groupNumber = rs.getInt("groupNumber");
                String studentName = rs.getString("studentName");
                
                proposalTitles.putIfAbsent(proposalId, title);
                groupsByProposal.putIfAbsent(proposalId, new HashMap<>());
                groupsByProposal.get(proposalId).putIfAbsent(groupNumber, new ArrayList<>());
                groupsByProposal.get(proposalId).get(groupNumber).add(studentName);
            }
            
            request.setAttribute("proposalTitles", proposalTitles);
            request.setAttribute("groupsByProposal", groupsByProposal);
            
            request.getRequestDispatcher("/WEB-INF/jsp/groups.jsp").forward(request, response);
            
        } catch (SQLException e) {
            throw new ServletException("Database error occurred", e);
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("addProposal".equals(action)) {
            addTripProposal(request, response);
        } else {
            doGet(request, response);
        }
    }

    private void addTripProposal(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String title = request.getParameter("title");
        String[] places = request.getParameterValues("places[]");
        
        if (title == null || places == null || places.length < 2) {
            request.setAttribute("error", "Title and at least 2 places are required");
            loadDashboard(request, response);
            return;
        }

        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // Insert trip proposal
            String sql = "INSERT INTO TripProposal (title, stage) VALUES (?, 'ROUND1')";
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, title);
            stmt.executeUpdate();

            // Get the generated proposal ID
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int proposalId = rs.getInt(1);

                // Insert places
                sql = "INSERT INTO TripPlace (proposalId, placeName) VALUES (?, ?)";
                stmt = conn.prepareStatement(sql);
                for (String place : places) {
                    if (place != null && !place.trim().isEmpty()) {
                        stmt.setInt(1, proposalId);
                        stmt.setString(2, place.trim());
                        stmt.executeUpdate();
                    }
                }
                conn.commit();
                request.setAttribute("success", "Trip proposal created successfully!");
            }
            loadDashboard(request, response);

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            request.setAttribute("error", "Failed to create trip proposal");
            loadDashboard(request, response);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
