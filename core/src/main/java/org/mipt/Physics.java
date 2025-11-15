package org.mipt;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import java.util.Random;
import org.mipt.entity.Molecule;
import org.mipt.entity.SimulationConfig;

public class Physics {
  private SimulationConfig config;
  private int gridWidth;
  private int gridHeight;
  private int[] cellSize;
  private Molecule[] grid;
  private Molecule[] molecules;
  private float epsilon = 0.1f;
  private final double k = 1.38e-23;
  private final double nAvogadro = 6.022E23;

  public Physics() {}

  public Physics(SimulationConfig config, Molecule[] molecules) {
    this.config = config;
    this.gridWidth = (int) (config.vessel.width() / (config.molecule.diameter()));
    this.gridHeight = (int) (config.vessel.height() / (config.molecule.diameter()));
    this.cellSize = new int[gridHeight * gridWidth];
    this.grid = new Molecule[gridWidth * gridHeight * config.simulation.clusterSize()];
    initializeMolecules(molecules);
    this.molecules = molecules;
    this.epsilon *=  config.molecule.diameter() / 2;
  }

  private void initializeMolecules(Molecule[] molecules) {
    Random random = new Random();

    for (int i = 0; i < molecules.length; i++) {
      float margin = config.molecule.diameter() * 5;
      Vector2 position =
          new Vector2(
              margin + random.nextFloat() * (config.vessel.width() - 2 * margin),
              margin + random.nextFloat() * (config.vessel.height() - 2 * margin));

      float initialSpeed = calculateInitialSpeed();
      float angle = random.nextFloat() * 2 * (float) Math.PI;
      Vector2 velocity =
          new Vector2(
              (float) Math.cos(angle) * initialSpeed, (float) Math.sin(angle) * initialSpeed);

      float kineticEnergy = 0.5f * config.molecule.mass() * velocity.len2();

      molecules[i] = new Molecule(config.molecule, velocity, kineticEnergy, position);
    }
  }

  private float calculateInitialSpeed() {
    double mass = config.molecule.mass();
    double speed = Math.sqrt(2 * k * config.simulation.temperature() / mass);
    return (float) speed;
  }

  public void applyPhysics(float dt) {
    for (Molecule molecule : molecules) {
      float x = molecule.getPosition().x;
      float y = molecule.getPosition().y;
      molecule.setPosition(
          new Vector2(x + molecule.getVelocity().x * dt, y + molecule.getVelocity().y * dt));
    }
  }

  public void fillGrid() {
    for (int i = 0; i < molecules.length; i++) {
      int x = (int) (molecules[i].getPosition().x / (config.molecule.diameter()));
      int y = (int) (molecules[i].getPosition().y / (config.molecule.diameter()));

      if (x < 0) {
        x = 0;
      } else if (x >= gridWidth) {
        x = gridWidth - 1;
      }
      if (y < 0) {
        y = 0;
      } else if (y >= gridHeight) {
        y = gridHeight - 1;
      }

      int cell = y * gridWidth + x;
      int countMoleculesInCell = cellSize[cell];

      if (countMoleculesInCell < config.simulation.clusterSize()) {
        int slot = cell * config.simulation.clusterSize() + countMoleculesInCell;
        grid[slot] = molecules[i];
        cellSize[cell] = countMoleculesInCell + 1;
      } else {
        System.out.println(i);
        throw new RuntimeException(
            "Bad starting distribution. Try to increase cell capacity or rearrange molecules");
      }
    }
  }

  public void collisions() {
    updateGrid();

    int cols = gridWidth;
    int rows = gridHeight;
    int cells = gridHeight * gridWidth;

    for (int i = 0; i < cells; i++) {
      int row = i / cols;
      int col = i % cols;

      for (int j = 0; j < cellSize[i]; j++) {
        // текущая клетка
        for (int k = j + 1; k < cellSize[i]; k++) {
          Molecule a = grid[i * config.simulation.clusterSize() + j];
          Molecule b = grid[i * config.simulation.clusterSize() + k];
          if (isColliding(a, b)) resolveCollision(a, b);
        }

        Molecule a = grid[i * config.simulation.clusterSize() + j];

        // правая клетка
        if (col + 1 < cols) {
          for (int k = 0; k < cellSize[i + 1]; k++) {
            Molecule b = grid[(i + 1) * config.simulation.clusterSize() + k];
            if (isColliding(a, b)) resolveCollision(a, b);
          }
        }

        // нижняя клетка
        if (row + 1 < rows) {
          for (int k = 0; k < cellSize[i + cols]; k++) {
            Molecule b = grid[(i + cols) * config.simulation.clusterSize() + k];
            if (isColliding(a, b)) resolveCollision(a, b);
          }
        }

        // правая-нижняя клетка
        if (col + 1 < cols && row + 1 < rows) {
          for (int k = 0; k < cellSize[i + cols + 1]; k++) {
            Molecule b = grid[(i + cols + 1) * config.simulation.clusterSize() + k];
            if (isColliding(a, b)) resolveCollision(a, b);
          }
        }

        // правая-верхняя клетка
        if (col + 1 < cols && row - 1 >= 0) {
          for (int k = 0; k < cellSize[i - cols + 1]; k++) {
            Molecule b = grid[(i - cols + 1) * config.simulation.clusterSize() + k];
            if (isColliding(a, b)) resolveCollision(a, b);
          }
        }
      }
    }
  }

