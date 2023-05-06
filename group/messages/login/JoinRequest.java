package messages.login;

import enums.MessageType;
import messages.Message;

public class JoinRequest extends Message {
    public final String clientName;
    public final boolean isActive;
    public final boolean isCpu;

    public JoinRequest(String version, String clientName, boolean isActive, boolean isCpu) {
        this.type = MessageType.JOIN;
        this.version = version;
        this.clientName = clientName;
        this.isActive = isActive;
        this.isCpu = isCpu;
    }
}
