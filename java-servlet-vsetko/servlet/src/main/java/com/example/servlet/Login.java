package com.example.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import user.User;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

@WebServlet(name = "Login", value = "/Login")
public class Login extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User newUser = new User(request.getParameter("name"));
        addNewUser(newUser, request);
        response.sendRedirect("Messenger");
    }

    void addNewUser(User newUser, HttpServletRequest request){
        ServletContext servletContext = getServletContext();
        if(servletContext.getAttribute("clients") == null){
            servletContext.setAttribute("clients", new HashMap<String,User>());
        }
        HashMap<String, User> clients = (HashMap<String, User>) servletContext.getAttribute("clients");
        clients.put(request.getSession().getId(),newUser);
        servletContext.setAttribute("clients", clients);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
