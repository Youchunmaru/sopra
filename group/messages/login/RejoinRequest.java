package messages.login;

import enums.MessageType;
import messages.Message;

public class RejoinRequest extends Message {

    public String clientSecret;

    public RejoinRequest(String version, String clientSecret){
        this.type = MessageType.REJOIN;
        this.version = version;
        this.clientSecret=clientSecret;
    }
}
