package com.medilocate.repository;

import com.medilocate.entity.Doctor;
import com.medilocate.entity.DoctorSlotConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DoctorSlotConfigurationRepository extends JpaRepository<DoctorSlotConfiguration, Long> {
    List<DoctorSlotConfiguration> findByDoctor(Doctor doctor);
}
