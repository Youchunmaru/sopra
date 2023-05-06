package messages.gameplay.outgoing;


import messages.Message;
import enums.MessageType;

import java.util.ArrayList;


public class GameStateMessage extends Message {
    public final int clientID;
    public final int[] activelyPlayingIDs;
    public final String[] history;


    public GameStateMessage(String version, int clientID, int[] activelyPlayingIDs, String[] history) {
        this.type = MessageType.GAMESTATE;
        this.version=version;
        this.clientID = clientID;
        this.activelyPlayingIDs = activelyPlayingIDs;
        this.history = history;
    }
}
