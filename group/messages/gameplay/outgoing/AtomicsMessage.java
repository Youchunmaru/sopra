package messages.gameplay.outgoing;

import enums.MessageType;
import messages.Message;

public class AtomicsMessage extends Message {
    public int clientID;
    public boolean shunned;
    public int atomicsLeft;

    public AtomicsMessage(String version, int clientID, boolean shunned, int atomicsLeft) {
        this.type = MessageType.ATOMICS_UPDATE_DEMAND;
        this.version=version;
        this.clientID = clientID;
        this.shunned = shunned;
        this.atomicsLeft = atomicsLeft;
    }
}
