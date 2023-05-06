package io.swapastack.dunetd.logic.tower;

import static io.swapastack.dunetd.Config.config;

import io.swapastack.dunetd.enums.AssetsEnum;
import io.swapastack.dunetd.logic.entity.Entity;
import io.swapastack.dunetd.logic.entity.EntityDecorator;
/**
 * A type of {@link Tower}.
 * Attacks very fast, but does almost no dmg
 *
 * @author Samuel Gröner
 * */
public class GunTower extends EntityDecorator {

  protected static float DMG;
  protected static float SPICE;
  protected static float SPEED;
  public static final AssetsEnum TYPE = AssetsEnum.GUN_TOWER;

  public GunTower(Entity entity) {
    super(entity);
    SPEED = config.getGunTower().get("speed");
    SPICE = config.getGunTower().get("spice");
    DMG = config.getGunTower().get("dmg");
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