  private void updateGrid() {
    for (int i = 0; i < gridWidth * gridHeight; i++) {
      for (int j = 0; j < cellSize[i]; j++) {
        int x =
            (int)
                (grid[i * config.simulation.clusterSize() + j].getPosition().x
                    / (config.molecule.diameter()));
        int y =
            (int)
                (grid[i * config.simulation.clusterSize() + j].getPosition().y
                    / (config.molecule.diameter()));

        if (x < 0) {
          x = 0;
        }
        if (y < 0) {
          y = 0;
        }
        if (x >= gridWidth) {
          x = gridWidth - 1;
        }
        if (y >= gridHeight) {
          y = gridHeight - 1;
        }

        if (y * gridWidth + x != i
            && cellSize[y * gridWidth + x] < config.simulation.clusterSize()) {
          int cell = y * gridWidth + x;
          grid[cell * config.simulation.clusterSize() + cellSize[cell]++] =
              grid[i * config.simulation.clusterSize() + j];
          grid[i * config.simulation.clusterSize() + j] =
              grid[i * config.simulation.clusterSize() + --cellSize[i]];
          j--;
        }
      }
    }
  }

  private boolean isColliding(Molecule first, Molecule second) {
    Vector2 a1 =
        first.getPosition().cpy().sub(first.getDirection().cpy().scl(first.getHalfBoundLength()));
    Vector2 a2 =
        first.getPosition().cpy().add(first.getDirection().cpy().scl(first.getHalfBoundLength()));
    Vector2 b1 =
        second
            .getPosition()
            .cpy()
            .sub(second.getDirection().cpy().scl(second.getHalfBoundLength()));
    Vector2 b2 =
        second
            .getPosition()
            .cpy()
            .add(second.getDirection().cpy().scl(second.getHalfBoundLength()));

    float dist = distanceBetweenSegments(a1, a2, b1, b2);
    return dist < ((first.getDiameter() + second.getDiameter()) / 2);
  }

  private float distanceBetweenSegments(Vector2 a1, Vector2 a2, Vector2 b1, Vector2 b2) {
    if (Intersector.intersectSegments(a1, a2, b1, b2, null)) {
      return 0;
    }

    float d1 = Intersector.distanceSegmentPoint(a1, a2, b1);
    float d2 = Intersector.distanceSegmentPoint(a1, a2, b2);
    float d3 = Intersector.distanceSegmentPoint(b1, b2, a1);
    float d4 = Intersector.distanceSegmentPoint(b1, b2, a2);

    return Math.min(Math.min(d1, d2), Math.min(d3, d4));
  }

  private void resolveCollision(Molecule first, Molecule second) {
    Vector2 v1 = first.getVelocity();
    Vector2 v2 = second.getVelocity();

    Vector2 center1 = first.getPosition();
    Vector2 center2 = second.getPosition();

    Vector2 normal = center2.cpy().sub(center1).nor();

    Vector2 relativeVelocity = v2.cpy().sub(v1);
    float velocityAlongNormal = relativeVelocity.dot(normal);

    if (velocityAlongNormal > 0) {
      return;
    }

    float m1 = first.getMass();
    float m2 = second.getMass();

    float impulseScalar = -2 * velocityAlongNormal / (1 / m1 + 1 / m2);
    Vector2 impulse = normal.cpy().scl(impulseScalar);

    Vector2 v1New = v1.cpy().sub(impulse.cpy().scl(1 / m1));
    Vector2 v2New = v2.cpy().add(impulse.cpy().scl(1 / m2));

    first.setVelocity(v1New);
    second.setVelocity(v2New);
    first.updateKineticEnergy();
    second.updateKineticEnergy();
  }

  public void handleCollisionsWithWalls() {
    for (Molecule molecule : molecules) {
      Vector2 position = molecule.getPosition();
      Vector2 velocity = molecule.getVelocity();

      if (position.x < config.vessel.position().x + epsilon
          || position.x + epsilon > config.vessel.position().x + config.vessel.width()) {
        velocity.x = -velocity.x;

        if (position.x < config.vessel.position().x) {
          position.x = config.vessel.position().x + epsilon;
        } else {
          position.x = config.vessel.position().x + config.vessel.width() - epsilon;
        }
      }

      if (position.y < config.vessel.position().y + epsilon
          || position.y + epsilon > config.vessel.position().y + config.vessel.height()) {
        velocity.y = -velocity.y;

        if (position.y < config.vessel.position().y) {
          position.y = config.vessel.position().y + epsilon;
        } else {
          position.y = config.vessel.position().y + config.vessel.height() - epsilon;
        }
      }
    }
  }

  public double calculatePressure(float deltaTime) {
    double totalImpulse = 0;
    for (Molecule molecule : molecules) {
      Vector2 position = molecule.getPosition();
      Vector2 velocity = molecule.getVelocity();
      double mass = molecule.getMass();

      if (position.x < config.vessel.position().x + epsilon
          || position.x + epsilon > config.vessel.position().x + config.vessel.width()) {
        totalImpulse += 2 * mass * Math.abs(velocity.x);
      }

      if (position.y < config.vessel.position().y + epsilon
          || position.y + epsilon > config.vessel.position().y + config.vessel.height()) {
        totalImpulse += 2 * mass * Math.abs(velocity.y);
      }
    }

    double totalForce = totalImpulse / deltaTime;
    double perimeter = 2 * (config.vessel.width() + config.vessel.height());
    double pressure = totalForce / perimeter;
    return pressure;
  }

  public double calcR(double pressure){
      return (pressure * config.vessel.width() * config.vessel.height() * nAvogadro) / (config.simulation.numberOfMolecules() * config.simulation.temperature());
  }

  public Molecule[] getMolecules() {
    return molecules;
  }
}
