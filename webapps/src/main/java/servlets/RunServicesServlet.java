package servlets;

import java.io.IOException;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.biosoft.server.servlets.webservices.WebServletHandler;

@WebServlet(urlPatterns = { "/genomebrowser/*" })
public class RunServicesServlet extends HttpServlet
{
    private String msg;

    @Override public void init(ServletConfig config) throws ServletException
    {
        super.init(config);

        msg = config.getInitParameter("message");
        if( msg == null )
        {
            msg = "Default HelloAServlet response";
        }
    }

    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        WebServletHandler.handle(request, response, "GET");
        //        response.setContentType("text/plain");
        //        response.getWriter().printf("Get query %s%n", msg);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        WebServletHandler.handle(request, response, "POST");
        //        response.setContentType("text/plain");
        //        response.getWriter().printf("POST query %s%n", msg);
    }
}
