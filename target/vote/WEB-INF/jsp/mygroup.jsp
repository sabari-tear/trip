<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<jsp:include page="header.jsp"/>

<h2>My Groups</h2>

<div class="container">
    <c:forEach items="${myGroups}" var="group">
        <div class="card mb-4">
            <div class="card-header">
                <h4>${group.value.title}</h4>
                <h6 class="text-muted">Destination: ${group.value.destination}</h6>
            </div>
            <div class="card-body">
                <h5>Group ${group.value.groupNumber}</h5>
                <ul class="list-group">
                    <c:forEach items="${group.value.members}" var="member">
                        <li class="list-group-item">${member}</li>
                    </c:forEach>
                </ul>
            </div>
        </div>
    </c:forEach>
    
    <c:if test="${empty myGroups}">
        <div class="alert alert-info">
            You are not part of any groups yet. Vote on available trips to join groups!
        </div>
    </c:if>
</div>

<jsp:include page="footer.jsp"/>
