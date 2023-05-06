package io.swapastack.dunetd.logic.entity;

import io.swapastack.dunetd.enums.AssetsEnum;
import java.util.Iterator;

public interface Entity {

  void update(float delta, Iterator<Entity> iterator);
  float getX();
  float getY();
  float getSPEED();
  AssetsEnum getTYPE();
  float getLive(float dmg);
  float getSPICE();
  float getDMG();
  void setSlow(boolean slow);
  public Entity getStart();
}
