package de.uol.dssp.source;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@SpringBootApplication
public class Application {

    public static void main(String[] args) throws IOException, TimeoutException {
        System.out.println("Loading CSV data...");
        DataSingleton.getInstance();

        SpringApplication.run(Application.class, args);
    }

}
