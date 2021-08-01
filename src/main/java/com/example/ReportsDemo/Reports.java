package com.example.ReportsDemo;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

import java.sql.*;

import org.json.JSONException;
import org.json.JSONObject;

@WebServlet(name = "Reports", urlPatterns = "/Reports")
public class Reports extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public Reports() {
        super();
    }

    @Override
    public void init() throws ServletException {
        ServletContext context = getServletContext();
        context.log("SERVLET INIT");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletContext context = getServletContext();
        context.log("GET REQUEST RECEIVED");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        ServletContext context = getServletContext();
        context.log("POST REQUEST RECEIVED");

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        String tab_id = request.getParameter("tab_id");
        int type = Integer.parseInt(tab_id);

        /* TEST CODE BLOCK
            UNCOMMENT FOR ADDTIONAL LOGGING
        Enumeration<String> params = request.getParameterNames();

        if (params.hasMoreElements()){
            while (params.hasMoreElements()){
            String param = params.nextElement();
            context.log(param);
            }
        } else {
            context.log("NO PARAMETERS IN REQUEST");
        }

        System.out.println("tab_id: " + tab_id);
        END TEST CODE BLOCK*/

        if (type == 0) {
            context.log("REPORT SUBMITTED");
            try {
                context.log("ATTEMPTING TO SERVICE REQUEST");
                DBUtility.createReport(request);
                confirmSuccess(response);
                context.log("ATTEMPTING TO SERVICE REQUEST SUCCESSFUL");
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
                context.log("REQUEST SERVICE FAILED");
            }
        } else if (type == 1) {
            context.log("QUERY SUBMITTED");
            try {
                DBUtility.queryReport(request, response);
            } catch (SQLException | ClassNotFoundException | JSONException e) {
                e.printStackTrace();
            }
        }else if (type == 2) {
            context.log("QUERY ALL SUBMITTED");
            try {
                DBUtility.queryAllReports(request, response);  // TODO: MAKE THIS, WRAP IN TRY/CATCH
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }
        }
    }

    protected void confirmSuccess(HttpServletResponse response) throws IOException {
        // this can be extended to also provide json parameters back to the response object
        // right now it's just a status confirmation
        JSONObject data = new JSONObject();
        try {
            data.put("status", "success");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        response.getWriter().write(data.toString());
    }

    public static void main(String[] args) {
        // nada
    }
} // we hope you have enjoyed this endpoint
