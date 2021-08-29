<h1>Java Servlet Example: Disaster Management System</h1>
This is a fully functional full-stack web app designed to allow for reporting of disasters and requests related to disaster recovery, written for a class in Geospatial Web and Mobile Programming.  The lab assignment was designed to provide a basic overview of Java class/project structure, Servlets, Apache Tomcat, JSP, and JDBC.<br><br>

In rewriting the application as designed for lab, I was able to provide some functional improvements over the existing codebase by using Prepared Statements instead of raw SQL exectution, and made the codebase more programmatic by using database metadata objects to perform CRUD operations instead of relying on explicating each parameter for every database interaction.  I also improved geospatial data handling by integrating PostGIS geometry objects directly with the servlet architecture, rather than relying on SQL strings to generate PostGIS objects at the DDL level.

<h2>Tech Stack</h2>

* Javascript
* JQuery
* Leaflet.js
* Mapbox Studio
* HTML5
* CSS
* Bootstrap
* GeoJSON
* spinner.js

<h2>Project Features</h2>

**Back end:**

* Java HTTPServlet provides API interface between frontend JSP and database using JDBC
* Business logic utilizes Prepared Statements instead of raw SQL exectution
* Programmatic approach to database operations by using metadata objects to perform CRUD operations using type inference
* Fully implements Java PostGIS objects interface for geospatial database operations, rather than relying on SQL strings
* Commenting and logging throughout provides clarity and informs efficient debugging

**Front end:**

* Web interface initializes with a map of all reports currently active in the database
* Uses Google Maps API to provide basemap, POI interface, and address autocompletion
* User-submitted reports represented by map pins with callouts containing detailed report information
* Offers users a Query tab to allow filtering the underlying database
* Offers users an intake form to allow for user report submissions
* Successful submission will zoom to report location and display map pin for the report
