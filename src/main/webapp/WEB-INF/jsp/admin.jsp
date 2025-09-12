<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<jsp:include page="header.jsp"/>

<h2>Admin Dashboard</h2>

<!-- Coordinator Management Section -->
<div class="card mb-4">
    <div class="card-header">
        <h4>Manage Coordinators</h4>
    </div>
    <div class="card-body">
        <form action="${pageContext.request.contextPath}/admin/dashboard?action=addCoordinator" method="post" class="mb-4">
            <div class="row">
                <div class="col-md-3">
                    <div class="mb-3">
                        <label for="username" class="form-label">Username</label>
                        <input type="text" class="form-control" id="username" name="username" required>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="mb-3">
                        <label for="password" class="form-label">Password</label>
                        <input type="password" class="form-control" id="password" name="password" required>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="mb-3">
                        <label for="department" class="form-label">Department</label>
                        <input type="text" class="form-control" id="department" name="department" required>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="mb-3">
                        <label class="form-label">&nbsp;</label>
                        <button type="submit" class="btn btn-primary d-block">Add Coordinator</button>
                    </div>
                </div>
            </div>
        </form>

        <table class="table">
            <thead>
                <tr>
                    <th>Username</th>
                    <th>Department</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${coordinators}" var="coordinator">
                    <tr>
                        <td>${coordinator.username}</td>
                        <td>${coordinator.department}</td>
                        <td>
                            <a href="${pageContext.request.contextPath}/admin/dashboard?action=removeCoordinator&id=${coordinator.id}" 
                               class="btn btn-danger btn-sm" 
                               onclick="return confirm('Are you sure you want to remove this coordinator?')">
                                Remove
                            </a>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
</div>

<!-- Settings Section -->
<div class="card mb-4">
    <div class="card-header">
        <h4>System Settings</h4>
    </div>
    <div class="card-body">
        <form action="${pageContext.request.contextPath}/admin/dashboard?action=updateSettings" method="post">
            <div class="mb-3">
                <label for="maxGroupSize" class="form-label">Maximum Group Size</label>
                <input type="number" class="form-control" id="maxGroupSize" name="maxGroupSize" 
                       value="${maxGroupSize}" min="1" required>
            </div>
            <button type="submit" class="btn btn-primary">Update Settings</button>
        </form>
    </div>
</div>

<!-- Trip Proposals Section -->
<div class="card">
    <div class="card-header">
        <h4>Trip Proposals</h4>
    </div>
    <div class="card-body">
        <table class="table">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Title</th>
                    <th>Stage</th>
                    <th>Status</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${proposals}" var="proposal">
                    <tr>
                        <td>${proposal.id}</td>
                        <td>${proposal.title}</td>
                        <td>${proposal.stage}</td>
                        <td>
                            <span class="badge ${proposal.archived ? 'bg-secondary' : 'bg-success'}">
                                ${proposal.archived ? 'Archived' : 'Active'}
                            </span>
                        </td>
                        <td>
                            <c:if test="${proposal.stage != 'ROUND1'}">
                                <a href="${pageContext.request.contextPath}/admin/dashboard?action=resetProposal&id=${proposal.id}" 
                                   class="btn btn-warning btn-sm">Reset to Round 1</a>
                            </c:if>
                            <c:if test="${!proposal.archived}">
                                <a href="${pageContext.request.contextPath}/admin/dashboard?action=archiveProposal&id=${proposal.id}&archive=true" 
                                   class="btn btn-danger btn-sm">Archive</a>
                            </c:if>
                            <c:if test="${proposal.archived}">
                                <a href="${pageContext.request.contextPath}/admin/dashboard?action=archiveProposal&id=${proposal.id}&archive=false" 
                                   class="btn btn-success btn-sm">Unarchive</a>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
</div>

<jsp:include page="footer.jsp"/>
