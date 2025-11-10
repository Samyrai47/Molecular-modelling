package org.mipt.entity;

import com.badlogic.gdx.math.Vector2;
import org.mipt.dto.MoleculeData;

public class Molecule {
  private float mass;
  private int numberOfAtoms;
  private int degreesOfFreedom;
  private float diameter;
  private float halfBoundLength;
  private Vector2 velocity;
  private float kineticEnergy;
  private Vector2 position;
  private Vector2 direction;

  public Molecule() {}

  public Molecule(
      float mass,
      int numberOfAtoms,
      int degreesOfFreedom,
      float diameter,
      float halfBoundLength,
      Vector2 velocity,
      float kineticEnergy,
      Vector2 position) {
    this.mass = mass;
    this.numberOfAtoms = numberOfAtoms;
    this.degreesOfFreedom = degreesOfFreedom;
    this.diameter = diameter;
    this.halfBoundLength = halfBoundLength;
    this.velocity = velocity;
    this.kineticEnergy = kineticEnergy;
    this.position = position;
    this.direction = new Vector2(1, 0);
  }

  public Molecule(MoleculeData data, Vector2 velocity, float kineticEnergy, Vector2 position) {
    this.mass = data.mass();
    this.numberOfAtoms = data.numberOfAtoms();
    this.degreesOfFreedom = data.degreesOfFreedom();
    this.diameter = data.diameter();
    this.halfBoundLength = data.halfBoundLength();
    this.velocity = velocity;
    this.kineticEnergy = kineticEnergy;
    this.position = position;
    this.direction = new Vector2(1, 0);
  }

  public float getMass() {
    return mass;
  }

  public int getNumberOfAtoms() {
    return numberOfAtoms;
  }

  public int getDegreesOfFreedom() {
    return degreesOfFreedom;
  }

  public float getDiameter() {
    return diameter;
  }

  public Vector2 getVelocity() {
    return velocity;
  }

  public float getKineticEnergy() {
    return kineticEnergy;
  }

  public Vector2 getPosition() {
    return position;
  }

  public float getHalfBoundLength() {
    return halfBoundLength;
  }

  public Vector2 getDirection() {
    return direction;
  }

  public void setVelocity(Vector2 velocity) {
    this.velocity = velocity;
  }

  public void updateKineticEnergy() {
    this.kineticEnergy = 0.5f * mass * velocity.len2();
  }
}
