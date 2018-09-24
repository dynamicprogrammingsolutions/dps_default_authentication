package dps.authentication;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Date;
import java.util.Random;

@Dependent
public class AuthenticationManagerImplementation implements Serializable, AuthenticationManager {

    private static final long serialVersionUID = -4548303728129343390L;

    @Inject
    UserDataProvider userDataProvider;
    @Inject
    LoginDataProvider loginDataProvider;

    private Long loginTimeOut = Long.valueOf(86400000);
    private Long tokenTimeOut = Long.valueOf(3600000);

    private LoginData loginData;
    private LoginStatus status = LoginStatus.NOT_LOGGED_IN;

    enum LoginStatus {
        NOT_LOGGED_IN,
        LOGGED_IN
    }

    /*AuthenticationManagerImplementation()
    {
        userDataProvider = CDI.current().select(UserDataProvider.class).get();
        loginDataProvider = CDI.current().select(LoginDataProvider.class).get();
    }*/

    void init()
    {
        /*userDataProvider = CDI.current().select(UserDataProvider.class).get();
        loginDataProvider = CDI.current().select(LoginDataProvider.class).get();*/
    }

    @Override
    public Boolean login(String username, String password) {

        init();
        //System.out.println("trying login: "+username+" "+password);
        AuthenticableUser user = userDataProvider.getUserByCredentials(username,password);
        if (user != null) {
            String token = generateToken(user);

            LoginData loginData = loginDataProvider.addToken(token,token,user);

            this.loginData = loginData;
            status = LoginStatus.LOGGED_IN;
            return true;
        } else {
            //System.out.println("no user");
            return false;
        }
    }

    @Override
    public Boolean login(String token)
    {
        init();

        String key = token; // extract key from token

        LoginData loginData = loginDataProvider.getLoginData(key);

        verifyToken(token, loginData);

        if (loginData != null && verifyLoginData(loginData) && checkTimeout(loginData)) {
            this.loginData = loginData;
            status = LoginStatus.LOGGED_IN;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public AuthenticableUser getUser()
    {
        return loginData != null ? loginData.getUser() : null;
    }

    @Override
    public String getToken() {
        return loginData != null ? loginData.getToken() : null;
    }

    @Override
    public void logout() {
        status = LoginStatus.NOT_LOGGED_IN;
    }

    @Override
    public Boolean isAuthenticated() {
        if (status == LoginStatus.LOGGED_IN) {
            //System.out.println("logged in");
            if (!checkTimeout(loginData)) return false;
            return true;
        } else {
            //System.out.println("not logged in: "+status);
            return false;
        }
    }

    @Override
    public Boolean isAuthorized(String operation) {
        if (isAuthenticated()) {
            init();
            //TODO: cache for operations
            return userDataProvider.checkAuthorization(loginData.getUser(), operation);
        } else {
            return false;
        }
    }

    private String generateToken(AuthenticableUser user)
    {
        return generateRandomChars("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890",16);
    }

    private String generateRandomChars(String candidateChars, int length) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(candidateChars.charAt(random.nextInt(candidateChars
                    .length())));
        }

        return sb.toString();
    }

    private Boolean verifyLoginData(LoginData loginData)
    {
        return true;
    }

    private Boolean verifyToken(String token, LoginData loginData)
    {
        return true;
    }

    private Boolean checkTimeout(LoginData loginData)
    {
        if (loginData.getLoginTime().getTime() < new Date().getTime()-loginTimeOut) {
            //System.out.println("login timeout");
            return false;
        }
        if (loginData.getTokenIssueTime().getTime() < new Date().getTime()-tokenTimeOut) {
            //System.out.println("token timeout");
            return false;
        }
        return true;
    }



}
