package messages.gameplay.outgoing;

import enums.MessageType;
import messages.Message;

public class SandwormDespawnMessage extends Message {



    public SandwormDespawnMessage(String version) {
        this.type = MessageType.SANDWORM_DESPAWN_DEMAND;
        this.version=version;
    }
}