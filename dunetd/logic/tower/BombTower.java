package io.swapastack.dunetd.logic.tower;

import static io.swapastack.dunetd.Config.config;

import io.swapastack.dunetd.enums.AssetsEnum;
import io.swapastack.dunetd.logic.entity.Entity;
import io.swapastack.dunetd.logic.entity.EntityDecorator;

/**
 * A type of {@link Tower}.
 * Only attacks very slow, but does area dmg
 *
 * @author Samuel Gröner
 * */
public class BombTower extends EntityDecorator {

  protected static float DMG;
  protected static float SPICE;
  protected static float SPEED;
  public static final AssetsEnum TYPE = AssetsEnum.BOMB_TOWER;

  public BombTower(Entity entity) {
    super(entity);
    SPEED = config.getBombTower().get("speed");
    SPICE = config.getBombTower().get("spice");
    DMG = config.getBombTower().get("dmg");
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
