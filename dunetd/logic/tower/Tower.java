package io.swapastack.dunetd.logic.tower;

import io.swapastack.dunetd.enums.AssetsEnum;
import io.swapastack.dunetd.logic.DuneTDMap;
import io.swapastack.dunetd.logic.Path;
import io.swapastack.dunetd.logic.enemy.EnemyHandler;
import io.swapastack.dunetd.logic.entity.Entity;
import io.swapastack.dunetd.logic.entity.EntityDecorator;
import io.swapastack.dunetd.ui.GameScreen;
import java.util.Iterator;

/**
 * Represents the attributes of all enemy's.
 * An Enemy class should only be created containing the types:
 * {@link GunTower}, {@link BombTower} and {@link SonicTower}.
 *
 * @author Samuel Gröner
 * */
public class Tower extends EntityDecorator {

  //pos
  private float x;
  private float y;
  public static final float RANGE = 103;
  private float stackedDelta = 0;
  /**
   * An Enemy class should only be created containing the types:
   * {@link GunTower}, {@link BombTower} and {@link SonicTower}.
   *
   * @param entity the type of {@link Tower}
   * */
  public Tower(Entity entity, float x, float y) {
    super(entity);
    this.x = x;
    this.y = y;
  }
  /**
   * starts the update process
   * */
  @Override
  public void update(float delta, Iterator<Entity> iterator) {
    if(stackedDelta > (this.getSPEED()/31)) {
      checkAttack();
      stackedDelta = 0;
    }
    stackedDelta += delta;
  }
  /**
   * looks if an enemy is nearby. if one is than it deals dmg.
   * */
  private void checkAttack(){
    Iterator<Entity> iterator = EnemyHandler.enemyList.iterator();
    while (iterator.hasNext()){
      Entity enemy = iterator.next();
      int xDiff = Math.abs((int)(enemy.getX() - x * GameScreen.tileSize));
      int yDiff = Math.abs((int)(enemy.getY() - y * GameScreen.tileSize));
      if (yDiff <= RANGE && xDiff <= RANGE) {
        if (this.getTYPE() == AssetsEnum.BOMB_TOWER){
          Iterator<Entity> bombIterator = EnemyHandler.enemyList.iterator();
          while (bombIterator.hasNext()){
            Entity bombEnemy = bombIterator.next();
            int xDiffB = Math.abs((int)(bombEnemy.getX() - enemy.getX()));
            int yDiffB = Math.abs((int)(bombEnemy.getY() - enemy.getY()));
            if (yDiffB <= RANGE/2 && xDiffB <= RANGE/2 && !enemy.equals(bombEnemy)) {
              bombEnemy.getLive(this.getDMG()/2);
            }
          }
        }
        enemy.getLive(this.getDMG());
        break;
      }
    }
  }
  @Override
  public float getX(){return x;}
  @Override
  public float getY(){return y;}
}
