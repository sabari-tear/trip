<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<jsp:include page="header.jsp"/>

<h2>Available Trip Proposals</h2>

<div class="container">
    <c:if test="${not empty error}">
        <div class="alert alert-danger mb-4">${error}</div>
    </c:if>
    <c:if test="${not empty success}">
        <div class="alert alert-success mb-4">${success}</div>
    </c:if>
    <c:if test="${empty proposals}">
        <div class="alert alert-info">
            No trip proposals are available at the moment. Please check back later!
        </div>
    </c:if>
    <c:forEach items="${proposals}" var="proposal">
        <div class="card mb-4">
            <div class="card-header">
                <div class="d-flex justify-content-between align-items-center">
                    <h5 class="mb-0">${proposal.title}</h5>
                    <span class="badge bg-info">${proposal.stage}</span>
                </div>
            </div>
            <div class="card-body">
                <c:choose>
                    <c:when test="${proposal.stage == 'FINALIZED'}">
                        <div class="alert alert-success">
                            This trip has been finalized! Check your group assignment in the "My Groups" page.
                        </div>
                    </c:when>
                    <c:otherwise>
                        <c:if test="${votedMap[proposal.id]}">
                            <div class="alert alert-info mb-3">
                                You have already voted in this round.
                            </div>
                        </c:if>
                        
                        <form action="${pageContext.request.contextPath}/student/vote" method="post" 
                              class="mb-3 ${votedMap[proposal.id] ? 'disabled' : ''}">
                            <input type="hidden" name="action" value="vote">
                            <input type="hidden" name="proposalId" value="${proposal.id}">
                            
                            <div class="list-group">
                                <c:forEach items="${placesByProposal[proposal.id]}" var="place">
                                    <label class="list-group-item">
                                        <input type="radio" name="placeId" value="${place.id}" 
                                               class="form-check-input me-2" required>
                                        ${place.name}
                                        <small class="text-muted ms-2">
                                            (Votes: ${proposal.stage == 'ROUND1' ? place.votesRound1 : place.votesRound2})
                                        </small>
                                    </label>
                                </c:forEach>
                            </div>
                            
                            <button type="submit" class="btn btn-primary mt-3"
                                    ${votedMap[proposal.id] ? 'disabled' : ''}>
                                Submit Vote
                            </button>
                        </form>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </c:forEach>
</div>

<jsp:include page="footer.jsp"/>
