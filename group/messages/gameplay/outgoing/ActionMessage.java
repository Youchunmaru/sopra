package messages.gameplay.outgoing;

import enums.ActionEnum;
import messages.util.TargetConfig;
import messages.Message;


import enums.MessageType;

public class ActionMessage extends Message {
    public final int clientID;
    public final int characterID;
    public final ActionEnum action;
    public TargetConfig specs;

    public ActionMessage(String version, int clientID, int characterID, ActionEnum action, TargetConfig specs) {
        this.type = MessageType.ACTION_DEMAND;
        this.version = version;
        this.clientID = clientID;
        this.characterID = characterID;
        this.action = action;
        this.specs = specs;
    }
}