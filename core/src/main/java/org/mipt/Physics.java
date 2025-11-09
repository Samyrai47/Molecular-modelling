package org.mipt;

import java.util.Arrays;
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
}
