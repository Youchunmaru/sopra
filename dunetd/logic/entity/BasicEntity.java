package io.swapastack.dunetd.logic.entity;

import io.swapastack.dunetd.enums.AssetsEnum;
import java.util.Iterator;

/**
 *
 *
 * @author Samuel Gröner
 * */
public class BasicEntity implements Entity{

  @Override
  public void update(float delta, Iterator<Entity> iterator) {
  }

  @Override
  public float getX() {
    return -1;
  }

  @Override
  public float getY() {
    return -1;
  }

  @Override
  public float getSPEED() {
    return -1;
  }

  @Override
  public AssetsEnum getTYPE() {
    return null;
  }

  @Override
  public float getLive(float dmg) {
    return -1;
  }

  @Override
  public float getSPICE() {
    return -1;
  }

  @Override
  public float getDMG() {
    return -1;
  }

  @Override
  public void setSlow(boolean slow){

  }

  @Override
  public Entity getStart(){return null;}
}
