package io.swapastack.dunetd.ui;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import io.swapastack.dunetd.DuneTD;
import io.swapastack.dunetd.enums.AssetsEnum;
import io.swapastack.dunetd.logic.DuneTDMap;
import io.swapastack.dunetd.enums.ScreenEnum;
import io.swapastack.dunetd.logic.Point;
import io.swapastack.dunetd.logic.enemy.EnemyHandler;
import io.swapastack.dunetd.logic.entity.Entity;
import io.swapastack.dunetd.logic.tower.ShaiHulud;
import io.swapastack.dunetd.logic.tower.TowerHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * The GameScreen class.
 *
 * @author Samuel Gröner
 */
public class GameScreen implements Screen {

  private final DuneTD parent;

  //textures
  private SpriteBatch spriteBatch;
  private Texture brdfLUT;

  //view
  private OrthographicCamera camera;
  private Viewport viewport;
  private Stage stage;
  private Skin skin;

  // 2D assets
  HashMap<AssetsEnum, Texture> textureAssetHashMap;
  public static float tileSize;

  // Grid Specifications
  DuneTDMap map = DuneTDMap.getINSTANCE();

  // SpaiR/imgui-java
  public ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
  public ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
  long windowHandle;

  public GameScreen(DuneTD parent) {
    this.parent = parent;
    // initialize OrthographicCamera with current screen size
    // e.g. OrthographicCamera(1280.f, 720.f)
    camera = new OrthographicCamera((float)Gdx.graphics.getWidth(), (float)Gdx.graphics.getHeight());
    // initialize ScreenViewport with the OrthographicCamera created above
    viewport = new ScreenViewport(camera);
    // initialize SpriteBatch
    spriteBatch = new SpriteBatch();
    // initialize the Stage with the ScreenViewport created above
    stage = new Stage(viewport, spriteBatch);
    // initialize the Skin
    skin = new Skin(Gdx.files.internal("glassy/skin/glassy-ui.json"));

    // SpaiR/imgui-java
    ImGui.createContext();
    windowHandle = ((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle();
    imGuiGlfw.init(windowHandle, true);
    imGuiGl3.init("#version 120");


    // load texture
    textureAssetHashMap = new HashMap<>();
    for (AssetsEnum asset: AssetsEnum.values()) {
      Texture texture = new Texture(asset.path);
      textureAssetHashMap.put(asset, texture);
    }

    // load background music
    // note: every game should have some background music
    //       feel free to exchange the current wav with one of your own music files
    //       but you must have the right license for the music file
    //backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("piano/piano_loop.wav"));
    //backgroundMusic.setLooping(true);
    //backgroundMusic.play(); // TODO: reactivate
  }

  /**
   * Called when this screen becomes the current screen for a {@link Game}.
   */
  @Override
  public void show() {
    Gdx.input.setInputProcessor(stage);
  }

  static float time = 0;
  /**
   * Called when the screen should render itself.
   *
   * @param delta - The time in seconds since the last render.
   */
  @Override
  public void render(float delta) {
    if (DuneTDMap.live <= 0){
      parent.changeScreen(ScreenEnum.ENDSCREEN);
    }
    if (DuneTDMap.waveCounter >= DuneTDMap.WAVES.getAsJsonArray().size()-1 && EnemyHandler.enemyList.size() == 0 && !DuneTDMap.spawning){
      parent.changeScreen(ScreenEnum.ENDSCREEN);
    }
    map.update(delta);
    tileSize = stage.getHeight()/DuneTDMap.HEIGHT;
    // clear the client area (Screen) with the clear color (black)
    Gdx.gl.glClearColor(0, 0, 0, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    // update camera
    camera.update();

    // update the current SpriteBatch
    spriteBatch.setProjectionMatrix(camera.combined);

    // draw background graphic
    renderBackground(delta);
    spriteBatch.begin();
    for (Entity entity: EnemyHandler.enemyList) {
      spriteBatch.draw(textureAssetHashMap.get(entity.getTYPE()),entity.getX(),entity.getY(),tileSize,tileSize);
    }
    for (Entity entity: TowerHandler.towerList) {
      spriteBatch.draw(textureAssetHashMap.get(entity.getTYPE()),entity.getX()*tileSize,entity.getY()*tileSize,tileSize,tileSize);
    }
    if (DuneTDMap.shaiHulud && DuneTDMap.shaiHuludEntity != null) {
      ShaiHulud shaiHulud = DuneTDMap.shaiHuludEntity;
      spriteBatch.draw(textureAssetHashMap.get(shaiHulud.getTYPE()),shaiHulud.x,shaiHulud.y,tileSize,tileSize);
    }
    spriteBatch.end();
    //ImGui:
    imGuiGlfw.newFrame();
    ImGui.newFrame();

    time += delta;
    ImGui.begin("Performance", ImGuiWindowFlags.AlwaysAutoResize);
    ImGui.text(String.format(Locale.US,"deltaTime: %1.6f", delta));
    ImGui.text(String.format(Locale.US,"frames: %1.6f", (1/delta)));
    ImGui.text(String.format(Locale.US,"live: %1.3f ", (float)DuneTDMap.live));
    ImGui.text(String.format(Locale.US,"spice: %1.3f ", (float)DuneTDMap.spice));
    ImGui.text(String.format(Locale.US,"score: %1.3f ", DuneTDMap.score));
    if (DuneTDMap.buildPhase) {
      ImGui.text(String.format(Locale.US, "time: %1.3f ", (30 - time)));
    }
    if (!DuneTDMap.buildPhase) {
      time = 0;
    }
    ImGui.end();
    ImGui.begin("TowerMenu", ImGuiWindowFlags.AlwaysAutoResize);
    if (DuneTDMap.buildPhase) {
      if (ImGui.button("GunTower")) {
        DuneTDMap.toBuild = AssetsEnum.GUN_TOWER;
      }
      if (ImGui.button("BombTower")) {
        DuneTDMap.toBuild = AssetsEnum.BOMB_TOWER;
      }
      if (ImGui.button("SonicTower")) {
        DuneTDMap.toBuild = AssetsEnum.SONIC_TOWER;
      }
      if (ImGui.button("Skip Timer")){
        DuneTDMap.waveTime += 30;
      }
      if (ImGui.button("sell")){
        DuneTDMap.selling = true;
      }
    }
    if (DuneTDMap.shaiHuludEntity == null) {
      if (ImGui.button("Klopfer")) {
        DuneTDMap.toBuild = AssetsEnum.SHAI_HULUD_TOWER;
      }
    }
    if (ImGui.button("MainMenu")){
      parent.changeScreen(ScreenEnum.MENU);
    }
    if (ImGui.button("Exit")){
      Gdx.app.exit();
    }
    ImGui.end();
    if (Gdx.input.isButtonPressed(Buttons.LEFT) && DuneTDMap.selling){
      DuneTDMap.sellPoint = new Point((int)(Gdx.input.getX()/tileSize), (int)((stage.getHeight() - Gdx.input.getY())/tileSize),null);
    }

    // SpaiR/imgui-java
    ImGui.render();
    imGuiGl3.renderDrawData(ImGui.getDrawData());
    // update the Stage
    stage.act(delta);
    // draw the Stage
    stage.draw();
  }
  public void renderBackground(float delta){
    spriteBatch.begin();
    for (int i = 0; i < DuneTDMap.HEIGHT; i++) {
      for (int k = 0; k < DuneTDMap.WIDTH; k++) {
        spriteBatch.draw(textureAssetHashMap.get(map.getBackgroundMap()[i][k]),
            k * tileSize,
            i * tileSize,
            tileSize,
            tileSize);
      }
    }
    if (DuneTDMap.path.path != null) {
      List<Point> path = DuneTDMap.path.path;
      for (int i = 0; i < path.size()-2; i++) {

        if (path.get(i).x - path.get(i + 1).x == 1) {//right
          if (path.get(i+1).y - path.get(i + 2).y == 1) {//down
            spriteBatch.draw(textureAssetHashMap.get(AssetsEnum.ROAD_DR),path.get(i+1).x*tileSize,path.get(i+1).y*tileSize,tileSize,tileSize);
            continue;
          }
          if (path.get(i+1).y - path.get(i + 2).y == -1) {//up
            spriteBatch.draw(textureAssetHashMap.get(AssetsEnum.ROAD_UR),path.get(i+1).x*tileSize,path.get(i+1).y*tileSize,tileSize,tileSize);
            continue;
          }
          if (path.get(i+1).y - path.get(i + 2).y == 0) {
            spriteBatch.draw(textureAssetHashMap.get(AssetsEnum.ROAD_LR),path.get(i+1).x*tileSize,path.get(i+1).y*tileSize,tileSize,tileSize);
            continue;
          }
        }if (path.get(i).x - path.get(i + 1).x == -1) {//left
          if (path.get(i+1).y - path.get(i + 2).y == 1) {//down
            spriteBatch.draw(textureAssetHashMap.get(AssetsEnum.ROAD_DL),path.get(i+1).x*tileSize,path.get(i+1).y*tileSize,tileSize,tileSize);
            continue;
          }
          if (path.get(i+1).y - path.get(i + 2).y == -1) {//up
            spriteBatch.draw(textureAssetHashMap.get(AssetsEnum.ROAD_UL),path.get(i+1).x*tileSize,path.get(i+1).y*tileSize,tileSize,tileSize);
            continue;
          }if (path.get(i+1).y - path.get(i + 2).y == 0) {
            spriteBatch.draw(textureAssetHashMap.get(AssetsEnum.ROAD_LR),path.get(i+1).x*tileSize,path.get(i+1).y*tileSize,tileSize,tileSize);
            continue;
          }
        }
        if (path.get(i).y - path.get(i + 1).y == 1) {//down
          if (path.get(i+1).x - path.get(i + 2).x == 1) {//left
            spriteBatch.draw(textureAssetHashMap.get(AssetsEnum.ROAD_UL),path.get(i+1).x*tileSize,path.get(i+1).y*tileSize,tileSize,tileSize);
            continue;
          }
          if (path.get(i+1).x - path.get(i + 2).x == -1) {//right
            spriteBatch.draw(textureAssetHashMap.get(AssetsEnum.ROAD_UR),path.get(i+1).x*tileSize,path.get(i+1).y*tileSize,tileSize,tileSize);
            continue;
          }
          if (path.get(i+1).x - path.get(i + 2).x == 0) {
            spriteBatch.draw(textureAssetHashMap.get(AssetsEnum.ROAD_DU),path.get(i+1).x*tileSize,path.get(i+1).y*tileSize,tileSize,tileSize);
            continue;
          }
        }
        if (path.get(i).y - path.get(i + 1).y == -1) {//up
          if (path.get(i+1).x - path.get(i + 2).x == 1) {//left
            spriteBatch.draw(textureAssetHashMap.get(AssetsEnum.ROAD_DL),path.get(i+1).x*tileSize,path.get(i+1).y*tileSize,tileSize,tileSize);
            continue;
          }
          if (path.get(i+1).x - path.get(i + 2).x == -1) {//right
            spriteBatch.draw(textureAssetHashMap.get(AssetsEnum.ROAD_DR),path.get(i+1).x*tileSize,path.get(i+1).y*tileSize,tileSize,tileSize);
            continue;
          }
          if (path.get(i+1).x - path.get(i + 2).x == 0) {
            spriteBatch.draw(textureAssetHashMap.get(AssetsEnum.ROAD_DU),path.get(i+1).x*tileSize,path.get(i+1).y*tileSize,tileSize,tileSize);
            continue;
          }
        }
      }
    }
    if (DuneTDMap.toBuild != null) {
      for (int i = 0; i < DuneTDMap.HEIGHT; i++) {
        for (int k = 0; k < DuneTDMap.WIDTH; k++) {
          spriteBatch.draw(textureAssetHashMap.get(AssetsEnum.GRID),
              k * tileSize,
              i * tileSize,
              tileSize,
              tileSize);
        }
      }
      spriteBatch.draw(textureAssetHashMap.get(DuneTDMap.toBuild), Gdx.input.getX(),
          stage.getHeight() - Gdx.input.getY());
      if (Gdx.input.isButtonPressed(Buttons.LEFT)) {
        DuneTDMap.buildPoint = new Point((int)(Gdx.input.getX()/tileSize), (int)((stage.getHeight() - Gdx.input.getY())/tileSize),null);
      }
    }
    spriteBatch.end();
  }

  @Override
  public void resize(int width, int height) {
  }

  @Override
  public void pause() {
    // TODO: implement pause logic if needed
  }

  @Override
  public void resume() {
    // TODO: implement resume logic if needed
  }

  @Override
  public void hide() {
    // TODO: implement hide logic if needed
  }

  @Override
  public void dispose() {
    for (AssetsEnum texture : AssetsEnum.values()) {
      textureAssetHashMap.get(texture).dispose();
    }
    stage.dispose();
    spriteBatch.dispose();
  }

}
