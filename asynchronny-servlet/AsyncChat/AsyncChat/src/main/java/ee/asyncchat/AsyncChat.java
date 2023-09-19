package ee.asyncchat;

import java.io.*;
import java.util.ArrayList;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

class WaitForMessage implements AsyncListener
{
    AsyncContext ac;

    public WaitForMessage(AsyncContext ac)
    {
        this.ac = ac;
    }

    public void newMessage(String nickname, String message, String sessionId)
    {
        try {
            ServletResponse servletResponse = ac.getResponse();
            HttpServletRequest servletRequest = (HttpServletRequest) ac.getRequest();

            if(servletResponse == null || servletRequest == null){
                return;
            }
            if(message != null && sessionId != null && !sessionId.equals(servletRequest.getSession().getId())){
                servletResponse.setContentType("text/html;charset=UTF-8");
                PrintWriter out = servletResponse.getWriter();
                out.println(nickname + ": " + message);
                ac.complete();
            }

        } catch (Exception e) {}
    }

    private void removeMeFromQueue()
    {
        ArrayList<WaitForMessage> waiting = (ArrayList<WaitForMessage>) ac.getRequest().getServletContext().getAttribute("waiting");
        synchronized(waiting)
        {
            waiting.remove(this);
        }
    }

    @Override
    public void onComplete(AsyncEvent event) throws IOException {
        removeMeFromQueue();
    }

    @Override
    public void onTimeout(AsyncEvent event) throws IOException {
        newMessage("", null, null);
    }

    @Override
    public void onError(AsyncEvent event) throws IOException {
        removeMeFromQueue();
    }

    @Override
    public void onStartAsync(AsyncEvent event) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

@WebServlet(name = "AsyncChat", value = "/asyncchat", asyncSupported = true)
public class AsyncChat extends HttpServlet {
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        AsyncContext ac = request.startAsync();
        ArrayList<WaitForMessage> waiting = (ArrayList<WaitForMessage>) request.getServletContext().getAttribute("waiting");

        if (waiting == null)
        {
            waiting = new ArrayList<WaitForMessage>();
            request.getServletContext().setAttribute("waiting", waiting);
        }
        WaitForMessage waiter = new WaitForMessage(ac);
        ac.addListener(waiter);
        waiting.add(waiter);

        response.setContentType("text/xml");
        response.setHeader("Cache-Control", "private, no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
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
        return "AsyncChat async servlet";
    }

}