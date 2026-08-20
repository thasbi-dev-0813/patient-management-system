package com.jfs.training.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jfs.training.entity.Patient;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

}