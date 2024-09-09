package com.medilocate.service;

import com.medilocate.entity.enums.Specialty;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;

@Service
public class SpecializationService {

    public List<Specialty> getAllSpecializations() {
        return Arrays.asList(Specialty.values());
    }
}