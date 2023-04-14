package it.polimi.ingsw.network.message;

/**
 * Class: LoginRequest
 * This message represents the login request by a Client
 * @author Paolo Ceffa
 *
 */
public class LoginRequest extends Message {

    public LoginRequest(String username) {
        super(username, MessageCode.LOGIN_REQUEST);
    }
}
