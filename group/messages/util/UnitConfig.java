package messages.util;

public class UnitConfig {
    private float maxHP;
    private int maxMP;
    private int maxAP;
    private float damage;
    private int inventorySize;
    private float healingHP;

    public UnitConfig(float maxHP, int maxMP, int maxAP, float damage, int inventorySize, float healingHP) {
        this.maxHP = maxHP;
        this.maxMP = maxMP;
        this.maxAP = maxAP;
        this.damage = damage;
        this.inventorySize = inventorySize;
        this.healingHP = healingHP;
    }

  public float getMaxHP() {
    return maxHP;
  }

  public int getMaxMP() {
    return maxMP;
  }

  public int getMaxAP() {
    return maxAP;
  }

  public float getDamage() {
    return damage;
  }

  public int getInventorySize() {
    return inventorySize;
  }

  public float getHealingHP() {
    return healingHP;
  }
}
