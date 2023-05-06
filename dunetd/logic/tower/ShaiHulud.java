package io.swapastack.dunetd.logic.tower;

import static io.swapastack.dunetd.Config.config;

import com.badlogic.gdx.Gdx;
import io.swapastack.dunetd.enums.AssetsEnum;
import io.swapastack.dunetd.logic.DuneTDMap;
import io.swapastack.dunetd.logic.enemy.EnemyHandler;
import io.swapastack.dunetd.logic.entity.Entity;
import io.swapastack.dunetd.ui.GameScreen;
import java.util.Iterator;
/**
 * A type of {@link Tower}.
 * Kills every thing in his way including your own.
 *
 * @author Samuel Gröner
 * */
public class ShaiHulud {

  protected static float DMG;
  protected static float SPEED;
  public static final AssetsEnum TYPE = AssetsEnum.SHAI_HULUD;
  public float x;
  public float y;
  public static final float RANGE = 13;
  private float targetX;
  private float targetY;

  public ShaiHulud(){
    DMG = config.getShaiHulud().get("dmg");
    SPEED = config.getShaiHulud().get("speed");
    Iterator<Entity> iterator = TowerHandler.towerList.iterator();
    while (iterator.hasNext()){
      Entity end = iterator.next();
      if (end.getTYPE() == AssetsEnum.SHAI_HULUD_TOWER){
        if (end.getStart() != null){
          Entity start = end.getStart();
          x = start.getX()*GameScreen.tileSize;
          y = start.getY()*GameScreen.tileSize;
          targetX = end.getX()*GameScreen.tileSize;
          targetY = end.getY()*GameScreen.tileSize;
          if (targetX-x == 0){
            if (targetY-y > 0){
              targetY = DuneTDMap.HEIGHT*GameScreen.tileSize;
              y = 0;
            }else {
              targetY = 0;
              y = DuneTDMap.HEIGHT*GameScreen.tileSize;
            }
          }else {
            if (targetX-x > 0){
              targetX = DuneTDMap.WIDTH*GameScreen.tileSize;
              x = 0;
            }else {
              targetX = 0;
              x = DuneTDMap.WIDTH*GameScreen.tileSize;
            }
          }
        }
      }
    }
  }
  public void update(float delta){
    attack();
    destroy();
    float diffX = targetX - x;
    float diffY = targetY - y;
    //I use the signum function to preserve the general direction
    //This way I can only navigate up, down, left and right
    //because the relative direction gets lost
    //I use x+1/x to ensure the signum function is never 0
    x += delta * getSPEED() * Math.signum(diffX + 1/diffX) * 0.31f;
    y += delta * getSPEED() * Math.signum(diffY + 1/diffY) * 0.31f;
    if ((Math.abs(diffX) < RANGE) && (Math.abs(diffY) < RANGE)) {
      DuneTDMap.shaiHulud = false;
      TowerHandler.towerList.removeIf(tower -> tower.getTYPE() == AssetsEnum.SHAI_HULUD_TOWER);
    }
  }
  private void destroy(){
    Iterator<Entity> iterator = TowerHandler.towerList.iterator();
    while (iterator.hasNext()) {
      Entity target = iterator.next();
      float diffX = target.getX()*GameScreen.tileSize - x;
      float diffY = target.getY()*GameScreen.tileSize - y;
      if ((Math.abs(diffX) < RANGE) && (Math.abs(diffY) < RANGE)) {
        TowerHandler.destroyTower(iterator, target);
      }
    }
  }
  private void attack(){
    Iterator<Entity> iterator = EnemyHandler.enemyList.iterator();
    while (iterator.hasNext()) {
      Entity target = iterator.next();
      float diffX = target.getX() - x;
      float diffY = target.getY() - y;
      if ((Math.abs(diffX) < RANGE) && (Math.abs(diffY) < RANGE)) {
        target.getLive(DMG);
      }
    }
  }
  public AssetsEnum getTYPE(){
    return TYPE;
  }
  private float getSPEED(){
    return SPEED;
  }
}
