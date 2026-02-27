# Building Calculator — запуск и диагностика

## Если у тебя ошибка `docker : Имя "docker" не распознано`

Это означает, что Docker Desktop не установлен (или не добавлен в `PATH`).

### Windows: как поставить Docker Desktop

1. Установи Docker Desktop:
   - через официальный сайт: https://www.docker.com/products/docker-desktop/
   - или через PowerShell (админ):

```powershell
winget install -e --id Docker.DockerDesktop
```

2. Перезагрузи ПК.
3. Запусти **Docker Desktop** и дождись статуса `Engine running`.
4. Проверь в новом PowerShell:

```powershell
docker --version
docker compose version
```

5. Потом запускай проект:

```powershell
docker compose up --build
```

После запуска:
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

---

## Запуск без Docker (если Docker пока не нужен)

### 1) Поднять PostgreSQL локально
Нужны параметры:
- DB: `calculator_db`
- user: `postgres`
- password: `postgres`

### 2) Backend

```bash
cd CalculatorBackend-main
chmod +x mvnw
./mvnw spring-boot:run
```

> На Windows можно запускать `mvnw.cmd spring-boot:run`.

### 3) Frontend

```bash
cd frontend
python3 -m http.server 3000
```

---

## Остановка Docker-стека

```bash
docker compose down
```

С очисткой volume БД:

```bash
docker compose down -v
```
