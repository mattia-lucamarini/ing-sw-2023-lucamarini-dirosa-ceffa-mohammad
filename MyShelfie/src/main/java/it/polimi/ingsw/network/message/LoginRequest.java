package it.polimi.ingsw.network.message;

/**
 * Class: LoginRequest
 * This message represents the login request by a Client
 * @author Paolo Ceffa
 *
 */
public class LoginRequest extends Message {

    private final String username;
    public LoginRequest(String username) {
        super(MessageCode.LOGIN_REQUEST);
        this.username = username;
    }
    public String getUsername() {
        return username;
    }
}
