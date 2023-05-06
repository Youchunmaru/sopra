package messages.gameplay.outgoing;

import enums.ChangeReason;
import enums.MessageType;
import messages.util.Point;
import messages.util.Tile;
import messages.Message;


public class MapChangeMessage extends Message {
    public ChangeReason changeReason;
    public Tile[][] newMap;
    public final Point stormEye;


    public MapChangeMessage(String version, ChangeReason changeReason, Tile[][] newMap, Point stormEye) {
        this.type = MessageType.MAP_CHANGE_DEMAND;
        this.version=version;
        this.changeReason = changeReason;
        this.newMap = newMap;
        this.stormEye = stormEye;
    }
}