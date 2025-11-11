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

public class Main extends ApplicationAdapter {
  private Physics physics;

  private static final float WORLD_HEIGHT = 600;
  private static final float WORLD_WIDTH = 1000;

  private final float moleculeRenderScale = 1E10f;

  private OrthographicCamera camera;
  private SpriteBatch batch;
  private ShapeRenderer shapeRenderer;
  private SimulationConfig config;
  private FillViewport viewport;
  private float accumulator = 0f;

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

    float frameTime = Gdx.graphics.getDeltaTime();
    accumulator += frameTime;

    while (accumulator >= config.simulation.timeStep()) {
      physics.applyPhysics(config.simulation.timeStep());
      physics.collisions();
      accumulator -= config.simulation.timeStep();
    }

    drawVessel();

    drawMolecules(physics.getMolecules());
  }

  private void drawVessel() {
    shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
    shapeRenderer.setColor(Color.WHITE);
    shapeRenderer.rect(
        config.vessel.position().x,
        config.vessel.position().y,
        config.vessel.width(),
        config.vessel.height());
    shapeRenderer.end();
  }

  private void drawMolecules(Molecule[] molecules) {
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    for (Molecule molecule : molecules) {
      drawMolecule(molecule);
    }

    shapeRenderer.end();
  }

  private void drawMolecule(Molecule molecule) {
    Vector2 position = molecule.getPosition();
    Vector2 velocity = molecule.getVelocity();

    float renderDiameter = config.molecule.diameter() * moleculeRenderScale;
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

  private float calculateInitialSpeed(float temperature) {
    double k = 1.38e-23;
    double mass = config.molecule.mass();
    double speed = Math.sqrt(2 * k * temperature / mass);
    return (float) speed;
  }

  @Override
  public void resize(int width, int height) {
    viewport.update(width, height);
  }

  @Override
  public void dispose() {
    batch.dispose();
    shapeRenderer.dispose();
  }
}
