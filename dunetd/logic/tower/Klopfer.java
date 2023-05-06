package io.swapastack.dunetd.logic.tower;

import io.swapastack.dunetd.enums.AssetsEnum;
import io.swapastack.dunetd.logic.entity.Entity;
import io.swapastack.dunetd.logic.entity.EntityDecorator;
/**
 * A type of {@link Tower}.
 * If two are placed on the battlefield they shall call the Shai-Hulud.
 * Can be placed everywhere.
 *
 * @author Samuel Gröner
 * */
public class Klopfer extends EntityDecorator {
  protected static float DMG;
  protected static float SPICE;
  protected static float SPEED;
  public static final AssetsEnum TYPE = AssetsEnum.SHAI_HULUD_TOWER;
  public Entity start;

  public Klopfer(Entity entity,Entity start) {
    super(entity);
    SPEED = 0;
    SPICE = 0;
    DMG = 0;
    this.start = start;
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
  @Override
  public Entity getStart() {
    return start;
  }
}
