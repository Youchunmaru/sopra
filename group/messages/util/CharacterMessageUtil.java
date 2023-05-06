package messages.util;

import enums.UnitType;

public class CharacterMessageUtil {
    public String characterName;
    public UnitType characterClass;

    public CharacterMessageUtil(String characterName, UnitType characterClass) {
        this.characterName = characterName;
        this.characterClass = characterClass;
    }
    @Override
    public String toString(){
        return "{ " + characterName + ", " + characterClass.toString() + " }";
    }
}
