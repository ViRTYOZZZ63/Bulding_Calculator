# Building Calculator — запуск и диагностика

## 1) Быстрый запуск через Docker

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

С очисткой БД volume:

```bash
docker compose down -v
```

---

## 2) Локальный запуск без Docker

### Backend

```bash
cd CalculatorBackend-main
chmod +x mvnw
./mvnw spring-boot:run
```

### Frontend

```bash
cd frontend
python3 -m http.server 3000
```

---

## 3) Если «не запускается» — быстрый чек-лист

### A) `./mvnw: Permission denied`

```bash
cd CalculatorBackend-main
chmod +x mvnw
```

### B) `403 Forbidden` при скачивании зависимостей Maven (`repo.maven.apache.org`)

Это ограничение сети/прокси (часто в корпоративных или CI окружениях).

Что сделать:
1. Настроить Maven mirror/proxy в `~/.m2/settings.xml`.
2. Или запустить в сети без блокировки Maven Central.
3. Для Docker: сначала проверить, что `mvn -DskipTests package` работает локально; если нет — и `docker compose up --build` тоже не соберёт backend.

### C) Проверка CORS / доступа фронта к API

Backend разрешает фронт с `http://localhost:3000`, поэтому открывай именно этот адрес для UI.

---

## Сервисы в `docker-compose.yml`

- `postgres` (PostgreSQL 16)
- `backend` (Spring Boot, Java 17)
- `frontend` (Nginx + React SPA)
