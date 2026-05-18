package az.edu.ada.wm2.courseservice.service;

import az.edu.ada.wm2.courseservice.client.StudentFeignClient;
import az.edu.ada.wm2.courseservice.exception.CourseNotFoundException;
import az.edu.ada.wm2.courseservice.exception.EnrollmentAlreadyExistsException;
import az.edu.ada.wm2.courseservice.exception.PrerequisiteNotMetException;
import az.edu.ada.wm2.courseservice.exception.RemoteStudentNotFoundException;
import az.edu.ada.wm2.courseservice.exception.StudentServiceCommunicationException;
import az.edu.ada.wm2.courseservice.model.dto.CourseRequestDto;
import az.edu.ada.wm2.courseservice.model.dto.CourseResponseDto;
import az.edu.ada.wm2.courseservice.model.dto.CourseStudentsResponseDto;
import az.edu.ada.wm2.courseservice.model.dto.EnrollmentResponseDto;
import az.edu.ada.wm2.courseservice.model.dto.StudentDto;
import az.edu.ada.wm2.courseservice.model.entity.Course;
import az.edu.ada.wm2.courseservice.model.entity.Enrollment;
import az.edu.ada.wm2.courseservice.repository.CourseRepository;
import az.edu.ada.wm2.courseservice.repository.EnrollmentRepository;
import feign.FeignException;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentFeignClient studentFeignClient;
    private final RestTemplate restTemplate;

    @Value("${student.service.base-url}")
    private String studentServiceBaseUrl;

    public CourseResponseDto createCourse(CourseRequestDto requestDto) {
        if (requestDto.getPrerequisiteCourseId() != null) {
            findCourseOrThrow(requestDto.getPrerequisiteCourseId());
        }
        Course course = Course.builder()
                .title(requestDto.getTitle())
                .code(requestDto.getCode())
                .credits(requestDto.getCredits())
                .prerequisiteCourseId(requestDto.getPrerequisiteCourseId())
                .build();
        return toCourseResponseDto(courseRepository.save(course));
    }

    public List<CourseResponseDto> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::toCourseResponseDto)
                .toList();
    }

    public CourseResponseDto getCourseById(Long id) {
        return toCourseResponseDto(findCourseOrThrow(id));
    }

    public CourseResponseDto updateCourse(Long id, CourseRequestDto requestDto) {
        Course existing = findCourseOrThrow(id);
        if (requestDto.getPrerequisiteCourseId() != null) {
            findCourseOrThrow(requestDto.getPrerequisiteCourseId());
        }
        existing.setTitle(requestDto.getTitle());
        existing.setCode(requestDto.getCode());
        existing.setCredits(requestDto.getCredits());
        existing.setPrerequisiteCourseId(requestDto.getPrerequisiteCourseId());
        return toCourseResponseDto(courseRepository.save(existing));
    }

    public void deleteCourse(Long id) {
        courseRepository.delete(findCourseOrThrow(id));
    }

    public EnrollmentResponseDto enrollStudent(Long courseId, Long studentId) {
        log.debug("Enrolling student {} into course {}", studentId, courseId);
        Course course = findCourseOrThrow(courseId);

        if (enrollmentRepository.existsByCourseIdAndStudentId(courseId, studentId)) {
            throw new EnrollmentAlreadyExistsException(courseId, studentId);
        }

        // Prerequisite check — if course has a prerequisite,
        // student must already be enrolled in that prerequisite course
        if (course.getPrerequisiteCourseId() != null) {
            boolean hasPrerequisite = enrollmentRepository
                    .existsByStudentIdAndCourseId(studentId, course.getPrerequisiteCourseId());
            if (!hasPrerequisite) {
                Course prerequisite = findCourseOrThrow(course.getPrerequisiteCourseId());
                throw new PrerequisiteNotMetException(
                        course.getTitle(),
                        prerequisite.getTitle(),
                        prerequisite.getId());
            }
        }

        validateStudentWithFeign(studentId);

        Enrollment enrollment = Enrollment.builder()
                .courseId(courseId)
                .studentId(studentId)
                .build();
        Enrollment saved = enrollmentRepository.save(enrollment);

        return new EnrollmentResponseDto(
                saved.getId(),
                saved.getCourseId(),
                saved.getStudentId(),
                saved.getEnrollmentDate(),
                "Student enrolled successfully."
        );
    }

    public CourseStudentsResponseDto getCourseStudents(Long courseId) {
        Course course = findCourseOrThrow(courseId);
        List<Long> studentIds = enrollmentRepository.findByCourseId(courseId)
                .stream()
                .map(Enrollment::getStudentId)
                .toList();
        List<StudentDto> students = studentIds.stream()
                .map(this::fetchStudentWithRestTemplate)
                .toList();
        return new CourseStudentsResponseDto(course.getId(), course.getTitle(), students);
    }

    public List<CourseResponseDto> getCoursesByStudentName(String name) {
        log.debug("Fetching courses for student name: {}", name);
        String url = studentServiceBaseUrl + "/api/v1/students/search?name=" + name;
        StudentDto[] students;
        try {
            students = restTemplate.getForObject(url, StudentDto[].class);
        } catch (RestClientException ex) {
            throw new StudentServiceCommunicationException("Could not search students by name.");
        }
        if (students == null || students.length == 0) {
            return List.of();
        }
        return Arrays.stream(students)
                .flatMap(student -> enrollmentRepository.findByStudentId(student.getId()).stream())
                .map(enrollment -> toCourseResponseDto(findCourseOrThrow(enrollment.getCourseId())))
                .distinct()
                .toList();
    }

    private void validateStudentWithFeign(Long studentId) {
        try {
            studentFeignClient.getStudentById(studentId);
        } catch (FeignException.NotFound ex) {
            throw new RemoteStudentNotFoundException(studentId);
        } catch (FeignException ex) {
            throw new StudentServiceCommunicationException("Could not validate student-service response.");
        }
    }

    private StudentDto fetchStudentWithRestTemplate(Long studentId) {
        String url = studentServiceBaseUrl + "/api/v1/students/" + studentId;
        try {
            return restTemplate.getForObject(url, StudentDto.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new RemoteStudentNotFoundException(studentId);
        } catch (RestClientException ex) {
            throw new StudentServiceCommunicationException("Could not fetch student details.");
        }
    }

    private Course findCourseOrThrow(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException(id));
    }

    private CourseResponseDto toCourseResponseDto(Course course) {
        return new CourseResponseDto(
                course.getId(),
                course.getTitle(),
                course.getCode(),
                course.getCredits(),
                course.getPrerequisiteCourseId()
        );
    }
}