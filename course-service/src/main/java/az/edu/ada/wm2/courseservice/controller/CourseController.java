package az.edu.ada.wm2.courseservice.controller;

import az.edu.ada.wm2.courseservice.model.dto.CourseRequestDto;
import az.edu.ada.wm2.courseservice.model.dto.CourseResponseDto;
import az.edu.ada.wm2.courseservice.model.dto.CourseStudentsResponseDto;
import az.edu.ada.wm2.courseservice.model.dto.EnrollmentResponseDto;
import az.edu.ada.wm2.courseservice.service.CourseService;
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
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@Tag(name = "Kurslar", description = "Kursların idarə edilməsi və tələbə qeydiyyatı əməliyyatları")
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    @Operation(summary = "Yeni kurs yarat",
            description = "Sistemə yeni kurs əlavə edir. İlkin şərt kursunu da təyin etmək mümkündür.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Kurs uğurla yaradıldı"),
            @ApiResponse(responseCode = "400", description = "Yanlış məlumat daxil edilib")
    })
    public ResponseEntity<CourseResponseDto> createCourse(@Valid @RequestBody CourseRequestDto requestDto) {
        return new ResponseEntity<>(courseService.createCourse(requestDto), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Bütün kursları gətir",
            description = "Sistemdəki bütün kursların siyahısını qaytarır.")
    @ApiResponse(responseCode = "200", description = "Kurs siyahısı uğurla qaytarıldı")
    public ResponseEntity<List<CourseResponseDto>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @GetMapping("/{id}")
    @Operation(summary = "ID ilə kurs tap",
            description = "Verilmiş ID-yə görə kursun məlumatlarını qaytarır.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Kurs tapıldı"),
            @ApiResponse(responseCode = "404", description = "Kurs tapılmadı")
    })
    public ResponseEntity<CourseResponseDto> getCourseById(
            @Parameter(description = "Kursun unikal ID-si", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Kursu yenilə",
            description = "Mövcud kursun məlumatlarını yeniləyir.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Kurs uğurla yeniləndi"),
            @ApiResponse(responseCode = "404", description = "Kurs tapılmadı")
    })
    public ResponseEntity<CourseResponseDto> updateCourse(
            @Parameter(description = "Kursun unikal ID-si", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody CourseRequestDto requestDto) {
        return ResponseEntity.ok(courseService.updateCourse(id, requestDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Kursu sil",
            description = "Verilmiş ID-yə görə kursu sistemdən silir.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Kurs uğurla silindi"),
            @ApiResponse(responseCode = "404", description = "Kurs tapılmadı")
    })
    public ResponseEntity<Void> deleteCourse(
            @Parameter(description = "Kursun unikal ID-si", example = "1")
            @PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{courseId}/students/{studentId}")
    @Operation(summary = "Tələbəni kursa qeydiyyat et",
            description = "Tələbəni seçilmiş kursa qeydiyyatdan keçirir. " +
                    "İlkin şərt yoxlanılır. Qeydiyyat tarixi avtomatik qeyd olunur.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Qeydiyyat uğurla tamamlandı"),
            @ApiResponse(responseCode = "400", description = "İlkin şərt yerinə yetirilməyib"),
            @ApiResponse(responseCode = "404", description = "Tələbə və ya kurs tapılmadı"),
            @ApiResponse(responseCode = "409", description = "Tələbə artıq bu kursa qeydiyyatdan keçib")
    })
    public ResponseEntity<EnrollmentResponseDto> enrollStudent(
            @Parameter(description = "Kursun unikal ID-si", example = "1")
            @PathVariable Long courseId,
            @Parameter(description = "Tələbənin unikal ID-si", example = "1")
            @PathVariable Long studentId) {
        return new ResponseEntity<>(courseService.enrollStudent(courseId, studentId), HttpStatus.CREATED);
    }

    @GetMapping("/{courseId}/students")
    @Operation(summary = "Kursun tələbələrini gətir",
            description = "Verilmiş kursa qeydiyyatdan keçmiş bütün tələbələrin məlumatlarını qaytarır.")
    @ApiResponse(responseCode = "200", description = "Tələbə siyahısı uğurla qaytarıldı")
    public ResponseEntity<CourseStudentsResponseDto> getCourseStudents(
            @Parameter(description = "Kursun unikal ID-si", example = "1")
            @PathVariable Long courseId) {
        return ResponseEntity.ok(courseService.getCourseStudents(courseId));
    }

    @GetMapping("/search")
    @Operation(summary = "Tələbənin adına görə kursları tap",
            description = "Tələbənin adı və ya soyadı ilə axtarış aparır, " +
                    "həmin tələbənin qeydiyyatdan keçdiyi kursları qaytarır.")
    @ApiResponse(responseCode = "200", description = "Kurslar uğurla tapıldı")
    public ResponseEntity<List<CourseResponseDto>> getCoursesByStudentName(
            @Parameter(description = "Tələbənin adı və ya soyadı", example = "Nicat")
            @RequestParam String name) {
        return ResponseEntity.ok(courseService.getCoursesByStudentName(name));
    }
}