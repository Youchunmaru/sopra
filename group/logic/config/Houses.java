package logic.config;

import enums.GreatHouseEnum;
import enums.PlayerEnum;
import logic.game.GameInstance;
import logic.game.entity.unit.*;
import logic.util.MathUtil;
import messages.util.CharacterMessageUtil;
import messages.util.HouseConfig;
import network.util.Player;
import java.util.*;

/**
 * Holds the information of all houses.
 *
 * @author Samuel Gröner, Julian Korinth
 */
public class Houses {
    private final List<HouseConfig> houseList;
    private final Map<GreatHouseEnum, List<CharacterMessageUtil>> houseToCharactersList = new EnumMap<>(GreatHouseEnum.class);

    /**
     * Constructor.
     *
     * @param houses a list of all houses
     */
    public Houses(List<HouseConfig> houses) {
        this.houseList = houses;
        translateListToMap();
    }

    private void translateListToMap() {
        for (HouseConfig house : houseList) {
            houseToCharactersList.put(house.houseName, Arrays.asList(house.houseCharacters.clone()));
        }
    }

    /**
     *
     * @param aggressorUnit
     * @param gameInstance
     * @return
     */
    public List<Unit> getAtomicsCompensationCharacters(Unit aggressorUnit, GameInstance gameInstance) {
        PlayerEnum aggressorPlayer = aggressorUnit.getPlayer();
        List<Player> aggressorAndVictim= gameInstance.getPlayers(aggressorPlayer);
        PlayerEnum victimPlayer = aggressorAndVictim.get(1).getPlayerEnum();
        GreatHouseEnum aggressor = aggressorAndVictim.get(0).getGreatHouse();
        GreatHouseEnum victim = aggressorAndVictim.get(1).getGreatHouse();
        List<CharacterMessageUtil> characters = new LinkedList<>();
        GameInstance.LOGGER.info("Houses(getAtomicsCompensationCharacters): victim: " + victimPlayer + ", aggressor: " +aggressorPlayer);
        GameInstance.LOGGER.info("Houses(getAtomicsCompensationCharacters): Removed participating. Remaining Houses: " +houseToCharactersList.toString());
        Map<Integer,Integer> integerToIntergerMap = new HashMap<>();
        int counter = 0;
        for (Map.Entry<GreatHouseEnum,List<CharacterMessageUtil>> characterList : houseToCharactersList.entrySet()) {
            if (!characterList.getValue().isEmpty()&&characterList.getKey()!=aggressor&&characterList.getKey()!=victim) {
                int nextRandom = MathUtil.random.nextInt(characterList.getValue().size());
                integerToIntergerMap.put(counter,nextRandom);
                GameInstance.LOGGER.info("Houses(getAtomicsCompensationCharacters): Choosing a random Unit");
                characters.add(characterList.getValue().get(nextRandom));
                }
            counter ++;
            }
            for(Map.Entry<Integer,Integer> keyValue : integerToIntergerMap.entrySet()){
                GameInstance.LOGGER.info("Houses(getAtomicsCompensationCharacters): trying to remove Unit at " + keyValue.getKey() + ", " + keyValue.getValue()
                +"\n" + GreatHouseEnum.values().clone()[keyValue.getKey()]);
                List<CharacterMessageUtil> characterListCopy =  new LinkedList<>();
                GameInstance.LOGGER.info("Houses(getAtomicsCompensationCharacters): Trying to remove from copy");
                for(int i=0; i<houseToCharactersList.get(GreatHouseEnum.values().clone()[keyValue.getKey()]).size(); i++) {
                    if((int)keyValue.getValue() != i){
                        characterListCopy.add(houseToCharactersList.get(GreatHouseEnum.values().clone()[keyValue.getKey()]).get(i));
                    }
                }
                houseToCharactersList.put(GreatHouseEnum.values().clone()[keyValue.getKey()], characterListCopy);
                GameInstance.LOGGER.info("Houses(getAtomicsCompensationCharacters): new characterlist at"+ GreatHouseEnum.values().clone()[keyValue.getKey()] + ", is " + houseToCharactersList.get(GreatHouseEnum.values().clone()[keyValue.getKey()]).toString());
            }
        GameInstance.LOGGER.info("Houses(getAtomicsCompensationCharacters): chosen characters are: " + characters);
        List<Unit> compensationCharacters = new LinkedList<>();
        for (CharacterMessageUtil character : characters) {
            switch (character.characterClass) {
                case NOBLE:
                    compensationCharacters.add(new Noble(character.characterName, victimPlayer, victim, gameInstance.generateCharacterID(), gameInstance));
                    break;
                case BENE_GESSERIT:
                    compensationCharacters.add(new BeneGesserit(character.characterName, victimPlayer, victim, gameInstance.generateCharacterID(), gameInstance));
                    break;
                case FIGHTER:
                    compensationCharacters.add(new Fighter(character.characterName, victimPlayer, victim, gameInstance.generateCharacterID(), gameInstance));
                    break;
                case MENTAT:
                    compensationCharacters.add(new Mentat(character.characterName, victimPlayer, victim, gameInstance.generateCharacterID(), gameInstance));
                    break;
            }
        }
        return compensationCharacters;
    }

    @Override
    public String toString() {
        String returnString = "";
        for (HouseConfig house : houseList) {
            returnString += house.toString() + "\n";
        }
        return returnString;
    }
}
