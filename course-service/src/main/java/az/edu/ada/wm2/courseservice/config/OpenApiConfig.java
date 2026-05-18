package az.edu.ada.wm2.courseservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Kurs Xidməti API",
                version = "v1",
                description = "Kursların idarə edilməsi və tələbə qeydiyyatı üçün API. " +
                        "İlkin şərt yoxlaması, qeydiyyat tarixi və ada görə kurs axtarışı dəstəklənir.",
                contact = @Contact(name = "WM2 Backend Kursu"),
                license = @License(name = "Tədris Məqsədli")
        ),
        servers = {
                @Server(url = "http://localhost:8081", description = "Lokal server")
        }
)
public class OpenApiConfig {
}