package fr.mines.event_manager.app.servlet;

import fr.mines.event_manager.core.http.Paths;
import fr.mines.event_manager.framework.router.http.Route;
import fr.mines.event_manager.framework.router.utils.WrappedServletAction;
import fr.mines.event_manager.core.servlet.BaseServlet;
import fr.mines.event_manager.framework.security.UserProvider;
import fr.mines.event_manager.framework.utils.Alert;
import fr.mines.event_manager.framework.validator.ValidatorProcessor;
import fr.mines.event_manager.user.entity.User;
import fr.mines.event_manager.user.manager.UserManager;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.*;

@WebServlet(name = "AppServlet", urlPatterns = {"/app/*"})
public class AppServlet extends BaseServlet {
    UserManager manager = UserManager.getInstance();

    @Override
    protected Set<Route> initGetRoutes() {
        Set<Route> routes = new HashSet<>();
        routes.add(Paths.getLogin(this::login));
        routes.add(Paths.getLogout(this::logout));
        routes.add(Paths.getSubscribe(this::subscribe));
        return routes;
    }

    @Override
    protected Set<Route> initPostRoutes() {
        Set<Route> routes = new HashSet<>();
        routes.add(Paths.postLogin(this::loginPost));
        routes.add(Paths.postSubscribe(this::subscribePost));
        return routes;
    }

    /**********
     * LOGIN
     *********/

    public void login(WrappedServletAction action) throws ServletException, IOException {
        if (UserProvider.isConnected(action.getRequest()))
        {
            this.redirect(action, "/");
            return;
        }
        String path = action.getRequest().getParameter("path");
        action.getRequest().setAttribute("PathFrom", path);
        this.render("login.jsp", action);
    }

    public void logout(WrappedServletAction action) throws ServletException, IOException {
        UserProvider.trashSession(action.getRequest());
        this.redirect(action,"/app/login",new Alert(Alert.TYPE.SUCCESS,"Déconnecté"));
    }

    protected void loginPost(WrappedServletAction action) throws IOException, ServletException {
        if (this.connect(action.getRequest())) {
            String path = "".equals(action.getRequest().getParameter("from")) ? "/event/" : action.getRequest().getParameter("from");
            this.redirect(action, path);
            return;
        }
        this.render("login.jsp", action, new Alert(Alert.TYPE.DANGER, "L'adresse mail et/ou le mot de passe ne sont pas valides"));
    }

    /**********
     * SUBSCRIBE
     *********/

    public void subscribe(WrappedServletAction action) throws ServletException, IOException {
        this.render("subscribe.jsp", action);
    }

    public void subscribePost(WrappedServletAction action) throws ServletException, IOException {
        User user = manager.create(action.getRequest());
        action.getRequest().setAttribute("user", user);

        String password2 = action.getRequest().getParameter("password2");

        if (!user.getPassword().equals(password2)) {
            render("subscribe.jsp", action, new Alert(Alert.TYPE.DANGER, "Les mots de passes sont différents."));
            return;
        }

        Map<String, String> errors = ValidatorProcessor.getInstance().isValid(user);

        if (!errors.isEmpty()) {
            this.render("subscribe.jsp", action, new Alert(Alert.TYPE.DANGER, errors));
            return;
        }

        this.redirect(action, "/app/login", new Alert(Alert.TYPE.SUCCESS, "Votre utilisateur a bien été enregistré"));
    }
}