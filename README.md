# Building Calculator — быстрый запуск

## One-click запуск (рекомендуется)

```bash
docker compose up --build
```

После запуска:
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

Остановка:

```bash
docker compose down
```

Остановка с очисткой volume базы:

```bash
docker compose down -v
```

---

## Что поднимает compose

- `postgres` (PostgreSQL 16)
- `backend` (Spring Boot, Java 17)
- `frontend` (Nginx + React SPA из `frontend/`)

---

## Локальный запуск без Docker

### Backend

```bash
cd CalculatorBackend-main
./mvnw spring-boot:run
```

### Frontend

```bash
cd frontend
python3 -m http.server 3000
```
