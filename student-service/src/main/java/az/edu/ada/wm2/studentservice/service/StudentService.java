package az.edu.ada.wm2.studentservice.service;

import az.edu.ada.wm2.studentservice.exception.StudentNotFoundException;
import az.edu.ada.wm2.studentservice.model.dto.StudentRequestDto;
import az.edu.ada.wm2.studentservice.model.dto.StudentResponseDto;
import az.edu.ada.wm2.studentservice.model.entity.Student;
import az.edu.ada.wm2.studentservice.repository.StudentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentResponseDto createStudent(StudentRequestDto requestDto) {
        Student student = Student.builder()
                .firstName(requestDto.getFirstName())
                .lastName(requestDto.getLastName())
                .email(requestDto.getEmail())
                .age(requestDto.getAge())
                .build();
        return toResponseDto(studentRepository.save(student));
    }

    public List<StudentResponseDto> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::toResponseDto)
                .toList();
    }

    public StudentResponseDto getStudentById(Long id) {
        return toResponseDto(findStudentOrThrow(id));
    }

    public StudentResponseDto updateStudent(Long id, StudentRequestDto requestDto) {
        Student existing = findStudentOrThrow(id);
        existing.setFirstName(requestDto.getFirstName());
        existing.setLastName(requestDto.getLastName());
        existing.setEmail(requestDto.getEmail());
        existing.setAge(requestDto.getAge());
        return toResponseDto(studentRepository.save(existing));
    }

    public void deleteStudent(Long id) {
        studentRepository.delete(findStudentOrThrow(id));
    }

    public List<StudentResponseDto> searchByName(String name) {
        return studentRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, name)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    private Student findStudentOrThrow(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException(id));
    }

    private StudentResponseDto toResponseDto(Student student) {
        return new StudentResponseDto(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getEmail(),
                student.getAge()
        );
    }
}