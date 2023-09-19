package ee.asyncchat;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

@WebServlet(name = "ChatServlet", urlPatterns = {"/chatservlet"})
public class ChatServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = null;
        try {
            out = response.getWriter();
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>SERVLET - ASYNCHRONNY</title>");
            //prevent javascript to be cached
            long version = java.util.Calendar.getInstance().getTimeInMillis();
            out.println("<script type='text/javascript' src='js/chat.js?ver=" + version + "'></script>");
            out.println("</head>");
            out.println("<body onload='start()'>");
            out.println("<h1>SERVLET - ASYNCHRONNY</h1>");

            if(request.getParameter("nickname") != null){
                out.println("<h2>" +  request.getParameter("nickname") + "</h2>");
                request.getSession().setAttribute("nickname", request.getParameter("nickname"));
            }

            ArrayList<WaitForMessage> waiting = (ArrayList<WaitForMessage>) request.getServletContext().getAttribute("waiting");
            if (waiting != null)
            {
                ArrayList<WaitForMessage> copy = new ArrayList<WaitForMessage>(waiting);
                for (WaitForMessage ac: copy)
                    ac.newMessage((String) request.getSession().getAttribute("nickname"), request.getParameter("msg"), request.getSession().getId());
            }

            out.println("<div id='msgs'>");
            out.println("</div><br /><br />");
            out.println("<input type='text' name='msg' id='msg' />");

        }
        catch (Exception e)
        {
            out.println("<pre>");
            e.printStackTrace(out);
            out.println("</pre>");
        }
        finally
        {
            out.println("</body>");
            out.println("</html>");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "AsyncChat main chat servlet";
    }

}
