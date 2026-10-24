package com.sentinel.ledger.controller;

import com.sentinel.ledger.dto.CreateStudentRequest;
import com.sentinel.ledger.dto.StudentDto;
import com.sentinel.ledger.dto.StudentHistoryDto;
import com.sentinel.ledger.dto.UpdateStudentRequest;
import com.sentinel.ledger.enums.AdmissionCategory;
import com.sentinel.ledger.enums.AdmissionStatus;
import com.sentinel.ledger.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@Tag(name = "Student Management", description = "Student records CRUD, history tracking, and institution-scoped querying")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    @Operation(summary = "Search and filter student records with pagination")
    public ResponseEntity<Page<StudentDto>> getStudents(
            @RequestParam(required = false) Long institutionId,
            @RequestParam(required = false) String program,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) AdmissionCategory category,
            @RequestParam(required = false) AdmissionStatus status,
            @RequestParam(required = false) Boolean credentialIssued,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        String[] sortParams = sort.split(",");
        Sort sortObj = Sort.by(sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortParams[0]);

        Page<StudentDto> students = studentService.getStudents(
                institutionId, program, academicYear, category, status, credentialIssued, query, PageRequest.of(page, size, sortObj));
        return ResponseEntity.ok(students);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed student profile by internal database ID")
    public ResponseEntity<StudentDto> getStudentById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new student record")
    public ResponseEntity<StudentDto> createStudent(@Valid @RequestBody CreateStudentRequest request) {
        StudentDto created = studentService.createStudent(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing student record with history audit")
    public ResponseEntity<StudentDto> updateStudent(@PathVariable Long id, @RequestBody UpdateStudentRequest request) {
        return ResponseEntity.ok(studentService.updateStudent(id, request));
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Get historical revisions and changes for a student record")
    public ResponseEntity<List<StudentHistoryDto>> getStudentHistory(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentHistory(id));
    }
}
