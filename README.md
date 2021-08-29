<h1>Java Servlet Example: Disaster Management System</h1>
This is a functional rewrite of a lab assignment for Geospatial Web and Mobile Programming.  The lab was designed to provide a basic overview of Java class/project structure, Servlets, Apache Tomcat, JSP, and JDBC.<br><br>

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

* Fully functional front- and back-end web app designed to allow for reporting of disasters and requests related to disaster recovery
* Front end initializes with a map of all reports currently active in the database
* Front end contains intake form to allow for user report submissions
* User receives report submission confirmation, as submission success will zoom to report location and display map pin for their report
* Front end contains a Query tab to allow filtering the underlying database
// GOT THIS FAR
* Custom-written timeline and playback controls allow for effortless exploration of data
* Playback controls allow for variations in speed, independent dragging, restarting to beginning when played from end, etc.
* Control panel on right sidebar enables contextual marker highlighting based on sample's recorded archaeological culture identification
* Era and symbol size legend callouts provide additional context to aid interperetation
* Custom Mapbox tileset used to enhance visual contrast of presentation
* Custom graphics and design elements (website background, etc.)
* Full academic attributions are included
