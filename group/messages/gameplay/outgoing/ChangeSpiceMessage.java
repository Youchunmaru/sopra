package messages.gameplay.outgoing;

import enums.MessageType;
import messages.Message;

public class ChangeSpiceMessage extends Message {
    public final int clientID;
    public final int newSpiceValue;

    public ChangeSpiceMessage(String version, int clientID, int newSpiceValue) {
        this.type = MessageType.CHANGE_PLAYER_SPICE_DEMAND;
        this.version=version;
        this.clientID = clientID;
        this.newSpiceValue = newSpiceValue;
    }
}