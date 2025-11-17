package org.mipt;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.google.gson.Gson;
import org.mipt.entity.Molecule;
import org.mipt.entity.SimulationConfig;

import java.io.IOException;

public class Main extends ApplicationAdapter {
  private Physics physics;
  private PhysicsLogger logger;

  private static final float WORLD_HEIGHT = 600;
  private static final float WORLD_WIDTH = 1000;

  private static final float RENDER_SCALE = 1E9f;

  private static final float FIXED_TIME_STEP = 0.01f;
  private boolean beginToIncreaseArea = false;

  private OrthographicCamera camera;
  private SpriteBatch batch;
  private ShapeRenderer shapeRenderer;
  private SimulationConfig config;
  private FillViewport viewport;
  private float accumulator = 0f;
  private int thermostatSteps = 0;

  @Override
  public void create() {
    Gson gson = new Gson();
    FileHandle file = Gdx.files.internal("config/simulation.json");
    SimulationConfig config = gson.fromJson(file.reader(), SimulationConfig.class);
    camera = new OrthographicCamera();
    viewport = new FillViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
    camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);
    this.config = config;

    batch = new SpriteBatch();
    shapeRenderer = new ShapeRenderer();

    Molecule[] molecules = new Molecule[config.simulation.numberOfMolecules()];

    physics = new Physics(config, molecules);
    physics.fillGrid();
      try {
          logger = new PhysicsLogger("dataset");
      } catch (IOException e) {
          throw new RuntimeException(e);
      }
      Gdx.input.setInputProcessor(
        new InputAdapter() {
          @Override
          public boolean touchDragged(int screenX, int screenY, int pointer) {
            float deltaX = -Gdx.input.getDeltaX() * camera.zoom;
            float deltaY = Gdx.input.getDeltaY() * camera.zoom;
            camera.translate(deltaX, deltaY);
            return true;
          }

          @Override
          public boolean scrolled(float amountX, float amountY) {
            float zoomFactor = 1.1f;
            if (amountY > 0) {
              camera.zoom *= zoomFactor;
            } else if (amountY < 0) {
              camera.zoom /= zoomFactor;
            }

            camera.zoom = MathUtils.clamp(camera.zoom, 0.3f, 100f);
            return true;
          }
        });
  }

  @Override
  public void render() {
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    camera.update();
    shapeRenderer.setProjectionMatrix(camera.combined);
    batch.setProjectionMatrix(camera.combined);
    /*float frameTime = FIXED_TIME_STEP;
    accumulator += frameTime;
    double pressure = 0;
    int iterations = 0;
    System.out.println(accumulator + " " + accumulator / config.simulation.timeStep());
    while (accumulator >= config.simulation.timeStep()){
        physics.applyPhysics(config.simulation.timeStep());
        //pressure += physics.calculatePressure(config.simulation.timeStep());
        physics.collisions();
        physics.handleCollisionsWithWalls();
        accumulator -= config.simulation.timeStep();
        ++iterations;
    }

    double totalSimTime = iterations * config.simulation.timeStep();
    pressure = (physics.getAccumulatedImpulse() / totalSimTime) / (2 * (config.vessel.width() + config.vessel.height()));
    double temp = physics.calcTemp();
    double r = physics.calcR(pressure, temp);
    System.out.println("Pressure: " + pressure + ", T: " + temp + ", R: " + r + " " + physics.getMolecules()[0].getPosition());*/
    for (int i = 0; i < config.simulation.stepsPerFrame(); i++) {
      if (beginToIncreaseArea){
        ++thermostatSteps;
        physics.turnOnWallMoving();
        physics.moveWall(config.simulation.timeStep());
      }

      physics.applyPhysics(config.simulation.timeStep());
      double pressure = physics.calculatePressure(config.simulation.timeStep());
      double curTemp = physics.calcTemp();
      double area = physics.calcArea();

      try {
        logger.logPT(pressure, curTemp);
        logger.logPV(pressure, area);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      System.out.println("Pressure: " + pressure + " Temp: " + curTemp + " Area: " + area);
      if (curTemp < config.simulation.targetTemp() && !beginToIncreaseArea) {
        physics.heatStep(config.simulation.timeStep() * config.simulation.tempRatePerSecond());
      } else {
          beginToIncreaseArea = true;
      }
      physics.collisions();
      physics.handleCollisionsWithWalls();
      if (thermostatSteps > config.simulation.thermostatStepsToApply() ) {
          physics.applyThermostat(config.simulation.targetTemp());
          thermostatSteps = 0;
      }
    }

    drawVessel();

    drawMolecules(physics.getMolecules());
  }

  private void drawVessel() {
    shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
    shapeRenderer.setColor(Color.WHITE);
    shapeRenderer.rect(
        config.vessel.position().x * RENDER_SCALE,
        config.vessel.position().y * RENDER_SCALE,
        physics.getWidth() * RENDER_SCALE,
        config.vessel.height() * RENDER_SCALE);
    shapeRenderer.end();
  }

  private void drawMolecules(Molecule[] molecules) {
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    for (int i = 0; i < molecules.length; i+= config.simulation.visibleMoleculesStep()) {
      drawMolecule(molecules[i]);
    }

    shapeRenderer.end();
  }

  private void drawMolecule(Molecule molecule) {
    Vector2 position = molecule.getPosition().cpy().scl(RENDER_SCALE);
    Vector2 velocity = molecule.getVelocity();

    float renderDiameter = config.molecule.diameter() * RENDER_SCALE;
    renderDiameter = Math.max(renderDiameter, 3f);

    float atomDistance = renderDiameter * 1.2f;

    Vector2 bondDirection = new Vector2(velocity.y, -velocity.x).nor();
    if (bondDirection.len() < 0.1f) {
      bondDirection.set(1, 0).nor();
    }

    Vector2 atom1Pos = new Vector2(position).mulAdd(bondDirection, -atomDistance * 0.5f);
    Vector2 atom2Pos = new Vector2(position).mulAdd(bondDirection, atomDistance * 0.5f);

    shapeRenderer.setColor(Color.LIGHT_GRAY);
    shapeRenderer.rectLine(atom1Pos, atom2Pos, renderDiameter * 0.15f);

    shapeRenderer.setColor(Color.LIGHT_GRAY);
    shapeRenderer.circle(atom1Pos.x, atom1Pos.y, renderDiameter * 0.3f);
    shapeRenderer.circle(atom2Pos.x, atom2Pos.y, renderDiameter * 0.3f);
  }

  @Override
  public void resize(int width, int height) {
    viewport.update(width, height);
  }

  @Override
  public void dispose() {
    try {
      logger.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    batch.dispose();
    shapeRenderer.dispose();
  }
}
