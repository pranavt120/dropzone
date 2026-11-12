package com.flashsale.service;

import com.flashsale.domain.Drops;
import com.flashsale.exceptions.DropsNotFoundException;
import com.flashsale.repo.DropsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DropsService {

    private final DropsRepository dropsRepository;

    public DropsService(DropsRepository dropsRepository) {
        this.dropsRepository = dropsRepository;
    }

    @Transactional(readOnly = true)
    public Drops findById(UUID id) {
        return this.dropsRepository.findById(id).orElseThrow(() -> new DropsNotFoundException("No drop found for id: " + id));
    }

}
