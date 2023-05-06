package io.swapastack.dunetd.logic.enemy;

import io.swapastack.dunetd.enums.AssetsEnum;
import io.swapastack.dunetd.logic.entity.BasicEntity;
import io.swapastack.dunetd.logic.entity.Entity;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Is created as a singleton.
 *
 * @author Samuel Gröner
 * */
public class EnemyHandler {

  private static final EnemyHandler INSTANCE = new EnemyHandler();
  public static List<Entity> enemyList;

  private EnemyHandler(){
    enemyList = new ArrayList<>();
  }
  /**
   * creates a singleton instance of {@link EnemyHandler}.
   *
   * @return a {@link EnemyHandler}
   * */
  public static EnemyHandler getINSTANCE() {
    return INSTANCE;
  }
  /**
   * creates an instance of the specified {@link Enemy}.
   *
   * @param enemy the type of Enemy
   * */
  public void createEnemy(AssetsEnum enemy){
    switch (enemy){
      case INFANTRY:
        enemyList.add(new Enemy(new Infantry(new BasicEntity())));
        break;
      case HARVESTER:
        enemyList.add(new Enemy(new Harvester(new BasicEntity())));
        break;
      case BOSS_UNIT:
        enemyList.add(new Enemy(new Boss(new BasicEntity())));
        break;
      default:
        System.err.println("Couldn't load enemy!\nAssetsEnum enemy isn't an type of Enemy!");
    }
  }
  protected static void deleteEntity(Iterator<Entity> iterator){
    iterator.remove();
  }
  /**
   * Starts the update process of all {@link Enemy}'s.
   *
   * @param delta the time since the last update
   * */
  public void update(float delta) {
    Iterator<Entity> iterator = enemyList.iterator();
    while (iterator.hasNext()){
      iterator.next().update(delta, iterator);
    }
  }
}
