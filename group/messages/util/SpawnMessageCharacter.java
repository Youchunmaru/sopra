package messages.util;

import enums.UnitType;

public class SpawnMessageCharacter {
    public UnitType characterType;
    public float healthMax;
    public float healthCurrent;
    public float healingHP;
    public int MPmax;
    public int MPcurrent;
    public int APmax;
    public int APcurrent;
    public float attackDamage;
    public int inventorySize;
    public int invertoryUsed;
    public boolean killedBySandworm;
    public boolean isLoud;

    public SpawnMessageCharacter(UnitType characterType, float healthMax, float healthCurrent,
                                 float healingHP, int MPmax, int MPcurrent,
                                 int APmax, int APcurrent, float attackDamage, int inventorySize,
                                 int invertoryUsed, boolean killedBySandworm, boolean isLoud) {
        this.characterType = characterType;
        this.healthMax = healthMax;
        this.healthCurrent = healthCurrent;
        this.healingHP = healingHP;
        this.MPmax = MPmax;
        this.MPcurrent = MPcurrent;
        this.APmax = APmax;
        this.APcurrent = APcurrent;
        this.attackDamage = attackDamage;
        this.inventorySize = inventorySize;
        this.invertoryUsed = invertoryUsed;
        this.killedBySandworm = killedBySandworm;
        this.isLoud = isLoud;
    }
}
