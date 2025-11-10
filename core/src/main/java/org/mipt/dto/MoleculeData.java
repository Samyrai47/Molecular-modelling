package org.mipt.dto;

public record MoleculeData(
    float mass, int numberOfAtoms, int degreesOfFreedom, float diameter, float halfBoundLength) {}
