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
            email: "jasonzhou3@gmail.com",
            age: "30",
            telephone: "928-777-8856",
            blood_type: "AB",
            longitude: "-87",
            latitude: "33",
            message: "request rescue!!!",
            report_type: "REQUEST",
            disaster_type: "WILDFIRE",
            resource_type: "RESCUE/VOLUNTEER",
            c_first_name: "Emergency",
            c_last_name: "Contact",
            c_email: "emergency3@contact.com",
            c_telephone: "555-555-1234"
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