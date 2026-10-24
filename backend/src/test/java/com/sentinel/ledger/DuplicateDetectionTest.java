package com.sentinel.ledger;

import com.sentinel.ledger.repository.ReviewCaseRepository;
import com.sentinel.ledger.repository.StudentRepository;
import com.sentinel.ledger.service.DuplicateDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DuplicateDetectionTest {

    private DuplicateDetectionService duplicateDetectionService;
    private StudentRepository studentRepository;
    private ReviewCaseRepository reviewCaseRepository;

    @BeforeEach
    void setUp() {
        studentRepository = Mockito.mock(StudentRepository.class);
        reviewCaseRepository = Mockito.mock(ReviewCaseRepository.class);
        duplicateDetectionService = new DuplicateDetectionService(studentRepository, reviewCaseRepository);
    }

    @Test
    @DisplayName("Should normalize synthetic APAAR references correctly")
    void testNormalizeSyntheticId() {
        assertEquals("APAAR-SYN-2024-1001", duplicateDetectionService.normalizeSyntheticId(" apaar-syn-2024-1001 "));
        assertEquals("APAAR-SYN-2024-1001", duplicateDetectionService.normalizeSyntheticId("APAAR-SYN- 2024-1001"));
        assertEquals("", duplicateDetectionService.normalizeSyntheticId(null));
    }
}
