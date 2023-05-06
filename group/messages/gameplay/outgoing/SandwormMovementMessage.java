package messages.gameplay.outgoing;

import enums.MessageType;
import messages.util.Point;
import messages.Message;

public class SandwormMovementMessage extends Message {

    public Point[] path;

    public SandwormMovementMessage(String version, Point[] path) {
        this.type = MessageType.SANDWORM_MOVE_DEMAND;
        this.version=version;
        this.path = path;
    }
}