package messages.login;

import enums.MessageType;
import messages.Message;

public class JoinAcceptedMessage extends Message {
    public final String clientSecret;
    public final int clientID;

    public JoinAcceptedMessage(String version, String clientSecret, int clientID) {
        this.type = MessageType.JOINACCEPTED;
        this.version = version;
        this.clientSecret = clientSecret;
        this.clientID=clientID;
    }
}
