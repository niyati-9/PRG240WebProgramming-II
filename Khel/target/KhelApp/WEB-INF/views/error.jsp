<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Error - Khel Sports App</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f8f9fa;
            margin: 0;
            padding: 40px;
            text-align: center;
        }
        .error-container {
            max-width: 500px;
            margin: 0 auto;
            background: white;
            padding: 40px;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        .error-code {
            font-size: 72px;
            font-weight: bold;
            color: #dc3545;
            margin: 0;
        }
        .error-message {
            font-size: 24px;
            color: #6c757d;
            margin: 20px 0;
        }
        .error-description {
            color: #6c757d;
            margin: 20px 0;
            line-height: 1.6;
        }
        .btn {
            background: #007bff;
            color: white;
            padding: 12px 24px;
            text-decoration: none;
            border-radius: 4px;
            display: inline-block;
            margin-top: 20px;
        }
        .btn:hover {
            background: #0056b3;
        }
    </style>
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