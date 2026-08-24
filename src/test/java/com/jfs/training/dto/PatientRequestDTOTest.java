package com.jfs.training.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class PatientRequestDTOTest {

    @Test
    void testGettersAndSetters() {
        PatientRequestDTO dto = new PatientRequestDTO();

        LocalDate dateOfBirth = LocalDate.of(2000, 1, 1);

        dto.setName("John");
        dto.setEmail("john@example.com");
        dto.setPhone("9876543210");
        dto.setDateOfBirth(dateOfBirth);
        dto.setGender("Male");
        dto.setAddress("Chennai");

        assertEquals("John", dto.getName());
        assertEquals("john@example.com", dto.getEmail());
        assertEquals("9876543210", dto.getPhone());
        assertEquals(dateOfBirth, dto.getDateOfBirth());
        assertEquals("Male", dto.getGender());
        assertEquals("Chennai", dto.getAddress());
    }
}