package messages.gameplay.incoming;

import enums.GreatHouseEnum;
import enums.MessageType;
import messages.Message;

public class HouseSelectionRequest extends Message {
    public final GreatHouseEnum houseName;

    public HouseSelectionRequest(String version, GreatHouseEnum houseName) {
        this.type = MessageType.HOUSE_REQUEST;
        this.version = version;
        this.houseName = houseName;
    }
}
