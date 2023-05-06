package io.swapastack.dunetd.logic.tower;

import static io.swapastack.dunetd.Config.config;

import io.swapastack.dunetd.enums.AssetsEnum;
import io.swapastack.dunetd.logic.entity.Entity;
import io.swapastack.dunetd.logic.entity.EntityDecorator;
/**
 * A type of {@link Tower}.
 * Does no dmg, but slows the enemy's.
 *
 * @author Samuel Gröner
 * */
public class SonicTower extends EntityDecorator {

  protected static float DMG;
  protected static float SPICE;
  protected static float SPEED;
  public static final AssetsEnum TYPE = AssetsEnum.SONIC_TOWER;

  public SonicTower(Entity entity) {
    super(entity);
    SPEED = config.getSonicTower().get("speed");
    SPICE = config.getSonicTower().get("spice");
    DMG = config.getSonicTower().get("dmg");
  }
  @Override
  public float getDMG() {
    return DMG;
  }
  @Override
  public float getSPICE() {
    return SPICE;
  }
  @Override
  public float getSPEED() {
    return SPEED;
  }
  @Override
  public AssetsEnum getTYPE(){
    return TYPE;
  }
}
