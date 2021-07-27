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

    $(document).ready(function () {
            // test request report
            // test emergency contact creation
            const params_1 = {
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

            // test damage report
            // test no emergency contact
            const params_2 = {
                tab_id: "0",
                first_name: "Wild",
                last_name: "Bill",
                gender: "Cowboy",
                email: "wildbill@outwest.com",
                age: "45",
                telephone: "222-333-8856",
                blood_type: "O",
                longitude: "-179.88",
                latitude: "88.77",
                message: "this disaster sucks",
                report_type: "DAMAGE",
                disaster_type: "MATT_GAETZ",
                damage_type: "POLLUTION"
            }

            // test donation report
            // test emergency contact is an existing user
            const params_3 = {
                tab_id: "0",
                first_name: "Milli",
                last_name: "Vanilli",
                gender: "Duo",
                email: "milli3@vanilli.com",
                age: "60",
                telephone: "114-654-9938",
                blood_type: "B",
                longitude: "-2.88",
                latitude: "-55.77",
                message: "oh, how the mighty fall",
                report_type: "DONATION",
                disaster_type: "HURRICANE",
                resource_type: "FOOD",
                c_first_name: "Wild",
                c_last_name: "Bill",
                c_email: "wildbill@outwest.com",
                c_telephone: "222-333-8856"
            }

            const params_4 = {
                tab_id: 1,
                disaster_type: "HURRICANE",
                report_type: "DONATION",
                resource_type: "FOOD"
            }

            const params_5 = {
                tab_id: 2
        }

            function insertTest() {
                $.ajax({
                    url: "Reports",
                    data: params_1,
                    type: "POST"
                })

                setTimeout(function () {
                    $.ajax({
                        url: "Reports",
                        data: params_2,
                        type: "POST"
                    })
                }, 3000)

                setTimeout(function () {
                    $.ajax({
                        url: "Reports",
                        data: params_3,
                        type: "POST"
                    })
                }, 6000)
            }

            function queryAllTest(wait) {
                var delay;
                wait ? delay = 10000 : delay = 0;

                setTimeout(function () {
                    $.ajax({
                        url: "Reports",
                        data: params_4,
                        type: "POST",
                        success: function(data){
                            for (let key in data) {
                                console.log(key, data[key])
                            }
                        }
                    })
                }, delay);
            }

            //insertTest();
            queryAllTest(false);
        }
    );

</script>
<h1>test executed</h1>
</body>
</html>