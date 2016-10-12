package fr.mines.event_manager.global;

import fr.chardan.jee.servlet.router.Servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class BaseServlet extends Servlet {
    protected void render(String jspPage, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("jspPage", jspPage);
        getServletContext().getRequestDispatcher("/WEB-INF/jsp/index.jsp").forward(req, resp);
    }
}
