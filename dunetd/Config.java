package io.swapastack.dunetd;

import com.badlogic.gdx.Gdx;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;

public class Config {

  private String name;
  private int height;
  private int width;
  private int spice;
  private int live;
  private Map<String,Integer> start;
  private Map<String,Integer> end;
  private Map<String,Float> infantry;
  private Map<String,Float> harvester;
  private Map<String,Float> boss;
  private Map<String,Float> gunTower;
  private Map<String,Float> bombTower;
  private Map<String,Float> sonicTower;
  private Map<String,Float> shaiHulud;
  private JsonArray waves;
  public static Config config;

  public static boolean loadConfig(){
    //"core/src/io/swapastack/dunetd/Config.json"
    try {
      Gson gson = new Gson();
      config = gson.fromJson(new FileReader("C:\\Users\\samue\\gitlabUniulm\\sopraEinzel\\samuel-groener\\dune-td-main\\core\\src\\io\\swapastack\\dunetd\\Config.json"),Config.class);
      return true;
    }catch (FileNotFoundException e){
      e.printStackTrace();
      System.err.println("Couldn't find Config.json");
    }
    return false;
  }

  public int getHeight() {
    return height;
  }

  public int getWidth() {
    return width;
  }

  public String getName() {
    return name;
  }

  public int getSpice() {
    return spice;
  }

  public int getLive() {
    return live;
  }

  public Map<String, Integer> getStart() {
    return start;
  }

  public Map<String, Integer> getEnd() {
    return end;
  }

  public Map<String, Float> getInfantry() {
    return infantry;
  }

  public Map<String, Float> getHarvester() {
    return harvester;
  }

  public Map<String, Float> getBoss() {
    return boss;
  }

  public Map<String, Float> getGunTower() {
    return gunTower;
  }

  public Map<String, Float> getBombTower() {
    return bombTower;
  }

  public Map<String, Float> getSonicTower() {
    return sonicTower;
  }

  public Map<String, Float> getShaiHulud() {
    return shaiHulud;
  }

  public JsonArray getWaves() {
    return waves;
  }
}
