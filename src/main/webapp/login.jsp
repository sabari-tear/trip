<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - Trip Planning Portal</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
    <div class="container mt-5">
        <div class="row justify-content-center">
            <div class="col-md-6">
                <div class="card">
                    <div class="card-header">
                        <ul class="nav nav-tabs card-header-tabs">
                            <li class="nav-item">
                                <a class="nav-link active" href="#login" data-bs-toggle="tab">Login</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="#register" data-bs-toggle="tab">Student Registration</a>
                            </li>
                        </ul>
                    </div>
                    <div class="card-body">
                        <div class="tab-content">
                            <div class="tab-pane fade show active" id="login">
                                <form action="${pageContext.request.contextPath}/login" method="post">
                                    <div class="mb-3">
                                        <label for="username" class="form-label">Username</label>
                                        <input type="text" class="form-control" id="username" name="username" required>
                                    </div>
                                    <div class="mb-3">
                                        <label for="password" class="form-label">Password</label>
                                        <input type="password" class="form-control" id="password" name="password" required>
                                    </div>
                                    <button type="submit" class="btn btn-primary">Login</button>
                                </form>
                            </div>
                            <div class="tab-pane fade" id="register">
                                <form action="${pageContext.request.contextPath}/login?action=register" method="post">
                                    <div class="mb-3">
                                        <label for="regName" class="form-label">Full Name</label>
                                        <input type="text" class="form-control" id="regName" name="name" required>
                                    </div>
                                    <div class="mb-3">
                                        <label for="regUsername" class="form-label">Username</label>
                                        <input type="text" class="form-control" id="regUsername" name="username" required>
                                    </div>
                                    <div class="mb-3">
                                        <label for="regPassword" class="form-label">Password</label>
                                        <input type="password" class="form-control" id="regPassword" name="password" required>
                                    </div>
                                    <div class="mb-3">
                                        <label for="department" class="form-label">Department</label>
                                        <input type="text" class="form-control" id="department" name="department" required>
                                    </div>
                                    <button type="submit" class="btn btn-primary">Register</button>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>
                <c:if test="${not empty error}">
                    <div class="alert alert-danger mt-3">${error}</div>
                </c:if>
                <c:if test="${not empty success}">
                    <div class="alert alert-success mt-3">${success}</div>
                </c:if>
            </div>
        </div>
    </div>
    <script>
        // Initialize Bootstrap tabs
        document.addEventListener('DOMContentLoaded', function() {
            var triggerTabList = [].slice.call(document.querySelectorAll('.nav-tabs a'));
            triggerTabList.forEach(function(triggerEl) {
                var tabTrigger = new bootstrap.Tab(triggerEl);
                triggerEl.addEventListener('click', function(event) {
                    event.preventDefault();
                    tabTrigger.show();
                });
            });
        });
    </script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
