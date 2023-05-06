package io.swapastack.dunetd.logic.entity;

import io.swapastack.dunetd.enums.AssetsEnum;
import java.util.Iterator;

/**
 *
 *
 * @author Sasmuel Gröner
 * */
public class EntityDecorator implements Entity {

  protected Entity entity;

  public EntityDecorator(Entity entity){
    this.entity = entity;
  }
  @Override
  public void update(float delta, Iterator<Entity> iterator) {
    this.entity.update(delta, iterator);
  }

  @Override
  public float getX() {
    return this.entity.getX();
  }

  @Override
  public float getY() {
    return this.entity.getY();
  }

  @Override
  public float getSPEED() {
    return this.entity.getSPEED();
  }

  @Override
  public AssetsEnum getTYPE() {
    return this.entity.getTYPE();
  }

  @Override
  public float getLive(float dmg) {
    return this.entity.getLive(dmg);
  }

  @Override
  public float getSPICE() {
    return this.entity.getSPICE();
  }

  @Override
  public float getDMG() {
    return this.entity.getDMG();
  }

  @Override
  public void setSlow(boolean slow){
    this.entity.setSlow(slow);
  }

  @Override
  public Entity getStart(){return this.entity.getStart();}
}
