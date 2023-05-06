package io.swapastack.dunetd.logic.tower;

import static io.swapastack.dunetd.Config.config;

import io.swapastack.dunetd.enums.AssetsEnum;
import io.swapastack.dunetd.logic.DuneTDMap;
import io.swapastack.dunetd.logic.Path;
import io.swapastack.dunetd.logic.entity.BasicEntity;
import io.swapastack.dunetd.logic.entity.Entity;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Is created as a singleton.
 *
 * @author Samuel Gröner
 * */
public class TowerHandler {

  private static final TowerHandler INSTANCE = new TowerHandler();
  public static ArrayList<Entity> towerList;

  private TowerHandler(){
    towerList = new ArrayList<>();
  }
  /**
   * creates a singleton instance of {@link TowerHandler}.
   *
   * @return a {@link TowerHandler}
   * */
  public static TowerHandler getINSTANCE(){
  return INSTANCE;
  }
  /**
   * creates an instance of the specified {@link Tower}.
   *
   * @param tower the type of Tower
   * @param x the x coordinate
   * @param y the y coordinate
   * */
  public void createTower(AssetsEnum tower, float x, float y){
    if (tower != AssetsEnum.SHAI_HULUD_TOWER && Path.map[(int)y][(int)x] == 1) {
      return;
    }
    if (tower != AssetsEnum.SHAI_HULUD_TOWER && Path.map[(int)y][(int)x] == 0) {
      Path.map[(int) y][(int) x] = 1;
      DuneTDMap.path.update();
      if (DuneTDMap.path.path == null) {
        Path.map[(int) y][(int) x] = 0;
        return;
      }
    }
      switch (tower) {
        case GUN_TOWER:
          if (!((DuneTDMap.START.x == x && DuneTDMap.START.y == y)
              || (DuneTDMap.END.x == x && DuneTDMap.END.y == y)))
          if (DuneTDMap.spice >= config.getGunTower().get("spice")) {
            towerList.add(new Tower(new GunTower(new BasicEntity()), x, y));
            DuneTDMap.spice -= GunTower.SPICE;
          }
          break;
        case BOMB_TOWER:
          if (!((DuneTDMap.START.x == x && DuneTDMap.START.y == y)
              || (DuneTDMap.END.x == x && DuneTDMap.END.y == y)))
          if (DuneTDMap.spice >= config.getBombTower().get("spice")) {
            towerList.add(new Tower(new BombTower(new BasicEntity()), x, y));
            DuneTDMap.spice -= BombTower.SPICE;
          }
          break;
        case SONIC_TOWER:
          if (!((DuneTDMap.START.x == x && DuneTDMap.START.y == y)
              || (DuneTDMap.END.x == x && DuneTDMap.END.y == y)))
          if (DuneTDMap.spice >= config.getSonicTower().get("spice")) {
            towerList.add(new Tower(new SonicTower(new BasicEntity()), x, y));
            DuneTDMap.spice -= SonicTower.SPICE;
          }
          break;
        case SHAI_HULUD_TOWER:
          Entity klopfer;
          boolean tried = false;
          Iterator<Entity> iterator = towerList.iterator();
          while (iterator.hasNext()){
            Entity start = iterator.next();
            if (start.getTYPE() == tower){
              tried = true;
              if ((start.getX()-x == 0 || start.getY()-y == 0)
                  && !(start.getX()-x == 0 && start.getY()-y == 0)) {
                klopfer = new Tower(new Klopfer(new BasicEntity(), start), x, y);
                towerList.add(klopfer);
                DuneTDMap.shaiHulud = true;
                DuneTDMap.shaiHuludEntity = new ShaiHulud();
                break;
              }
              break;
            }
          }
          if (!tried) {
            klopfer = new Tower(new Klopfer(new BasicEntity(), null), x, y);
            towerList.add(klopfer);
          }
          break;
        default:
          System.err.println("Couldn't load tower!\nAssetsEnum tower isn't an type of Tower!");
      }
  }
  /**
   * if you want to sell the tower you just build
   * */
  public void sellEntity(float x, float y){
    Iterator<Entity> iterator = towerList.iterator();
    while (iterator.hasNext()){
      Entity tower = iterator.next();
      if (tower.getX() == x && tower.getY() == y && DuneTDMap.selling){
        iterator.remove();
        Path.map[(int) y][(int) x] = 0;
        DuneTDMap.path.update();
        DuneTDMap.spice += tower.getSPICE()/10;
      }
    }
    DuneTDMap.sellPoint = null;
    DuneTDMap.selling = false;
  }
  /**
   * well the Shai-Hulud did eat your tower
   * */
  public static void destroyTower(Iterator<Entity> iterator, Entity target){
    iterator.remove();
    int x = (int) target.getX();
    int y = (int) target.getY();
    Path.map[y][x] = 0;
  }
  /**
   * Starts the update process of all {@link Tower}'s.
   *
   * @param delta the time since the last update
   * */
  public void update(float delta){
    Iterator<Entity> iterator = towerList.iterator();
    while (iterator.hasNext()){
      iterator.next().update(delta,iterator);
    }
  }
}
