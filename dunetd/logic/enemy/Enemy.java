package io.swapastack.dunetd.logic.enemy;

import com.badlogic.gdx.Gdx;
import io.swapastack.dunetd.enums.AssetsEnum;
import io.swapastack.dunetd.logic.DuneTDMap;
import io.swapastack.dunetd.logic.Point;
import io.swapastack.dunetd.logic.entity.Entity;
import io.swapastack.dunetd.logic.entity.EntityDecorator;
import io.swapastack.dunetd.logic.tower.Tower;
import io.swapastack.dunetd.logic.tower.TowerHandler;
import io.swapastack.dunetd.ui.GameScreen;
import java.util.Iterator;

/**
 * Represents the attributes of all enemy's.
 * An Enemy class schuld only be created containing the types:
 * {@link Infantry}, {@link Harvester} and {@link Boss}.
 *
 * @author Samuel Gröner
 * */
public class Enemy extends EntityDecorator {
  //pos
  private float x;
  private float y;
  private int progress;
  protected boolean slow;
  /**
   * An Enemy class schuld only be created containing the types:
   * {@link Infantry}, {@link Harvester} and {@link Boss}.
   *
   * @param entity the type of {@link Enemy}
   * */
  public Enemy(Entity entity){
    super(entity);
    x = DuneTDMap.START.x;
    y = DuneTDMap.START.y;
    progress = 1;
  }
  public void setSlow(boolean slow){
    this.slow = slow;
  }
  /**
   * Starts the update process of the attributes of the Enemy.
   *
   * @param delta the time since the last update
   * */
  @Override
  public void update(float delta, Iterator<Entity> iterator){
    checkSlow();
    if (slow) {
      delta = delta*0.5f;
      setSlow(false);
    }
    if (progress <= DuneTDMap.path.path.size() - 1) {
      updatePos(delta);
    } else {
      DuneTDMap.live -= this.getLive(0);
      EnemyHandler.deleteEntity(iterator);
    }
    if (getLive(0) <= 0) {
      DuneTDMap.spice += this.getSPICE();
      DuneTDMap.score += this.getSPICE()*Math.sqrt(DuneTDMap.live);
      EnemyHandler.deleteEntity(iterator);
    }
  }
  /**
   * looks for SonicTowers in the near area,
   * if found it slows the unit
   * */
  private void checkSlow(){
    Iterator<Entity> iterator = TowerHandler.towerList.iterator();
    while (iterator.hasNext()) {
      Entity tower = iterator.next();
      if (tower.getTYPE() == AssetsEnum.SONIC_TOWER) {
        int xDiff = Math.abs((int)(tower.getX()* GameScreen.tileSize - x));
        int yDiff = Math.abs((int)(tower.getY()* GameScreen.tileSize - y));
        if (yDiff <= Tower.RANGE && xDiff <= Tower.RANGE) {
          setSlow(true);
        }
      }
    }
  }
  /**
   * moves the unit from a to b
   * */
  private void updatePos(float delta){
    Point target = DuneTDMap.path.path.get(progress);
    float diffX = (target.x* (Gdx.graphics.getWidth()/DuneTDMap.WIDTH)) - x;
    float diffY = (target.y* (Gdx.graphics.getHeight()/DuneTDMap.HEIGHT)) - y;
    //I use the signum function to preserve the general direction
    //This way I can only navigate up, down, left and right
    //because the relative direction gets lost
    //I use x+1/x to ensure the signum function is never 0
    x += delta * getSPEED() * Math.signum(diffX + 1/diffX) * 0.57f;
    y += delta * getSPEED() * Math.signum(diffY + 1/diffY) * 0.57f;
    if ((Math.abs(diffX) < 13) && (Math.abs(diffY) < 13)) {
      progress++;
    }
  }
  @Override
  public float getX() {
    return x;
  }
  @Override
  public float getY() {
    return y;
  }
}
