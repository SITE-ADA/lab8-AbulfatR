package az.edu.ada.wm2.studentservice.controller;

import az.edu.ada.wm2.studentservice.model.dto.StudentRequestDto;
import az.edu.ada.wm2.studentservice.model.dto.StudentResponseDto;
import az.edu.ada.wm2.studentservice.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
@Tag(name = "Tələbələr", description = "Tələbələrin idarə edilməsi üçün əməliyyatlar")
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    @Operation(summary = "Yeni tələbə yarat",
            description = "Sistemə yeni tələbə qeydiyyatdan keçirir.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tələbə uğurla yaradıldı"),
            @ApiResponse(responseCode = "400", description = "Yanlış məlumat daxil edilib")
    })
    public ResponseEntity<StudentResponseDto> createStudent(@Valid @RequestBody StudentRequestDto requestDto) {
        return new ResponseEntity<>(studentService.createStudent(requestDto), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Bütün tələbələri gətir",
            description = "Sistemdəki bütün tələbələrin siyahısını qaytarır.")
    @ApiResponse(responseCode = "200", description = "Tələbə siyahısı uğurla qaytarıldı")
    public ResponseEntity<List<StudentResponseDto>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @GetMapping("/{id}")
    @Operation(summary = "ID ilə tələbə tap",
            description = "Verilmiş ID-yə görə tələbənin məlumatlarını qaytarır.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tələbə tapıldı"),
            @ApiResponse(responseCode = "404", description = "Tələbə tapılmadı")
    })
    public ResponseEntity<StudentResponseDto> getStudentById(
            @Parameter(description = "Tələbənin unikal ID-si", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Tələbəni yenilə",
            description = "Mövcud tələbənin məlumatlarını yeniləyir.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tələbə uğurla yeniləndi"),
            @ApiResponse(responseCode = "404", description = "Tələbə tapılmadı")
    })
    public ResponseEntity<StudentResponseDto> updateStudent(
            @Parameter(description = "Tələbənin unikal ID-si", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody StudentRequestDto requestDto) {
        return ResponseEntity.ok(studentService.updateStudent(id, requestDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Tələbəni sil",
            description = "Verilmiş ID-yə görə tələbəni sistemdən silir.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tələbə uğurla silindi"),
            @ApiResponse(responseCode = "404", description = "Tələbə tapılmadı")
    })
    public ResponseEntity<Void> deleteStudent(
            @Parameter(description = "Tələbənin unikal ID-si", example = "1")
            @PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Ada görə tələbə axtar",
            description = "Tələbənin adı və ya soyadı ilə axtarış aparır. Hərflərə həssas deyil.")
    @ApiResponse(responseCode = "200", description = "Uyğun tələbələr qaytarıldı")
    public ResponseEntity<List<StudentResponseDto>> searchByName(
            @Parameter(description = "Axtarış üçün ad və ya soyad", example = "Nicat")
            @RequestParam String name) {
        return ResponseEntity.ok(studentService.searchByName(name));
    }
}