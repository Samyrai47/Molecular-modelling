package org.mipt;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.google.gson.Gson;
import org.mipt.entity.Molecule;
import org.mipt.entity.SimulationConfig;

public class Main extends ApplicationAdapter {
  private Physics physics;

  private Molecule molecules[];

  @Override
  public void create() {
    Gson gson = new Gson();
    FileHandle file = Gdx.files.internal("config/simulation.json");
    SimulationConfig config = gson.fromJson(file.reader(), SimulationConfig.class);
    molecules = new Molecule[config.simulation.numberOfMolecules()];

    physics = new Physics(config, molecules);
    physics.fillGrid();

    Molecule hydrogenMolecule = new Molecule(config.molecule, 0, 0, new Vector2(0, 0));
  }

  @Override
  public void render() {
    physics.updateGrid();
  }

  @Override
  public void dispose() {}
}
