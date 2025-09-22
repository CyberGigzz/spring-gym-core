package com.gym.crm.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import jakarta.annotation.PostConstruct; 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class StorageInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageInitializer.class);

    private final Storage storage;
    private final ObjectMapper objectMapper;

    @Value("${initial.data.path}")
    private Resource dataFile;
    
    public StorageInitializer(Storage storage) {
        this.storage = storage;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @PostConstruct
    public void initialize() {
        try {
            if (dataFile.exists()) {
                LOGGER.info("Initializing storage from file: {}", dataFile.getFilename());
                JsonNode rootNode = objectMapper.readTree(dataFile.getInputStream());

                if (rootNode.has("trainees")) {
                    for (JsonNode node : rootNode.path("trainees")) {
                        Trainee trainee = objectMapper.treeToValue(node, Trainee.class);
                        trainee.setUsername(trainee.getFirstName().toLowerCase() + "." + trainee.getLastName().toLowerCase());
                        trainee.setPassword("InitialPass123"); 
                        storage.getTraineeStorage().put(trainee.getId(), trainee);
                    }
                }

                if (rootNode.has("trainers")) {
                    for (JsonNode node : rootNode.path("trainers")) {
                        Trainer trainer = objectMapper.treeToValue(node, Trainer.class);
                        trainer.setUsername(trainer.getFirstName().toLowerCase() + "." + trainer.getLastName().toLowerCase());
                        trainer.setPassword("InitialPass123");
                        storage.getTrainerStorage().put(trainer.getId(), trainer);
                    }
                }
                LOGGER.info("Storage initialized successfully with {} trainee(s) and {} trainer(s).", storage.getTraineeStorage().size(), storage.getTrainerStorage().size());
            } else {
                LOGGER.warn("Initial data file not found at '{}', starting with empty storage.", dataFile.getURI());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to initialize storage from data file", e);
        }
    }
}