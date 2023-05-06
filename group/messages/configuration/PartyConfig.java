package messages.configuration;

import messages.util.UnitConfig;

/**
 * The SessionConfig class represents the object representation of the sessionconfiguration json file.
 * Its contents describe the initial attributes of all characters and other game variables.
 * The file ending should be .party.json.
 */
public class PartyConfig {
    //Integervalues(>=0), length 6, in order: HP, MP, AP, attackdamage, inventoryspace, healingrate.
    public UnitConfig noble;
    //Integervalues(>=0), length 6, in order: HP, MP, AP, attackdamage, inventoryspace, healingrate.
    public UnitConfig mentat;
    //Integervalues(>=0), length 6, in order: HP, MP, AP, attackdamage, inventoryspace, healingrate.
    public UnitConfig beneGesserit;
    //Integervalues(>=0), length 6, in order: HP, MP, AP, attackdamage, inventoryspace, healingrate.
    public UnitConfig fighter;
    //Integer(>=1), number of rounds until overlength.
    public int numbOfRounds;
    //Int(>=0), amount of allotted time for character actions.
    public int actionTimeUserClient;
    //Int(>=0), amount of allotted time for AI actions.
    public int actionTimeAIClient;
    //Float(>=0), bonus from high to low combat.
    public float highGroundBonusRatio;
    //Float(>=0), malus from low to high combat.
    public float lowGroundMalusRatio;
    //Float(0<=x<=1), chance for a successful kanley attack.
    public float kanlySuccessProbability;
    //Integer(>=0), minimum spice amount before a spice blow can occur
    public int spiceMinimum;
    //String(Regex B[0-8]*/S[0-8]*"), string with the rule after which dunes travel
    public String cellularAutomaton;
    //Float(0<=x<=1), chance to crash the Ornithopter
    public float crashProbability;
    //Integer(>=0), number of units in fields a sandworm can travel per round
    public int sandWormSpeed;
    //Integer(>=0), distance on spawn from sandworm to its Target
    public int sandWormSpawnDistance;
    //Float(0<=x<=1), chance to clone any unit on regular death
    public float cloneProbability;
    //Integer(>=0), miminum Duration of Time to elapse before a different player can unpause
    public int minPauseTime;
    //Integer(>=0), Number of strikes allowed
    public int maxStrikes;


    public PartyConfig(UnitConfig noble, UnitConfig mentat, UnitConfig beneGesserit, UnitConfig fighter, int numbOfRounds,
                       int actionTimeUserClient, int actionTimeAIClient, float highGroundBonusRatio,
                       float lowGroundMalusRatio, float kanlySuccessProbability, int spiceMinimum,
                       String cellularAutomaton, float crashProbability, int sandWormSpeed, int sandWormSpawnDistance,
                       float cloneProbability, int minPauseTime, int maxStrikes) {
        this.noble = noble;
        this.mentat = mentat;
        this.beneGesserit = beneGesserit;
        this.fighter = fighter;
        this.numbOfRounds = numbOfRounds;
        this.actionTimeUserClient = actionTimeUserClient;
        this.actionTimeAIClient = actionTimeAIClient;
        this.highGroundBonusRatio = highGroundBonusRatio;
        this.lowGroundMalusRatio = lowGroundMalusRatio;
        this.kanlySuccessProbability = kanlySuccessProbability;
        this.spiceMinimum = spiceMinimum;
        this.cellularAutomaton = cellularAutomaton;
        this.crashProbability = crashProbability;
        this.sandWormSpeed = sandWormSpeed;
        this.sandWormSpawnDistance = sandWormSpawnDistance;
        this.cloneProbability = cloneProbability;
        this.minPauseTime = minPauseTime;
        this.maxStrikes = maxStrikes;
    }

  public UnitConfig getNoble() {
    return noble;
  }

  public UnitConfig getMentat() {
    return mentat;
  }

  public UnitConfig getBeneGesserit() {
    return beneGesserit;
  }

  public UnitConfig getFighter() {
    return fighter;
  }

  public int getnumbOfRounds() {
    return numbOfRounds;
  }

  public int getActionTimeUserClient() {
    return actionTimeUserClient;
  }

  public int getActionTimeAIClient() {
    return actionTimeAIClient;
  }

  public float getHighGroundBonusRatio() {
    return highGroundBonusRatio;
  }

  public float getLowGroundMalusRatio() {
    return lowGroundMalusRatio;
  }

  public float getKanlySuccessProbability() {
    return kanlySuccessProbability;
  }

  public int getSpiceMinimum() {
    return spiceMinimum;
  }

  public String getCellularAutomaton() {
    return cellularAutomaton;
  }

  public float getCrashProbability() {
    return crashProbability;
  }

  public int getSandWormSpeed() {
    return sandWormSpeed;
  }

  public int getSandWormSpawnDistance() {
    return sandWormSpawnDistance;
  }

  public float getCloneProbability() {
    return cloneProbability;
  }

  public int getMinPauseTime() {
    return minPauseTime;
  }

  public int getMaxStrikes() {
    return maxStrikes;
  }
}
