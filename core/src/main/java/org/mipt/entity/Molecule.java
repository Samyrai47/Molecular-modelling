package org.mipt.entity;

import com.badlogic.gdx.math.Vector2;
import org.mipt.dto.MoleculeData;

public class Molecule {
  private double mass;
  private int numberOfAtoms;
  private int degreesOfFreedom;
  private double diameter;
  private float velocity;
  private float kineticEnergy;
  private Vector2 position;

  public Molecule() {}

  public Molecule(
      double mass,
      int numberOfAtoms,
      int degreesOfFreedom,
      double diameter,
      float velocity,
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

  public Molecule(MoleculeData data, float velocity, float kineticEnergy, Vector2 position) {
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

  public float getVelocity() {
    return velocity;
  }

  public float getKineticEnergy() {
    return kineticEnergy;
  }

  public Vector2 getPosition() {
    return position;
  }
}
