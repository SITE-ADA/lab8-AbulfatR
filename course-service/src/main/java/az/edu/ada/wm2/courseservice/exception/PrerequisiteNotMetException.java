package az.edu.ada.wm2.courseservice.exception;

public class PrerequisiteNotMetException extends RuntimeException {
    public PrerequisiteNotMetException(String courseName, String prerequisiteName, Long prerequisiteId) {
        super(String.format(
                "Qeydiyyat rədd edildi. '%s' kursuna yazılmaq üçün əvvəlcə '%s' (ID=%d) kursunu keçməlisiniz.",
                courseName, prerequisiteName, prerequisiteId));
    }
}