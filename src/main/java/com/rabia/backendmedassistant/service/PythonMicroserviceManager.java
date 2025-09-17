package com.rabia.backendmedassistant.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class PythonMicroserviceManager {

    private final List<Process> processes = new ArrayList<>();

    @PostConstruct
    public void startServices() {
        System.out.println("Starting Python microservices...");
        startService("ai-microservices/diagnosis-service/app.py", "Diagnosis-Service");
        startService("ai-microservices/rag-service/app.py", "RAG-Service");
        System.out.println("Python microservices startup initiated.");
    }

    private void startService(String scriptPath, String serviceName) {
        try {
            ProcessBuilder pb = new ProcessBuilder("python", scriptPath);
            pb.directory(new File(System.getProperty("user.dir")));
            pb.redirectErrorStream(true);
            pb.inheritIO(); // Redirects the subprocess's output to the main process's console
            Process process = pb.start();
            processes.add(process);
            System.out.println(serviceName + " started successfully.");
        } catch (IOException e) {
            System.err.println("Failed to start " + serviceName + " at " + scriptPath);
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void stopServices() {
        System.out.println("Stopping Python microservices...");
        for (Process process : processes) {
            if (process.isAlive()) {
                process.destroyForcibly(); // Forcefully terminate the process
            }
        }
        processes.clear(); // Clear the list of processes
        System.out.println("Python microservices stopped.");
    }
}
