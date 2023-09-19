package com.example.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import message.Message;
import user.User;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "Messenger", value = "/Messenger")
public class Messenger extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //response.setContentType("text/html;charset=UTF-8");
        if(request.getSession(false) == null){
            response.sendRedirect("index.html");
            return;
        }

        String idOfSession = request.getSession().getId();
        HashMap<String, User> clients = (HashMap<String, User>) getServletContext().getAttribute("clients");
        User user = clients.get(idOfSession);

        if(request.getParameter("odhlas") != null){
            clients.remove(idOfSession);
            getServletContext().setAttribute("client", clients);
            response.sendRedirect("index.html");
            return;
        }

        if(request.getParameter("clear_vsetko") != null){
            HashMap<Integer, User> allClients = (HashMap<Integer, User>) getServletContext().getAttribute("clients");
            for(User u: allClients.values()){
                u.indexOfMessages = 0;
            }
            getServletContext().setAttribute("messages", new ArrayList<String>());
            response.sendRedirect("Messenger");
            return;
        }

        if(request.getParameter("clear_lokalne") != null){
            ArrayList<String> messages = (ArrayList<String>) getServletContext().getAttribute("messages");
            user.indexOfMessages = messages.size();
            response.sendRedirect("Messenger");
            return;
        }

        if(request.getParameter("message") != null){
            Message newMessage = new Message(user.name, request.getParameter("message"));
            addNewMessage(newMessage);
            response.sendRedirect("Messenger");
            return;
        }

        String tittle = "SERVLET CHAT";

        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>" + tittle + "</title>");
        out.println("<meta charset=\"UTF-8\">");
        out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>" + tittle + "</h1>");
        out.println("<h2>Aktivni pouzivatelia:</h2>");
        writeActiveClients(out, idOfSession);

        out.println("<form id = \"temp\" method=\"GET\" action=\"Messenger\">");
        out.println("<input type=\"submit\" value=\"Odhlas\" name=\"odhlas\" id=\"odhlas\">");
        out.println("</form><br>");

        out.println("<form id = \"temp\" method=\"GET\" action=\"Messenger\">");
        out.println("<input type=\"submit\" value=\"Clear vsetko\" name=\"clear_vsetko\" id=\"clear_vsetko\">");
        out.println("</form><br>");

        out.println("<form id = \"temp\" method=\"GET\" action=\"Messenger\">");
        out.println("<input type=\"submit\" value=\"Clear lokalne\" name=\"clear_lokalne\" id=\"clear_lokalne\">");
        out.println("</form><br>");

        out.println("<h2>Spravy:</h2>");
        writeAllMessages(out, user);

        out.println("<form id = \"formMessage\" method=\"GET\" action=\"Messenger\">");
        out.println("<input id=\"message\" type=\"text\" name=\"message\" size=\"100\" required>");
        out.println("<input type=\"submit\" value=\"Posli spravu\" name=\"posli\" id=\"posli\">");
        out.println("</form><br>");

        out.println("</body>");
        out.println("</html>");

    }

    void addNewMessage(Message newMessage){
        ServletContext servletContext = getServletContext();
        if(servletContext.getAttribute("messages") == null){
            servletContext.setAttribute("messages", new ArrayList<Message>());
        }
        ArrayList<Message> messages = (ArrayList<Message>) servletContext.getAttribute("messages");
        messages.add(newMessage);
        servletContext.setAttribute("messages", messages);
    }


    void writeActiveClients(PrintWriter out, String sessionID){
        ServletContext servletContext = getServletContext();
        HashMap<String, User> clients = (HashMap<String, User>) servletContext.getAttribute("clients");
        if(clients == null){
            return;
        }

        for (Map.Entry<String, User> entry : clients.entrySet()) {
            String key = entry.getKey();
            User value = entry.getValue();
            if(HttpSessionCollector.contain(key)) {
                if (key.equals(sessionID)) {
                    out.println("<p><b>" + value.name + "</b></p>");
                } else {
                    out.println("<p>" + value.name + "</p>");
                }
            }
        }

    }

    void writeAllMessages(PrintWriter out, User user){
        ServletContext servletContext = getServletContext();
        ArrayList<Message> messages = (ArrayList<Message>) servletContext.getAttribute("messages");
        if(messages == null){
            return;
        }

        int startMessagesFrom = user.indexOfMessages;
        for(int i = startMessagesFrom; i < messages.size(); i++){
            out.println("<p><b>"+messages.get(i).userName + ":</b> " + messages.get(i).message + "</p>");
        }

    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
