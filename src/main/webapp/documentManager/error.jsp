<%--
    Simple error page for document management errors
--%>
<!DOCTYPE html>
<html>
<head>
    <title>Document Error</title>
    <link href="${pageContext.request.contextPath}/library/bootstrap/3.0.0/css/bootstrap.css" rel="stylesheet" type="text/css"/>
    <style>
        body { padding: 20px; }
        .error-container { max-width: 600px; margin: 0 auto; }
    </style>
</head>
<body>
    <div class="error-container">
        <div class="alert alert-danger">
            <h3><span class="glyphicon glyphicon-exclamation-sign"></span> Document Error</h3>
            <p>
                <%
                String errorMessage = (String) request.getAttribute("errorMessage");
                if (errorMessage != null) {
                    out.print(errorMessage);
                } else {
                    out.print("An error occurred while processing the document. The document may not exist or there was a problem with the file upload.");
                }
                %>
            </p>

            <div class="btn-group">
                <button type="button" class="btn btn-default" onclick="window.close()">Close Window</button>
            </div>
        </div>
    </div>
</body>
</html>