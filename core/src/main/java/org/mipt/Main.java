package org.mipt;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import org.mipt.entity.Molecule;

public class Main extends ApplicationAdapter {
    private Physics physics;

    @Override
    public void create() {
        Molecule hydrogenMolecule = new Molecule(3.35E-27, 2, 5, 0, 0, new Vector3(0, 0, 0));
    }

    @Override
    public void render() {
    }

    @Override
    public void dispose() {
    }
}
