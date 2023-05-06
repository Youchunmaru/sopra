package io.swapastack.dunetd.logic;

import static io.swapastack.dunetd.Config.config;

import com.google.gson.JsonElement;
import io.swapastack.dunetd.Config;
import io.swapastack.dunetd.enums.AssetsEnum;
import io.swapastack.dunetd.logic.enemy.EnemyHandler;
import io.swapastack.dunetd.logic.tower.ShaiHulud;
import io.swapastack.dunetd.logic.tower.TowerHandler;


/**
 * Handels the logical aspect of the Game.
 * Is created as a Singleton.
 *
 * @author Samuel Gröner
 * */
public class DuneTDMap {
  private static final DuneTDMap INSTANCE = new DuneTDMap();
  public static int HEIGHT;
  public static int WIDTH;
  public static int spice;
  public static int live;
  public static Point START;
  public static Point END;
  public static JsonElement WAVES;
  private static AssetsEnum[][] backgroundMap;
  public static EnemyHandler enemyHandler;
  public static TowerHandler towerHandler;
  public static Path path;
  public static int waveCounter = -1;
  public static boolean newRound;
  public static float waveTime;
  public static boolean spawning;
  private static int waveProgress;
  public static boolean buildPhase;
  public static AssetsEnum toBuild;
  public static Point buildPoint;
  public static boolean selling;
  public static Point sellPoint;
  public static boolean shaiHulud;
  public static ShaiHulud shaiHuludEntity;
  public static float score;

  private DuneTDMap() {
    if (Config.loadConfig()) {
      System.out.println("Config loaded!");
      HEIGHT = config.getHeight();
      WIDTH = config.getWidth();
      spice = config.getSpice();
      live = config.getLive();
      WAVES = config.getWaves();
      START = new Point(config.getStart().get("x"), config.getStart().get("y"), null);
      END = new Point(config.getEnd().get("x"), config.getEnd().get("y"), null);
      enemyHandler = EnemyHandler.getINSTANCE();
      towerHandler = TowerHandler.getINSTANCE();
      backgroundMap = new AssetsEnum[HEIGHT][WIDTH];
      generateMap();
      path = Path.getINSTANCE(START, END);
      buildPhase = true;
      path.update();
      score = 0;
    }
  }
  /**
   * creates a Singleton Instance of {@link DuneTDMap}.
   *
   * @return a {@link DuneTDMap}
   * */
  public static DuneTDMap getINSTANCE(){
    return INSTANCE;
  }

  /**
   * generates the background
   **/
  public void generateMap(){
    for (int i = 0; i < HEIGHT; i++) {
      for (int j = 0; j < WIDTH; j++) {
        backgroundMap[i][j] = AssetsEnum.BACKGROUND;
      }
    }
    if (!((START.y == END.y && Math.abs(START.x - END.x) < 2)
        || (START.x == END.x && Math.abs(START.y - END.y) < 2))) {
      backgroundMap[START.y][START.x] = AssetsEnum.PORTAL_START;
      backgroundMap[END.y][END.x] = AssetsEnum.PORTAL_END;
    }
  }
  /**
   * Starts the update process of the game logic.
   *
   * @param delta the time since the last update
   * */
  public void update(float delta) {
    if (shaiHulud && shaiHuludEntity != null){
      shaiHuludEntity.update(delta);
    }
    towerUpdater(delta);
    if (buildPhase) {
      path.update();
      waveTime += delta;
      if (waveTime > 30) {
        buildPhase = false;
        waveTime = 0;
        newRound = true;
      }
    }else {
      waveUpdater(delta);
      if (EnemyHandler.enemyList.size() == 0 && !spawning) {
        buildPhase = true;
      }
    }
    enemyHandler.update(delta);
    towerHandler.update(delta);
  }
  /**
   * handles the building inputs
   * */
  private void towerUpdater(float delta){
    if (toBuild != null) {
      if (buildPoint != null) {
        towerHandler.createTower(toBuild, buildPoint.x, buildPoint.y);
        DuneTDMap.toBuild = null;
        DuneTDMap.buildPoint = null;
        path.update();
      }
    }
    if(selling){
      if (sellPoint != null){
        towerHandler.sellEntity(sellPoint.x, sellPoint.y);
      }
    }
  }
  /**
   * spawns the enemy's
   * */
  private void waveUpdater(float delta){
    if (newRound) {
      path.update();
      waveTime = 0.0f;
      waveProgress = 0;
      spawning = true;
      waveCounter++;
      if (!shaiHulud){
        shaiHuludEntity = null;
      }
    }
    if (waveCounter < WAVES.getAsJsonArray().size()) {
      if (spawning) {
        String wave = WAVES.getAsJsonArray().get(waveCounter).getAsString();
        if (waveProgress < wave.length()) {
          waveTime += delta;
          if (waveTime > 2.1f) {
            AssetsEnum interpretedWave = waveReader(wave, waveProgress);
            assert interpretedWave != null;
            enemyHandler.createEnemy(interpretedWave);
            waveProgress++;
            waveTime = 0.0f;
          }
        }else {
          spawning = false;
        }
      }
    }
    newRound = false;
  }
  /**
   * reads which enemy should be spawnt
   * */
  public static AssetsEnum waveReader(String wave,int waveProgress){
    if (wave.length() > 0) {
      char[] units = wave.toCharArray();
      char unit = units[waveProgress];
      switch (unit) {
        case 'i':
          return AssetsEnum.INFANTRY;
        case 'h':
          return AssetsEnum.HARVESTER;
        case 'b':
          return AssetsEnum.BOSS_UNIT;
        default:
          System.err.println("Couldn't interpret the type of Unit!\nIn DuneTDMap#waveReader");
      }
    }
    return null;
  }
  public AssetsEnum[][] getBackgroundMap() {
    return backgroundMap;
  }
}
