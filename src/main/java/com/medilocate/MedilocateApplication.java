package com.medilocate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medilocate.entity.Doctor;
import com.medilocate.entity.enums.DoctorStatus;
import com.medilocate.entity.enums.Specialty;
import com.medilocate.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Map;

@SpringBootApplication
@RequiredArgsConstructor
@EnableJpaAuditing
@EnableScheduling
@EnableAsync
public class MedilocateApplication implements CommandLineRunner {

	private final DoctorService doctorService;

	private final ObjectMapper objectMapper;

	public static void main(String[] args) {
		SpringApplication.run(MedilocateApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		String availability = "{"
				+ "\"Monday\":\"9:00-17:00\","
				+ "\"Tuesday\":\"9:00-17:00\","
				+ "\"Wednesday\":\"9:00-17:00\","
				+ "\"Thursday\":\"9:00-17:00\","
				+ "\"Friday\":\"9:00-17:00\","
				+ "\"Saturday\":\"10:00-14:00\""
				+ "}";
//		Map<String, String> availabilityMap = objectMapper.readValue(availability, Map.class);

//		Doctor doctor1 = new Doctor();
//		doctor1.setName("Park");
//		doctor1.setSpecialty(Specialty.CARDIOLOGIST);
//		doctor1.setAvailability(availabilityMap);
//		doctor1.setLatitude(29.333914);
//		doctor1.setLongitude(76.985632);
//		doctor1.setCity("Panipat");
//		doctor1.setStatus(DoctorStatus.AVAILABLE);

	//	doctorService.saveDoctor(doctor1);
//
//		Doctor doctor2 = new Doctor();
//		doctor2.setName("Healing Heart");
//		doctor2.setSpecialty(Specialty.NEUROLOGIST);
//		doctor2.setAvailability(availabilityMap);
//		doctor2.setLatitude(29.248175);
//		doctor2.setLongitude(77.008577);
//		doctor2.setCity("Panipat");
//		doctor2.setStatus(DoctorStatus.AVAILABLE);
//
//		doctorService.saveDoctor(doctor2);
	}

}
