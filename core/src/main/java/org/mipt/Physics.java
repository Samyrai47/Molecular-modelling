package org.mipt;

import java.util.Arrays;

import com.badlogic.gdx.math.Vector2;
import org.mipt.entity.Molecule;
import org.mipt.entity.SimulationConfig;

public class Physics {
  private SimulationConfig config;
  private int gridWidth;
  private int gridHeight;
  private final int MAX_CELL_CAPACITY = 4;
  private int[] cellSize;
  private Molecule[] grid;
  private Molecule[] molecules;

  public Physics() {}

  public Physics(SimulationConfig config, Molecule[] molecules) {
    this.config = config;
    this.gridWidth = (int) (config.vessel.width() / config.molecule.diameter());
    this.gridHeight = (int) (config.vessel.height() / config.molecule.diameter());
    this.cellSize = new int[gridHeight * gridWidth];
    Arrays.fill(cellSize, MAX_CELL_CAPACITY);
    this.grid = new Molecule[gridWidth * gridHeight * MAX_CELL_CAPACITY];
    this.molecules = molecules;
  }

  public void applyPhysics(float dt) {
      for (Molecule molecule : molecules) {
          float x = molecule.getPosition().x;
          float y = molecule.getPosition().y;
          molecule.setPosition(new Vector2(x + molecule.getVelocity().x * dt, y + molecule.getVelocity().y * dt));
      }
      handleCollisionsWithWalls(molecules, dt);
  }

  public void fillGrid() {
    for (int i = 0; i < molecules.length; i++) {
      int x = (int) (molecules[i].getPosition().x / config.molecule.diameter());
      int y = (int) (molecules[i].getPosition().y / config.molecule.diameter());

      int cell = y * gridWidth + x;
      int countMoleculesInCell = cellSize[cell];

      if (countMoleculesInCell < MAX_CELL_CAPACITY) {
        int slot = cell * MAX_CELL_CAPACITY + countMoleculesInCell;
        grid[slot] = molecules[i];
        cellSize[cell] = countMoleculesInCell + 1;
      } else {
        throw new RuntimeException("Bad starting distribution. Try to increase cell capacity or rearrange molecules");
      }
    }
  }

  public void updateGrid() {
    for (int i = 0; i < gridWidth * gridHeight; i++) {
      for (int j = 0; j < cellSize[i]; j++) {
        int x =
            (int) (grid[i * MAX_CELL_CAPACITY + j].getPosition().x / config.molecule.diameter());
        int y =
            (int) (grid[i * MAX_CELL_CAPACITY + j].getPosition().y / config.molecule.diameter());

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

        if (y * gridWidth + x != i && cellSize[y * gridWidth + x] < MAX_CELL_CAPACITY) {
          int cell = y * gridWidth + x;
          grid[cell * MAX_CELL_CAPACITY + cellSize[cell]++] = grid[i * MAX_CELL_CAPACITY + j];
          grid[i * MAX_CELL_CAPACITY + j] = grid[i * MAX_CELL_CAPACITY + --cellSize[i]];
        }
      }
    }
  }

  public void resolveCollisions() {

  }

  public double handleCollisionsWithWalls(Molecule[] molecules, float deltaTime) {
      double[] totalWallForces = new double[4];
      int[] totalWallCollisions = new int[4];
      for (Molecule molecule : molecules) {
          Vector2 position = molecule.getPosition();
          Vector2 velocity = molecule.getVelocity();
          double mass = molecule.getMass();

          float epsilon = 0.1f;
          if (position.x < config.vessel.position().x + epsilon || position.x + epsilon > config.vessel.position().x + config.vessel.width()) {
              velocity.x = -velocity.x;

              double force = 2 * mass * Math.abs(velocity.x) / deltaTime;
              if (position.x < config.vessel.position().x) {
                  position.x = config.vessel.position().x + epsilon;
                  totalWallCollisions[0] += 1;
                  totalWallForces[0] += force;
              } else {
                  position.x = config.vessel.position().x + config.vessel.width() - epsilon;
                  totalWallCollisions[1] += 1;
                  totalWallForces[1] += force;
              }
          }

          if (position.y < config.vessel.position().y + epsilon || position.y + epsilon > config.vessel.position().y + config.vessel.height()) {
              velocity.y = -velocity.y;

              double force = 2 * mass * Math.abs(velocity.y) / deltaTime;
              if (position.y < config.vessel.position().y) {
                  position.y = config.vessel.position().y + epsilon;
                  totalWallCollisions[2] += 1;
                  totalWallForces[2] += force;
              } else  {
                  position.y = config.vessel.position().y + config.vessel.height() - epsilon;
                  totalWallCollisions[3] += 1;
                  totalWallForces[3] += force;
              }
          }
      }
      double avgForceLeft = (totalWallCollisions[0] == 0 ? 0 : totalWallForces[0] / totalWallCollisions[0]);
      double avgForceRight = (totalWallCollisions[1] == 0 ? 0 : totalWallForces[1] / totalWallCollisions[1]);
      double avgForceBottom = (totalWallCollisions[2] == 0 ? 0 : totalWallForces[2] / totalWallCollisions[2]);
      double avgForceTop = (totalWallCollisions[3] == 0 ? 0 : totalWallForces[3] / totalWallCollisions[3]);

      return (avgForceLeft / config.vessel.width() + avgForceRight / config.vessel.width() +
              avgForceBottom / config.vessel.height() + avgForceTop / config.vessel.height());
  }

    public Molecule[] getMolecules() {
        return molecules;
    }
}
