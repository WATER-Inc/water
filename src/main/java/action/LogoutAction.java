package action;

import dao.PersistException;
import entity.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Logger;

public class LogoutAction extends AuthorizedUserAction {
    private static Logger logger = Logger.getLogger(String.valueOf(LogoutAction.class));

    @Override
    public Action.Forward exec(HttpServletRequest request, HttpServletResponse response) throws PersistException {
        User user = getAuthorizedUser();
        logger.info(String.format("user \"%s\" is logged out", user.getUsername()));
        request.getSession(false).invalidate();
        return new Forward("/login.html");
    }
}

