package org.mipt;

import java.io.FileWriter;
import java.io.IOException;

public class PhysicsLogger {
    private final FileWriter procPTWriter;
    private final FileWriter procPVWriter;

    public  PhysicsLogger(String fileName) throws IOException {
        procPTWriter = new FileWriter(fileName + "_PT.csv");
        procPVWriter = new FileWriter(fileName + "_PV.csv");
        procPTWriter.write("P,T\n");
        procPVWriter.write("P,V\n");
    }

    public void logPT(double pressure, double temperature) throws IOException {
        procPTWriter.write(Double.toString(pressure));
        procPTWriter.write(',');
        procPTWriter.write(Double.toString(temperature));
        procPTWriter.write('\n');
    }

    public void logPV(double pressure, double area) throws IOException {
        procPVWriter.write(Double.toString(pressure));
        procPVWriter.write(',');
        procPVWriter.write(Double.toString(area));
        procPVWriter.write('\n');
    }

    public void close() throws IOException {
        procPTWriter.close();
    }
}
