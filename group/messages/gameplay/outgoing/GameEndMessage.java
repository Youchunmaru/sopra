package messages.gameplay.outgoing;

import enums.MessageType;
import messages.Message;
import network.util.Statistics;

public class GameEndMessage extends Message {
    public final int winnerID;
    public final int loserID;
    public final Statistics[] statistics;

    public GameEndMessage(String version, int winnerID, int loserID, Statistics[] statistics) {
        this.type = MessageType.GAME_END;
        this.version=version;
        this.winnerID = winnerID;
        this.loserID = loserID;
        this.statistics = statistics;
    }
}
