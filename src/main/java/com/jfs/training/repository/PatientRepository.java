package com.jfs.training.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jfs.training.entity.Patient;


public interface PatientRepository extends JpaRepository<Patient, Long> {

	List<Patient> findByNameContainingIgnoreCase(String name);
}