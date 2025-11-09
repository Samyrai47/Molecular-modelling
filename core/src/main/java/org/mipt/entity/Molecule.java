package org.mipt.entity;

import com.badlogic.gdx.math.Vector3;

public class Molecule {
    private double mass;
    private int numberOfAtoms;
    private int degreesOfFreedom;
    private float velocity;
    private float kineticEnergy;
    private Vector3 position;

    public Molecule(double mass, int numberOfAtoms, int degreesOfFreedom, float velocity, float kineticEnergy, Vector3 position) {
        this.mass = mass;
        this.numberOfAtoms = numberOfAtoms;
        this.degreesOfFreedom = degreesOfFreedom;
        this.velocity = velocity;
        this.kineticEnergy = kineticEnergy;
        this.position = position;
    }
}
