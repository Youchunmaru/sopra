package io.swapastack.dunetd.enums;

/**
 * Holds all paths to the needed assets.
 * Is used to specify the specific type of {@link io.swapastack.dunetd.logic.entity.Entity}
 *
 * @author Samuel Gröner
 * */
public enum AssetsEnum {
  //default assets:
  BACKGROUND("default_background"),
  PORTAL_START("default_portalStart"),
  PORTAL_END("default_portalEnd"),
  GUN_TOWER("default_gunTower"),
  BOMB_TOWER("default_bombTower"),
  SONIC_TOWER("default_sonicTower"),
  INFANTRY("default_infantry"),
  HARVESTER("default_harvester"),
  BOSS_UNIT("default_bossUnit"),
  GUN_TOWER_ATTACK("default_gunTowerAttack"),
  BOMB_TOWER_ATTACK("default_bombTowerAttack"),
  TOWER_DEATH("default_towerDeath"),
  UNIT_DEATH("default_unitDeath"),
  GRID("default_grid"),
  ROAD_LR("default_pathlr"),
  ROAD_DU("default_pathdu"),
  ROAD_DL("default_pathCurvedl"),
  ROAD_DR("default_pathCurvedr"),
  ROAD_UL("default_pathCurveul"),
  ROAD_UR("default_pathCurveur"),
  SHAI_HULUD_TOWER("default_shaiHuludTower"),
  SHAI_HULUD("default_shaiHulud");
  //extra assets: TODO: search for some extra cool assets
  //SONIC_TOWER_ATTACK("default_sonicTowerAttack")

  public String path;
  public String fileName;

  AssetsEnum(String fileName){
    this.fileName = fileName;
    this.path = "kenney_top_down/PNG/Retina/" + fileName + ".png";
  }
}
