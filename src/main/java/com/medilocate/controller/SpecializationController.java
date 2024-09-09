package com.medilocate.controller;

import com.medilocate.entity.enums.Specialty;
import com.medilocate.service.SpecializationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/specializations")
@RequiredArgsConstructor
public class SpecializationController {

    private final SpecializationService specializationService;

    @GetMapping
    public List<Specialty> getSpecializations() {
        return specializationService.getAllSpecializations();
    }
}
