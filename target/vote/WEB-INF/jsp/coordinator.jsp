<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<jsp:include page="header.jsp"/>

<h2>Coordinator Dashboard</h2>

<c:if test="${not empty error}">
    <div class="alert alert-danger mb-4">${error}</div>
</c:if>
<c:if test="${not empty success}">
    <div class="alert alert-success mb-4">${success}</div>
</c:if>

<!-- Department Vote Statistics -->
<div class="card mb-4">
    <div class="card-header">
        <h4>Department Vote Statistics (Top 5 Places)</h4>
    </div>
    <div class="card-body">
        <c:forEach items="${departmentVotes}" var="entry">
            <div class="mb-4">
                <h5>${entry.value[0].title}</h5>
                <table class="table table-striped">
                    <thead>
                        <tr>
                            <th>Place</th>
                            <th>Total Votes</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${entry.value}" var="place">
                            <tr>
                                <td>${place.placeName}</td>
                                <td>${place.totalVotes}</td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:forEach>
    </div>
</div>

<!-- Add New Trip Proposal -->
<div class="card mb-4">
    <div class="card-header">
        <h4>Add New Trip Proposal</h4>
    </div>
    <div class="card-body">
        <form action="${pageContext.request.contextPath}/coordinator/dashboard?action=addProposal" method="post">
            <div class="mb-3">
                <label for="title" class="form-label">Trip Title</label>
                <input type="text" class="form-control" id="title" name="title" required>
            </div>
            <div id="placesContainer">
                <div class="mb-3">
                    <label class="form-label">Place Options (minimum 2)</label>
                    <div class="input-group mb-2">
                        <input type="text" class="form-control" name="places[]" required>
                        <button type="button" class="btn btn-outline-danger" onclick="removePlace(this)">Remove</button>
                    </div>
                    <div class="input-group mb-2">
                        <input type="text" class="form-control" name="places[]" required>
                        <button type="button" class="btn btn-outline-danger" onclick="removePlace(this)">Remove</button>
                    </div>
                </div>
            </div>
            <button type="button" class="btn btn-secondary mb-3" onclick="addPlace()">Add Another Place</button>
            <div>
                <button type="submit" class="btn btn-primary">Create Trip Proposal</button>
            </div>
        </form>
    </div>
</div>

<!-- Existing Trip Proposals -->
<div class="card">
    <div class="card-header">
        <h4>Active Trip Proposals</h4>
    </div>
    <div class="card-body">
        <c:forEach items="${proposals}" var="proposal">
            <div class="card mb-4">
                <div class="card-header">
                    <div class="d-flex justify-content-between align-items-center">
                        <h5 class="mb-0">${proposal.title}</h5>
                        <span class="badge bg-info">${proposal.stage}</span>
                    </div>
                </div>
                <div class="card-body">
                    <table class="table">
                        <thead>
                            <tr>
                                <th>Place</th>
                                <th>Round 1 Votes</th>
                                <th>Round 2 Votes</th>
                                <th>Total Votes</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${placesByProposal[proposal.id]}" var="place">
                                <tr>
                                    <td>${place.name}</td>
                                    <td>${place.votesRound1}</td>
                                    <td>${place.votesRound2}</td>
                                    <td>${place.votesRound1 + place.votesRound2}</td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                    
                    <div class="mt-3">
                        <c:choose>
                            <c:when test="${proposal.stage == 'ROUND1'}">
                                <a href="${pageContext.request.contextPath}/coordinator/dashboard?action=moveToRound2&id=${proposal.id}" 
                                   class="btn btn-primary">Move to Round 2</a>
                            </c:when>
                            <c:when test="${proposal.stage == 'ROUND2'}">
                                <a href="${pageContext.request.contextPath}/coordinator/dashboard?action=finalize&id=${proposal.id}" 
                                   class="btn btn-success">Finalize Trip</a>
                            </c:when>
                            <c:otherwise>
                                <a href="${pageContext.request.contextPath}/coordinator/dashboard?action=viewGroups&id=${proposal.id}" 
                                   class="btn btn-info">View Groups</a>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </c:forEach>
    </div>
</div>

<jsp:include page="footer.jsp"/>
