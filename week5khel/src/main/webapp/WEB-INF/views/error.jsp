<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Error - Khel Sports App</title>
</head>
<body>
    <div class="error-container">
        <h1 class="error-code">
            <c:choose>
                <c:when test="${pageContext.errorData.statusCode == 404}">404</c:when>
                <c:when test="${pageContext.errorData.statusCode == 500}">500</c:when>
                <c:otherwise>Error</c:otherwise>
            </c:choose>
        </h1>

        <div class="error-message">
            <c:choose>
                <c:when test="${pageContext.errorData.statusCode == 404}">Page Not Found</c:when>
                <c:when test="${pageContext.errorData.statusCode == 500}">Internal Server Error</c:when>
                <c:otherwise>Something went wrong</c:otherwise>
            </c:choose>
        </div>

        <div class="error-description">
            <c:choose>
                <c:when test="${pageContext.errorData.statusCode == 404}">
                    The page you're looking for doesn't exist or has been moved.
                </c:when>
                <c:when test="${pageContext.errorData.statusCode == 500}">
                    We're experiencing some technical difficulties. Please try again later.
                </c:when>
                <c:otherwise>
                    An unexpected error occurred. Please try again or contact support.
                </c:otherwise>
            </c:choose>
        </div>

        <a href="${pageContext.request.contextPath}/" class="btn">Return to Home</a>
    </div>
</body>
</html>