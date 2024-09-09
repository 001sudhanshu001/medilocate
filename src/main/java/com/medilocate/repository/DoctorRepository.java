package com.medilocate.repository;

import com.medilocate.entity.Doctor;
import com.medilocate.entity.enums.Specialty;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    // TODO : Pagination
    @Query("SELECT d, " +
            "(6371 * acos(cos(radians(:userLatitude)) * cos(radians(d.latitude)) * cos(radians(d.longitude) - radians(:userLongitude)) + sin(radians(:userLatitude)) * sin(radians(d.latitude)))) AS distance " +
            "FROM Doctor d " +
            "WHERE d.specialty = :specialty " +
            "AND (6371 * acos(cos(radians(:userLatitude)) * cos(radians(d.latitude)) * cos(radians(d.longitude) - radians(:userLongitude)) + sin(radians(:userLatitude)) * sin(radians(d.latitude)))) <= :radius")
    List<Object[]> findClosestDoctors(@Param("userLatitude") double userLatitude,
                                      @Param("userLongitude") double userLongitude,
                                      @Param("specialty") Specialty specialty,
                                      @Param("radius") double radius,
                                      Pageable pageable);

    @Query("SELECT d FROM Doctor d WHERE d.name ILIKE %?1% ORDER BY similarity(d.name, ?1) DESC")
    List<Doctor> findByNameContaining(String name, Pageable pageable);

    List<Doctor> findByCityIgnoreCaseAndSpecialty(String city, Specialty specialty, Pageable pageable);

    Optional<Doctor> findByEmail(String email);

    /*
     @Query(value = "SELECT *, " +
            "(6371 * acos(cos(radians(:userLatitude)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:userLongitude)) + sin(radians(:userLatitude)) * sin(radians(latitude)))) AS distance " +
            "FROM doctor " +
            "WHERE specialty = :specialty " +
            "AND (6371 * acos(cos(radians(:userLatitude)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:userLongitude)) + sin(radians(:userLatitude)) * sin(radians(latitude)))) <= :radius " +
            "ORDER BY distance",
            countQuery = "SELECT count(*) " +
                    "FROM doctor " +
                    "WHERE specialty = :specialty " +
                    "AND (6371 * acos(cos(radians(:userLatitude)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:userLongitude)) + sin(radians(:userLatitude)) * sin(radians(latitude)))) <= :radius",
            nativeQuery = true)
     Page<Object[]> findClosestDoctors(@Param("userLatitude") double userLatitude,
                                       @Param("userLongitude") double userLongitude,
                                       @Param("specialty") Specialty specialty,
                                       @Param("radius") double radius,
                                       Pageable pageable);

     */
}

