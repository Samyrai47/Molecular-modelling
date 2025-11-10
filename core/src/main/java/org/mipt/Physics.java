package org.mipt;

import com.badlogic.gdx.math.Intersector;
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
  private final double SCALE = 1E10;

  public Physics() {}

  public Physics(SimulationConfig config, Molecule[] molecules) {
    this.config = config;
    this.gridWidth = (int) (config.vessel.width() / (config.molecule.diameter() * SCALE));
    this.gridHeight = (int) (config.vessel.height() / (config.molecule.diameter() * SCALE));
    this.cellSize = new int[gridHeight * gridWidth];
    this.grid = new Molecule[gridWidth * gridHeight * MAX_CELL_CAPACITY];
    this.molecules = molecules;
  }

  public void fillGrid() {
    for (int i = 0; i < molecules.length; i++) {
      int x = (int) (molecules[i].getPosition().x / (config.molecule.diameter() * SCALE));
      int y = (int) (molecules[i].getPosition().y / (config.molecule.diameter() * SCALE));

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

      if (countMoleculesInCell < MAX_CELL_CAPACITY) {
        int slot = cell * MAX_CELL_CAPACITY + countMoleculesInCell;
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
          Molecule a = grid[i * MAX_CELL_CAPACITY + j];
          Molecule b = grid[i * MAX_CELL_CAPACITY + k];
          if (isColliding(a, b)) resolveCollision(a, b);
        }

        Molecule a = grid[i * MAX_CELL_CAPACITY + j];

        // правая клетка
        if (col + 1 < cols) {
          for (int k = 0; k < cellSize[i + 1]; k++) {
            Molecule b = grid[(i + 1) * MAX_CELL_CAPACITY + k];
            if (isColliding(a, b)) resolveCollision(a, b);
          }
        }

        // нижняя клетка
        if (row + 1 < rows) {
          for (int k = 0; k < cellSize[i + cols]; k++) {
            Molecule b = grid[(i + cols) * MAX_CELL_CAPACITY + k];
            if (isColliding(a, b)) resolveCollision(a, b);
          }
        }

        // правая-нижняя клетка
        if (col + 1 < cols && row + 1 < rows) {
          for (int k = 0; k < cellSize[i + cols + 1]; k++) {
            Molecule b = grid[(i + cols + 1) * MAX_CELL_CAPACITY + k];
            if (isColliding(a, b)) resolveCollision(a, b);
          }
        }

        // правая-верхняя клетка
        if (col + 1 < cols && row - 1 >= 0) {
          for (int k = 0; k < cellSize[i - cols + 1]; k++) {
            Molecule b = grid[(i - cols + 1) * MAX_CELL_CAPACITY + k];
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
                (grid[i * MAX_CELL_CAPACITY + j].getPosition().x
                    / (config.molecule.diameter() * SCALE));
        int y =
            (int)
                (grid[i * MAX_CELL_CAPACITY + j].getPosition().y
                    / (config.molecule.diameter() * SCALE));

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
    return dist < ((first.getDiameter() + second.getDiameter()) * SCALE / 2);
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

    Vector2 relativeVelocity = v1.cpy().sub(v2);

    float velocityAlongNormal = relativeVelocity.dot(normal);

    if (velocityAlongNormal > 0) {
      return;
    }

    float m1 = first.getMass();
    float m2 = second.getMass();

    float impulseScalar = -2 * velocityAlongNormal / (1 / m1 + 1 / m2);

    Vector2 impulse = normal.cpy().scl(impulseScalar);

    Vector2 v1New = v1.cpy().add(impulse.cpy().scl(1 / m1));
    Vector2 v2New = v2.cpy().sub(impulse.cpy().scl(1 / m2));

    first.setVelocity(v1New);
    second.setVelocity(v2New);
    first.updateKineticEnergy();
    second.updateKineticEnergy();
  }
}
