<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Error</title>
</head>
<body>
    <h1>Error ${statusCode}</h1>
    <p>${message}</p>
    <p>Request: ${requestUri}</p>
    <p><a href="${pageContext.request.contextPath}/">Back to home</a></p>
</body>
</html>
