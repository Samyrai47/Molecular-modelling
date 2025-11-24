package org.mipt.entity;

import com.badlogic.gdx.math.Vector2;
import org.mipt.dto.VesselData;

public class Vessel {
  private float width;
  private float height;
  private Vector2 position;

  public Vessel(VesselData data) {
    this.width = data.width();
    this.height = data.height();
    this.position = data.position();
  }
}
