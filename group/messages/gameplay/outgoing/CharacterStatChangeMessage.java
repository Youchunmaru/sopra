package messages.gameplay.outgoing;

import messages.util.UnitStatChange;
import messages.Message;
import enums.MessageType;

public class CharacterStatChangeMessage extends Message {
    public final int clientID;
    public final int characterID;
    public final UnitStatChange stats;

    public CharacterStatChangeMessage(String version, int clientID, int characterID, UnitStatChange stats) {
        this.type = MessageType.CHARACTER_STAT_CHANGE_DEMAND;
        this.version = version;
        this.clientID = clientID;
        this.characterID = characterID;
        this.stats = stats;
    }
}