package com.trip.servlets;

import java.io.IOException;
import java.sql.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import com.trip.util.DBUtil;
import com.trip.model.*;
import java.util.*;

public class StudentServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        if (session.getAttribute("user") == null || !"student".equals(session.getAttribute("role"))) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/dashboard")) {
            loadDashboard(request, response);
        } else if (pathInfo.equals("/mygroups")) {
            loadMyGroups(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        if ("vote".equals(action)) {
            handleVote(request, response);
        } else {
            doGet(request, response);
        }
    }
    
    private void loadDashboard(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("Loading student dashboard");
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            System.out.println("No user in session");
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        Connection conn = null;
        
        try {
            conn = DBUtil.getConnection();
            
            // Get active proposals with places and user's votes
            String sql = "SELECT p.*, pl.id as placeId, pl.placeName, " +
                        "pl.votesRound1, pl.votesRound2, v.id as voteId " +
                        "FROM TripProposal p " +
                        "LEFT JOIN TripPlace pl ON p.id = pl.proposalId " +
                        "LEFT JOIN Vote v ON pl.id = v.placeId AND v.studentId = ? " +
                        "AND ((p.stage = 'ROUND1' AND v.round = 1) OR (p.stage = 'ROUND2' AND v.round = 2)) " +
                        "WHERE p.isArchived = false " +
                        "ORDER BY p.id DESC, pl.votesRound1 + pl.votesRound2 DESC";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, user.getId());
            ResultSet rs = stmt.executeQuery();
            
            Map<Integer, TripProposal> proposals = new LinkedHashMap<>();
            Map<Integer, List<Map<String, Object>>> placesByProposal = new HashMap<>();
            Set<Integer> votedProposals = new HashSet<>();
            
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
                
                if (rs.getObject("voteId") != null) {
                    votedProposals.add(proposalId);
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
            
            System.out.println("Proposals found: " + proposals.size());
            System.out.println("Places by proposal: " + placesByProposal.size());
            System.out.println("Voted proposals: " + votedProposals.size());
            
            // Build a lookup map for EL instead of calling collection methods in JSP
            Map<Integer, Boolean> votedMap = new HashMap<>();
            for (Integer pid : votedProposals) {
                votedMap.put(pid, Boolean.TRUE);
            }

            request.setAttribute("proposals", proposals.values());
            request.setAttribute("placesByProposal", placesByProposal);
            request.setAttribute("votedMap", votedMap);
            
            System.out.println("Forwarding to student.jsp");
            request.getRequestDispatcher("/WEB-INF/jsp/student.jsp").forward(request, response);
            
        } catch (SQLException e) {
            throw new ServletException("Database error occurred", e);
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    private void loadMyGroups(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        User user = (User) request.getSession().getAttribute("user");
        Connection conn = null;
        
        try {
            conn = DBUtil.getConnection();
            
            String sql = "SELECT p.id, p.title, pl.placeName as destination, " +
                        "g1.groupNumber, s.name as studentName " +
                        "FROM `Group` g1 " +
                        "JOIN TripProposal p ON g1.proposalId = p.id " +
                        "JOIN `Group` g2 ON g1.proposalId = g2.proposalId AND g1.groupNumber = g2.groupNumber " +
                        "JOIN Student s ON g2.studentId = s.id " +
                        "JOIN TripPlace pl ON p.finalizedPlaceId = pl.id " +
                        "WHERE g1.studentId = ? AND p.stage = 'FINALIZED' AND p.isArchived = false " +
                        "ORDER BY p.id DESC, s.name";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, user.getId());
            ResultSet rs = stmt.executeQuery();
            
            Map<Integer, Map<String, Object>> myGroups = new LinkedHashMap<>();
            
            while (rs.next()) {
                int proposalId = rs.getInt("id");
                
                if (!myGroups.containsKey(proposalId)) {
                    Map<String, Object> groupInfo = new HashMap<>();
                    groupInfo.put("title", rs.getString("title"));
                    groupInfo.put("destination", rs.getString("destination"));
                    groupInfo.put("groupNumber", rs.getInt("groupNumber"));
                    groupInfo.put("members", new ArrayList<String>());
                    myGroups.put(proposalId, groupInfo);
                }
                
                ((List<String>) myGroups.get(proposalId).get("members")).add(rs.getString("studentName"));
            }
            
            request.setAttribute("myGroups", myGroups);
            request.getRequestDispatcher("/WEB-INF/jsp/mygroup.jsp").forward(request, response);
            
        } catch (SQLException e) {
            throw new ServletException("Database error occurred", e);
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    private void handleVote(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        User user = (User) request.getSession().getAttribute("user");
        int proposalId = Integer.parseInt(request.getParameter("proposalId"));
        int placeId = Integer.parseInt(request.getParameter("placeId"));
        
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            
            // Get proposal stage
            String sql = "SELECT stage FROM TripProposal WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, proposalId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String stage = rs.getString("stage");
                int round = stage.equals("ROUND1") ? 1 : 2;

                // Ensure the selected place belongs to the provided proposal
                sql = "SELECT 1 FROM TripPlace WHERE id = ? AND proposalId = ?";
                PreparedStatement placeCheckStmt = conn.prepareStatement(sql);
                placeCheckStmt.setInt(1, placeId);
                placeCheckStmt.setInt(2, proposalId);
                ResultSet placeCheckRs = placeCheckStmt.executeQuery();

                if (placeCheckRs.next()) {
                    // Check if user already voted in this round
                    sql = "SELECT 1 FROM Vote WHERE studentId = ? AND proposalId = ? AND round = ?";
                    stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, user.getId());
                    stmt.setInt(2, proposalId);
                    stmt.setInt(3, round);
                    rs = stmt.executeQuery();

                    if (!rs.next()) {
                        // Record vote
                        sql = "INSERT INTO Vote (studentId, proposalId, placeId, round) VALUES (?, ?, ?, ?)";
                        stmt = conn.prepareStatement(sql);
                        stmt.setInt(1, user.getId());
                        stmt.setInt(2, proposalId);
                        stmt.setInt(3, placeId);
                        stmt.setInt(4, round);
                        stmt.executeUpdate();

                        // Update vote count
                        sql = "UPDATE TripPlace SET " +
                              (round == 1 ? "votesRound1 = votesRound1 + 1" : "votesRound2 = votesRound2 + 1") +
                              " WHERE id = ?";
                        stmt = conn.prepareStatement(sql);
                        stmt.setInt(1, placeId);
                        stmt.executeUpdate();
                    }
                }
            }
            
            conn.commit();
            request.setAttribute("success", "Your vote has been recorded successfully!");
            loadDashboard(request, response);
            
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
}
