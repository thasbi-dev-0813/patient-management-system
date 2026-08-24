package com.jfs.training.service;

import org.springframework.stereotype.Service;

import com.jfs.training.dto.PatientRequestDTO;
import com.jfs.training.entity.Patient;
import com.jfs.training.exception.PatientNotFoundException;
import com.jfs.training.repository.PatientRepository;

import java.util.List;

@Service
public class PatientService {

	private final PatientRepository patientRepository;

	public PatientService(PatientRepository patientRepository) {
	    this.patientRepository = patientRepository;
	}

    public Patient registerPatient(PatientRequestDTO patientDTO) {

        Patient patient = new Patient();

        patient.setName(patientDTO.getName());
        patient.setEmail(patientDTO.getEmail());
        patient.setPhone(patientDTO.getPhone());
        patient.setDateOfBirth(patientDTO.getDateOfBirth());
        patient.setGender(patientDTO.getGender());
        patient.setAddress(patientDTO.getAddress());

        return patientRepository.save(patient);
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public Patient getPatientById(Long id) {

        return patientRepository.findById(id)
                .orElseThrow(() ->
                        new PatientNotFoundException(
                                "Patient not found with id: " + id));
    }

    public Patient updatePatient(Long id, PatientRequestDTO patientDTO) {

        Patient existingPatient = getPatientById(id);

        existingPatient.setName(patientDTO.getName());
        existingPatient.setEmail(patientDTO.getEmail());
        existingPatient.setPhone(patientDTO.getPhone());
        existingPatient.setDateOfBirth(patientDTO.getDateOfBirth());
        existingPatient.setGender(patientDTO.getGender());
        existingPatient.setAddress(patientDTO.getAddress());

        return patientRepository.save(existingPatient);
    }

    public void deletePatient(Long id) {

        Patient existingPatient = getPatientById(id);

        patientRepository.delete(existingPatient);
    }

    public List<Patient> searchPatientsByName(String name) {
        return patientRepository.findByNameContainingIgnoreCase(name);
    }
}