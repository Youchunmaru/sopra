package messages.gameplay.outgoing;

import enums.MessageType;
import messages.util.HouseConfig;
import messages.Message;

public class HouseOfferMessage extends Message {
    public final int clientID;
    public final HouseConfig[] houses;


    public HouseOfferMessage(String version,int clientID, HouseConfig[] houses) {
        this.type = MessageType.HOUSE_OFFER;
        this.version = version;
        this.clientID=clientID;
        this.houses = houses;
    }
}
