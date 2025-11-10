package org.mipt.entity;

import com.badlogic.gdx.math.Vector2;
import org.mipt.dto.MoleculeData;

public class Molecule {
  private double mass;
  private int numberOfAtoms;
  private int degreesOfFreedom;
  private double diameter;
  private Vector2 velocity;
  private float kineticEnergy;
  private Vector2 position;

  public Molecule() {}

  public Molecule(
      double mass,
      int numberOfAtoms,
      int degreesOfFreedom,
      double diameter,
      Vector2 velocity,
      float kineticEnergy,
      Vector2 position) {
    this.mass = mass;
    this.numberOfAtoms = numberOfAtoms;
    this.degreesOfFreedom = degreesOfFreedom;
    this.diameter = diameter;
    this.velocity = velocity;
    this.kineticEnergy = kineticEnergy;
    this.position = position;
  }

  public Molecule(MoleculeData data, Vector2 velocity, float kineticEnergy, Vector2 position) {
    this.mass = data.mass();
    this.numberOfAtoms = data.numberOfAtoms();
    this.degreesOfFreedom = data.degreesOfFreedom();
    this.diameter = data.diameter();
    this.velocity = velocity;
    this.kineticEnergy = kineticEnergy;
    this.position = position;
  }

  public double getMass() {
    return mass;
  }

  public int getNumberOfAtoms() {
    return numberOfAtoms;
  }

  public int getDegreesOfFreedom() {
    return degreesOfFreedom;
  }

  public double getDiameter() {
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

  public void setPosition(Vector2 position) {
      this.position = position;
  }
}
