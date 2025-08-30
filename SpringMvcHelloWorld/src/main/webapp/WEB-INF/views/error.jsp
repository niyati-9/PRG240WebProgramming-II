<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page isErrorPage="true" %>
<html>
<head><title>Error</title></head>
<body>
    <h1>Error Occurred</h1>
    <p>Status Code: ${pageContext.errorData.statusCode}</p>
    <p>Request URI: ${pageContext.errorData.requestURI}</p>
    <p>Servlet Name: ${pageContext.errorData.servletName}</p>
    <p>Exception: ${pageContext.exception}</p>
    <p>Message: ${pageContext.exception.message}</p>
</body>
</html>
