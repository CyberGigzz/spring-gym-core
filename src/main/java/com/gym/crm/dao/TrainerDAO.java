package com.gym.crm.dao;

import com.gym.crm.model.Trainer;
import com.gym.crm.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public class TrainerDAO {

    private Storage storage;

    @Autowired
    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public Trainer save(Trainer trainer) {
        storage.getTrainerStorage().put(trainer.getId(), trainer);
        return trainer;
    }

    public Optional<Trainer> findById(Long id) {
        return Optional.ofNullable(storage.getTrainerStorage().get(id));
    }

    public Collection<Trainer> findAll() {
        return storage.getTrainerStorage().values();
    }

}