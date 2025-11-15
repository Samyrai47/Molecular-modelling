package org.mipt.dto;

public record SimulationSettings(
    int numberOfMolecules, float timeStep, float temperature, int clusterSize, int stepsPerFrame, int visibleMoleculesStep) {}
