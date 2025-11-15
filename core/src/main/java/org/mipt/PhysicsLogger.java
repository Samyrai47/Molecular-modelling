package org.mipt;

import java.io.FileWriter;
import java.io.IOException;

public class PhysicsLogger {
    private final FileWriter procPTWriter;

    public  PhysicsLogger(String fileName) throws IOException {
        procPTWriter = new FileWriter(fileName + "_PT.csv");
    }

    public void logPT(double pressure, double temperature) throws IOException {
        procPTWriter.write(Double.toString(pressure));
        procPTWriter.write(',');
        procPTWriter.write(Double.toString(temperature));
        procPTWriter.write('\n');
    }

    public void close() throws IOException {
        procPTWriter.close();
    }
}
