package messages.util;

import enums.ColorEnum;
import enums.GreatHouseEnum;

public class HouseConfig {
    public final GreatHouseEnum houseName;
    public final ColorEnum houseColour;
    public CharacterMessageUtil[] houseCharacters;

    public HouseConfig(GreatHouseEnum houseName, ColorEnum houseColour, CharacterMessageUtil[] houseCharacters) {
        this.houseName = houseName;
        this.houseColour = houseColour;
        this.houseCharacters=houseCharacters;
    }
    @Override
    public String toString(){
        String charactersString = "";
        if(houseCharacters!=null){
            for (int i = 0; i < 6; i++) {
                if(houseCharacters[i]!=null)charactersString+= houseCharacters[i].characterName + ", " + houseCharacters[i].characterClass + "\n";
            }
        }
        return houseName.toString() + "\n"
                +houseColour.toString() +"\n"
                + charactersString;
    }
}
