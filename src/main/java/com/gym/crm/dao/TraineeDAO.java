package com.gym.crm.dao;

import com.gym.crm.model.Trainee;
import com.gym.crm.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository; 

import java.util.Collection;
import java.util.Optional;

@Repository
public class TraineeDAO {

    private Storage storage;

    @Autowired
    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public Trainee save(Trainee trainee) {
        storage.getTraineeStorage().put(trainee.getId(), trainee);
        return trainee;
    }

    public Optional<Trainee> findById(Long id) {
        return Optional.ofNullable(storage.getTraineeStorage().get(id));
    }

    public Collection<Trainee> findAll() {
        return storage.getTraineeStorage().values();
    }

    public void deleteById(Long id) {
        storage.getTraineeStorage().remove(id);
    }
}