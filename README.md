# Universitet İdarəetmə Sistemi

Spring Boot ilə hazırlanmış mikroservis arxitekturalı universitet idarəetmə sistemi.

## Servislar

| Servis | Port | Təsvir |
|--------|------|--------|
| student-service | 9090 | Tələbələrin idarə edilməsi |
| course-service | 8081 | Kurslar və qeydiyyat |

## Texnologiyalar

- Java 21, Spring Boot 3.3.5
- Spring Data JPA, Spring Validation
- PostgreSQL, Spring Cloud OpenFeign
- Springdoc OpenAPI (Swagger)
- Docker, Docker Compose

## Layihəni İşə Salmaq

### 1. Verilənlər bazalarını başlat
```bash
docker-compose up -d student-db course-db
```

### 2. Hər iki servisi ayrı terminalda işə sal
```bash
# Terminal 1
cd student-service
./gradlew bootRun

# Terminal 2
cd course-service
./gradlew bootRun
```

## Swagger UI

| Servis | URL |
|--------|-----|
| Tələbə Xidməti | http://localhost:9090/swagger-ui.html |
| Kurs Xidməti | http://localhost:8081/swagger-ui.html |

## Əsas Endpointlər

### Tələbə Xidməti (port 9090)
- `POST /api/v1/students` — Yeni tələbə yarat
- `GET /api/v1/students` — Bütün tələbələri gətir
- `GET /api/v1/students/{id}` — ID ilə tələbə tap
- `GET /api/v1/students/search?name=Nicat` — Ada görə axtar

### Kurs Xidməti (port 8081)
- `POST /api/v1/courses` — Yeni kurs yarat
- `GET /api/v1/courses` — Bütün kursları gətir
- `POST /api/v1/courses/{courseId}/students/{studentId}` — Qeydiyyat et
- `GET /api/v1/courses/search?name=Nicat` — Ada görə kursları tap

## Nümunə Sorğular

### Tələbə yarat
```bash
curl -X POST http://localhost:9090/api/v1/students \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Nicat","lastName":"Aliyev","email":"nicat@ada.edu.az","age":20}'
```

### İlkin şərtsiz kurs yarat
```bash
curl -X POST http://localhost:8081/api/v1/courses \
  -H "Content-Type: application/json" \
  -d '{"title":"Proqramlaşdırmaya Giriş","code":"CS101","credits":3,"prerequisiteCourseId":null}'
```

### İlkin şərtli kurs yarat (CS101 tamamlanmalıdır)
```bash
curl -X POST http://localhost:8081/api/v1/courses \
  -H "Content-Type: application/json" \
  -d '{"title":"Məlumat Strukturları","code":"CS201","credits":4,"prerequisiteCourseId":1}'
```

### Qeydiyyat et
```bash
curl -X POST http://localhost:8081/api/v1/courses/1/students/1
```

### Ada görə kurs axtar
```bash
curl "http://localhost:8081/api/v1/courses/search?name=Nicat"
```