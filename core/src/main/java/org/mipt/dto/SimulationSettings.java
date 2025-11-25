package org.mipt.dto;

public record SimulationSettings(
    int numberOfMolecules, float timeStep, float temperature, float targetTemp, int clusterSize, int stepsPerFrame, int visibleMoleculesStep, double tempRatePerSecond, int thermostatStepsToApply, double power) {}
