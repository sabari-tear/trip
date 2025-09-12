<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<jsp:include page="header.jsp"/>

<h2>Student Groups</h2>

<div class="container">
    <c:forEach items="${proposalTitles}" var="proposal">
        <div class="card mb-4">
            <div class="card-header">
                <h4>${proposal.value}</h4>
            </div>
            <div class="card-body">
                <c:forEach items="${groupsByProposal[proposal.key]}" var="group">
                    <div class="card mb-3">
                        <div class="card-header">
                            <h5 class="mb-0">Group ${group.key}</h5>
                        </div>
                        <div class="card-body">
                            <ul class="list-group">
                                <c:forEach items="${group.value}" var="student">
                                    <li class="list-group-item">${student}</li>
                                </c:forEach>
                            </ul>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </div>
    </c:forEach>
</div>

<jsp:include page="footer.jsp"/>
