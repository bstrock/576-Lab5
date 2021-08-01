<%--
  Created by IntelliJ IDEA.
  User: brianstrock
  Date: 7/28/21
  Time: 10:53 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Web Project</title>

    <!-- jQuery -->
    <script src="//code.jquery.com/jquery-1.11.3.min.js"></script>
    <script src="//code.jquery.com/jquery-migrate-1.2.1.min.js"></script>

    <!-- Bootstrap -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
    <link rel="stylesheet" href="./css/style.css">

    <!-- Google Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Open+Sans:wght@300&display=swap" rel="stylesheet">

</head>

<nav class="navbar navbar-inverse navbar-fixed-top">
    <a class="navbar-brand">Disaster Management Portal</a>
</nav>

<body>

<div class="container-fluid">
    <div class="row flex-content">
        <div class="sidebar col-sm-4 col-md-4">
            <div id="tabs">
                <ul class="nav nav-tabs">
                    <li class="nav-item active">
                        <a class="nav-link" href="#create_report" data-toggle="tab" role="tab">Create Report</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav=link" href="#query_report" data-toggle="tab" role="tab">Query</a>
                    </li>
                </ul>
                <div class="tab-content" id="tabContent">
                    <div class="tab-pane highlight active" id="create_report" role="tabpanel">
                        <form id="create_report_form">
                            <div class="form-group row">
                                <div class="form-group col">
                                    <label for="first_name" class="col-sm-4 col-md-4 col-form-label px-2">First
                                        Name:&nbsp</label>
                                    <input name="first_name" placeholder="What's your first name?" id="first_name"
                                           class="col-sm-8 mb-2 form-control">
                                </div>
                                <div class="form-group col">
                                    <label for="last_name" class="col-sm-4 col-md-4 col-form-label px-2">Last
                                        Name:&nbsp</label>
                                    <input name="last_name" placeholder="What's your last name?" id="last_name"
                                           class="col-sm-8 mb-2 form-control">
                                </div>
                                <div class="form-group col">
                                    <label for="gender"
                                           class="col-sm-4 col-md-4 col-form-label px-2">Gender:&nbsp</label>
                                    <input name="gender" placeholder="How do you identify your gender?" id="gender"
                                           class="col-sm-8 mb-2 form-control">
                                </div>
                                <div class="form-group col">
                                    <label for="email" class="col-sm-4 col-form-label px-2">Email:&nbsp</label>
                                    <input name="email" placeholder="What's your email address?" id="email"
                                           class="col-sm-8 mb-2 form-control">
                                </div>
                                <div class="form-group col">
                                    <label for="age" class="col-sm-4 col-form-label px-2">Age:&nbsp</label>
                                    <input name="age" placeholder="How old are you?" id="age"
                                           class="col-sm-8 mb-2 form-control">
                                </div>
                                <div class="form-group col">
                                    <label for="telephone" class="col-sm-4 col-form-label px-2">Telephone:&nbsp</label>
                                    <input name="telephone" placeholder="What's your phone number?" id="telephone"
                                           class="col-sm-8 mb-2 form-control">
                                </div>
                                <div class="form-group col">
                                    <label for="blood_type" class="col-sm-4 col-form-label px-2">Blood
                                        Type:&nbsp</label>
                                    <select name="blood_type" id="blood_type" class="col-sm-auto px-2 form-control">
                                        <option value="">Select blood type</option>
                                        <option value="A">A</option>
                                        <option value="B">B</option>
                                        <option value="O">O</option>
                                        <option value="AB">AB</option>
                                        <option value="OTHER">Unknown/Other</option>
                                    </select>
                                </div>
                                <div class="form-group col">
                                    <label for="address" class="col-sm-4 col-form-label px-2">Address:&nbsp</label>
                                    <input name="address" placeholder="What's your address?" id="address"
                                           class="col-sm-8 mb-2 form-control">
                                </div>
                                <div class="form-group col">
                                    <label for="message" class="col-sm-4 col-form-label px-2">Message:&nbsp</label>
                                    <input placeholder="What additional info would you like to provide?" name="message"
                                           id="message" class="col-sm-8 mb-2 form-control">
                                </div>
                                <div class="form-group col">
                                    <label for="report_type" class="col-sm-4 col-form-label px-2">Report:&nbsp</label>
                                    <select onchange="onSelectReportType(this)" name="report_type" id="report_type"
                                            class="col-sm-auto px-2 form-control">
                                        <option value="">What is this report for?</option>
                                        <option value="DONATION">I am donating something</option>
                                        <option value="REQUEST">I want to request something</option>
                                        <option value="DAMAGE">I want to report damage</option>
                                    </select>
                                </div>
                                <div class="form-group col additional_msg_div" style="visibility: hidden">
                                    <label for="additional_message" class="additional_msg col-sm-4 col-form-label px-2">Invisible:&nbsp</label>
                                    <select name="additional_message" id="additional_message"
                                            class="additional_msg_select col-sm-auto px-2 form-control">
                                    </select>
                                </div>
                                <div class="form-group col">
                                    <label for="disaster_type"
                                           class="col-sm-4 col-form-label px-2">Disaster:&nbsp</label>
                                    <select name="disaster_type" id="disaster_type"
                                            class="col-sm-auto px-2 form-control">
                                        <option value="">Select a disaster</option>
                                        <option value="FLOOD">Flood</option>
                                        <option value="WILDFIRE">Wildfire</option>
                                        <option value="EARTHQUAKE">Earthquake</option>
                                        <option value="TORNADO">Tornado</option>
                                        <option value="HURRICANE">Hurricane</option>
                                        <option value="MATT_GAETZ">Encounter with Matt Gaetz</option>
                                        <option value="OTHER">Something else</option>
                                    </select>
                                </div>
                                <br>
                                <div class="container-fluid"><label class="col-sm-auto col-form-label px-2">OPTIONAL:
                                    Emergency
                                    Contact</label></div>
                                <br>
                                <div class="form-group col">
                                    <label for="c_first_name" class="col-sm-4 col-form-label px-2">First
                                        Name:&nbsp</label>
                                    <input placeholder="Contact's first name" name="c_first_name" id="c_first_name"
                                           class="col-sm-auto px-2 form-control"></label>
                                </div>
                                <div class="form-group col">
                                    <label for="c_last_name" class="col-sm-4 col-form-label px-2">Last
                                        Name:&nbsp</label>
                                    <input placeholder="Contact's last name" name="c_last_name" id="c_last_name"
                                           class="col-sm-auto px-2 form-control">
                                </div>
                                <div class="form-group col">
                                    <label for="c_email" class="col-sm-4 col-form-label px-2">Email:&nbsp</label>
                                    <input placeholder="Contact's email address" name="c_email" id="c_email"
                                           class="col-sm-auto px-2 form-control">
                                </div>
                                <div class="form-group col">
                                    <label for="c_telephone" class="col-sm-4 col-form-label px-2">Tel:&nbsp</label>
                                    <input placeholder="Contact's telephone" name="c_tel" id="c_telephone"
                                           class="col-sm-auto px-2 form-control">
                                </div>
                                <button type="submit" class="btn btn-default" id="report_submit_btn">
                                    <span class="glyphicon glyphicon-star"></span> Submit
                                </button>
                            </div>
                        </form>
                    </div>

                    <div class="tab-pane highlight" id="query_report" role="tabpanel">
                        <form id="query_report_form">
                            <div class="form-group row">
                                <div class="form-group col">
                                    <label for="query_report_type" class="col-sm-4 col-form-label px-2">Report
                                        Type:&nbsp</label>
                                    <select onchange="onSelectReportType(this)" name="report_type"
                                            id="query_report_type"
                                            class="col-sm-auto px-2 form-control">
                                        <option value="">Choose the report type</option>
                                        <option value="DONATION">Donation</option>
                                        <option value="REQUEST">Request</option>
                                        <option value="DAMAGE">Damage Report</option>
                                    </select>
                                </div>
                                <div class="form-group col additional_msg_div" style="visibility: hidden">
                                    <label for="query_message" class="additional_msg col-sm-4 col-form-label px-2">Invisible:&nbsp</label>
                                    <select name="additional_message" id="query_message"
                                            class="additional_msg_select col-sm-auto px-2 form-control">
                                    </select>
                                </div>
                                <div class="form-group col">
                                    <label for="disaster_type"
                                           class="col-sm-4 col-form-label px-2">Disaster:&nbsp</label>
                                    <select name="disaster_type" id="query_disaster_type"
                                            class="col-sm-auto px-2 form-control">
                                        <option value="">Choose the disaster type</option>
                                        <option value="FLOOD">flood</option>
                                        <option value="WILDFIRE">wildfire</option>
                                        <option value="EARTHQUAKE">earthquake</option>
                                        <option value="TORNADO">tornado</option>
                                        <option value="HURRICANE">hurricane</option>
                                        <option value="OTHER">other</option>
                                    </select>
                                </div>
                                <button type="submit" class="btn btn-default">
                                    <span class="glyphicon glyphicon-star"></span> Submit the query
                                </button>
                            </div>
                        </form>
                    </div>
            </div>
        </div>
    </div>
    <div id="map-canvas" class="col-xs-8"></div>
</div>

</div>
<script src="js/loadform.js"></script>

</body>
</html>