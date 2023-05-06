package io.swapastack.dunetd.logic.enemy;

import static io.swapastack.dunetd.Config.config;

import io.swapastack.dunetd.enums.AssetsEnum;
import io.swapastack.dunetd.logic.entity.Entity;
import io.swapastack.dunetd.logic.entity.EntityDecorator;

/**
 * A type of {@link Enemy}
 * Is the Infantry, just the normal Unit on the field.
 *
 * @author Samuel Gröner
 * */
public class Infantry extends EntityDecorator {

  protected static float SPEED;
  protected static float SPICE;
  protected float live;
  public final static AssetsEnum TYPE = AssetsEnum.INFANTRY;

  public Infantry(Entity entity){
    super(entity);
    SPEED = config.getInfantry().get("speed");
    SPICE = config.getInfantry().get("spice");
    live = config.getInfantry().get("live");
  }
  @Override
  public float getSPEED() {
    return SPEED;
  }
  @Override
  public AssetsEnum getTYPE(){
    return TYPE;
  }
  @Override
  public float getLive(float dmg) {
    live -= dmg;
    return live;
  }
  @Override
  public float getSPICE() {
    return SPICE;
  }
}
