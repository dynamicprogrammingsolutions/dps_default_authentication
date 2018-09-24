package dps.authentication;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import java.io.Serializable;

@Dependent
public class AuthenticationManagerFactoryImplementation implements Serializable, AuthenticationManagerFactory {

    private static final long serialVersionUID = -1739075918477460463L;

    @Inject
    Instance<AuthenticationManagerImplementation> authenticationManagerImplementations;

    @Override
    public AuthenticationManager getAuthenticationManager(HttpSession session) {
        AuthenticationManager authenticationManager = (AuthenticationManager)session.getAttribute("authenticationmanager");
        if (authenticationManager == null) {
            authenticationManager = authenticationManagerImplementations.get();
            session.setAttribute("authenticationmanager",authenticationManager);
        }
        return authenticationManager;
    }
}
