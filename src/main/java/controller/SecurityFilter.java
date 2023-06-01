package controller;


import action.*;
import entity.Role;
import entity.User;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Set;

public class SecurityFilter implements Filter {
    private static Logger logger = LogManager.getLogger(SecurityFilter.class);
    private static User mode = User.Default.ADMIN.getUser();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (mode.equals(User.Default.ADMIN.getUser()))
            logger.info("Admin mode");
        else
            logger.info("User mode");
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            Action action = (Action) httpRequest.getAttribute("action");
            logger.debug(action);
            Set<Role> allowRoles = action.getAllowRoles();
            String userName = "unauthorized user";
            HttpSession session = httpRequest.getSession();
            User user = null;
            logger.debug(session.getId());
            if (session != null) {
                user = (User) session.getAttribute("authorizedUser");
                logger.debug(user);
                action.setAuthorizedUser(user);
                String errorMessage = (String) session.getAttribute("SecurityFilterMessage");
                if (errorMessage != null) {
                    httpRequest.setAttribute("message", errorMessage);
                    session.removeAttribute("SecurityFilterMessage");
                }
            }
            logger.debug("AuthorizedUser: " + user);
            boolean canExecute = action.getName().equals("loginAction") || action.getName().equals("registrationAction");
            if (user != null) {
                canExecute = canExecute || action.getAuthorizedUser() != null;
            }
            logger.debug("|" + user + "|");
            logger.debug("CanExecute:" + canExecute);

            if (canExecute) {
                chain.doFilter(request, response);
            } else {
                logger.info(String.format("Trying of %s access to forbidden resource \"%s\"", userName, action.getName()));
                if (session != null && action.getClass() != MainAction.class) {
                    session.setAttribute("SecurityFilterMessage", "Доступ запрещён");
                }
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/");
            }
        } else {
            logger.error("It is impossible to use HTTP filter");
            request.getServletContext().getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(request, response);
        }
    }

    @Override
    public void destroy() {
    }
}
