package messages.gameplay.incoming;

import enums.ActionEnum;
import enums.MessageType;
import messages.util.TargetConfig;
import messages.Message;

public class ActionRequest extends Message {
    public final int clientID;
    public final int characterID;
    public final ActionEnum action;
    public TargetConfig specs;

    public ActionRequest(String version, int clientID, int characterID, ActionEnum action, TargetConfig specs) {
        this.type = MessageType.ACTION_REQUEST;
        this.version = version;
        this.clientID = clientID;
        this.characterID = characterID;
        this.action = action;
        this.specs = specs;

    }
}

