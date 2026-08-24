package com.jfs.training.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.jfs.training.dto.PatientRequestDTO;
import com.jfs.training.entity.Patient;
import com.jfs.training.service.PatientService;

import java.util.List;

import javax.validation.Valid;

@RestController
@RequestMapping("/patients")
@CrossOrigin(origins = "*")
public class PatientController {

	private final PatientService patientService;

	public PatientController(PatientService patientService) {
	    this.patientService = patientService;
	}

	 @PostMapping("/registerPatient")
	    public ResponseEntity<Patient> registerPatient(
	            @Valid @RequestBody PatientRequestDTO patientDTO) {

	        Patient savedPatient =
	                patientService.registerPatient(patientDTO);

	        return new ResponseEntity<>(
	                savedPatient,
	                HttpStatus.CREATED
	        );
	    }

    @GetMapping("/getAllPatients")
    public ResponseEntity<List<Patient>> getAllPatients() {

        return ResponseEntity.ok(
                patientService.getAllPatients()
        );
    }

    @GetMapping("/getPatientById/{id}")
    public ResponseEntity<Patient> getPatientById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                patientService.getPatientById(id)
        );
    }

    @PutMapping("/updatePatient/{id}")
    public ResponseEntity<Patient> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientRequestDTO patientDTO) {

        return ResponseEntity.ok(
                patientService.updatePatient(id, patientDTO)
        );
    }

    @DeleteMapping("/deletePatient/{id}")
    public ResponseEntity<Void> deletePatient(
            @PathVariable Long id) {

        patientService.deletePatient(id);

        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/searchByPatientName")
    public ResponseEntity<List<Patient>> searchPatients(
            @RequestParam String name) {

        return ResponseEntity.ok(
                patientService.searchPatientsByName(name)
        );
    }
}