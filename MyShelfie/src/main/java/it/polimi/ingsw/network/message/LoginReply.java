package it.polimi.ingsw.network.message;

/**
 * Class: LoginReply
 * This message represents the response of the Server to a Client login request
 * @author Paolo Ceffa
 */
public class LoginReply extends Message {
    private final boolean outcome; // true ok, false bad response

    /**
     * Default constructor
     * @param outcome code response status: true Server accepts the login request, false Serve rejects login request
     */
    public LoginReply(boolean outcome) {
        super(MessageCode.LOGIN_REPLY);
        this.outcome = outcome;
    }

    public boolean getOutcome() {
        return outcome;
    }
}
