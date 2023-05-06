package messages.gameplay.outgoing;

import enums.GreatHouseEnum;
import enums.MessageType;
import messages.Message;

public class HouseSelectionAckMessage extends Message {
    public final int clientID;
    public final GreatHouseEnum houseName;

    public HouseSelectionAckMessage(String version,int clientID, GreatHouseEnum houseName) {
        this.type = MessageType.HOUSE_ACKNOWLEDGEMENT;
        this.version = version;
        this.clientID = clientID;
        this.houseName = houseName;
    }
}
