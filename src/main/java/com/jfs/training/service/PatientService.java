package com.jfs.training.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jfs.training.entity.Patient;
import com.jfs.training.exception.PatientNotFoundException;
import com.jfs.training.repository.PatientRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class PatientService {

	@Autowired
    PatientRepository patientRepository;


	 public Patient registerPatient(Patient patient) {
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

    public Patient updatePatient(Long id, Patient patient) {

        Patient existingPatient = getPatientById(id);

        existingPatient.setName(patient.getName());
        existingPatient.setEmail(patient.getEmail());
        existingPatient.setPhone(patient.getPhone());
        existingPatient.setDateOfBirth(patient.getDateOfBirth());
        existingPatient.setGender(patient.getGender());
        existingPatient.setAddress(patient.getAddress());

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