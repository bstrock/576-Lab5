<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<head>
    <meta http-equiv="Content-Type" content="text/html">
    <title>Web Project</title>
    <script
            src="https://code.jquery.com/jquery-3.6.0.min.js"
            integrity="sha256-/xUj+3OJU5yExlq6GSYGSHk7tPXikynS7ogEvDej/m4="
            crossorigin="anonymous"></script>
    <script
            src="https://code.jquery.com/jquery-migrate-3.3.2.min.js"
            integrity="sha256-Ap4KLoCf1rXb52q+i3p0k2vjBsmownyBTE1EqlRiMwA="
            crossorigin="anonymous"></script>
</head>
<html>
<body>
<script>

    $(document).ready(function() {
        const params = {
            tab_id: "0",
            first_name: "Jason",
            last_name: "Zhou",
            gender: "wolfkin",
            email: "jasonzhou@gmail.com",
            age: "30",
            telephone: "928-777-8856",
            blood_type: "AB",
            message: "request rescue!!!",
            longitude: "-87",
            latitude: "33",
            report_type: "request",
            disaster_type: "wildfire"
        }

        $.ajax({
            url: "Reports",
            data: params,
            type: "POST"
        })
        // $.get("Reports")
    });

</script>
fuck this
</body>
</html>