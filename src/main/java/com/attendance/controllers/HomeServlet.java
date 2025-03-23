package com.attendance.controllers;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Logger;

/**
 * Home servlet to handle the root URL
 */
@WebServlet(name = "HomeServlet", urlPatterns = {"", "/"})
public class HomeServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(HomeServlet.class.getName());
    private static final long serialVersionUID = 1L;
    
    /**
     * Handle GET requests
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        LOGGER.info("HomeServlet: GET request received");
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }
}