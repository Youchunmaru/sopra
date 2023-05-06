package messages.login;


import enums.MessageType;
import messages.util.CityConfig;
import messages.util.Point;
import messages.Message;
import messages.configuration.PartyConfig;

public class GameCFGMessage extends Message {
    public final String[][] scenario;
    public final PartyConfig party;
    public final CityConfig[] playerInfo;
    public final Point stormEye;

    public GameCFGMessage(String version ,String[][] scenario, PartyConfig party,
                          CityConfig[] playerInfo, Point stormEye) {
        this.type = MessageType.GAMECFG;
        this.version = version;
        this.scenario = scenario;
        this.party = party;
        this.playerInfo =playerInfo;
        this.stormEye = stormEye;

    }

}
