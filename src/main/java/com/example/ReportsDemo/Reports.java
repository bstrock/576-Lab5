package com.example.ReportsDemo;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

import java.sql.*;
import java.util.Enumeration;
import java.util.logging.*;

import org.json.JSONException;
import org.json.JSONObject;

@WebServlet(name="Reports", urlPatterns = "/Reports")
public class Reports extends HttpServlet {

// use any string you want
    private static final long serialVersionUID = 1L;
    public Reports() {
        super();
    }

    @Override
    public void init() throws ServletException {
    ServletContext context = getServletContext();
    Logger logger = Logger.getLogger("Reports");
    logger.log(Level.INFO, "LOG INIT");
    log("LOG LOG INIT");
    context.log("CONTEXT INIT");
    System.out.println(("SYS INIT"));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Logger logger = Logger.getLogger("Reports");
        logger.log(Level.INFO, "LOG LOG GET");

        ServletContext context = getServletContext();
        context.log("CONTEXT LOG GET");

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        ServletContext context = getServletContext();
        context.log("PING");

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        String tab_id = request.getParameter("tab_id");
        int type = Integer.parseInt(tab_id);
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

        if (type == 0) {
            context.log("REPORT SUBMITTED");

            try {
                DBUtility.createReport(request);
                confirmSuccess(response);

            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else if (tab_id.equals("1")) {
            //queryReport(request, response);
        }
    }

    protected void confirmSuccess(HttpServletResponse response) throws IOException {
        JSONObject data = new JSONObject();
        try {
            data.put("status", "success");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        response.getWriter().write(data.toString());
    }

    public static void main(String[] args){

        System.out.println("FUCK");
    }
}


