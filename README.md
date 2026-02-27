# Building Calculator — локальный запуск

## Что внутри
- `CalculatorBackend-main` — Spring Boot backend.
- `frontend` — SPA на Vanilla JS (статические файлы + локальные изображения в `frontend/media`).

## Минимальные требования
- Java 17+
- PostgreSQL 14+
- Один из вариантов для запуска фронта:
  - Python 3, или
  - Node.js 18+ (`npx serve`)


## Важно про фронтенд
- Фронтенд больше не зависит от CDN React/Babel: `frontend/index.html` и `frontend/app.js` полностью локальные.
- Если раньше был чёрный экран из-за недоступного `unpkg`, теперь эта проблема устранена.

## Быстрый старт

### 1) Поднять PostgreSQL
Нужные параметры по умолчанию:
- DB: `calculator_db`
- user: `postgres`
- password: `postgres`
- host: `localhost`
- port: `5432`

Пример создания БД:
```sql
CREATE DATABASE calculator_db;
```

### 2) Запустить backend
```bash
cd CalculatorBackend-main
chmod +x mvnw
./mvnw spring-boot:run
```

Windows:
```powershell
cd CalculatorBackend-main
mvnw.cmd spring-boot:run
```

Backend после старта:
- API: http://localhost:8080/api
- Swagger UI: http://localhost:8080/swagger-ui.html

### 3) Запустить frontend
В новом терминале (вариант 1):
```bash
cd frontend
python3 -m http.server 3000
```

Если Python не установлен, вариант 2:
```bash
cd frontend
npx serve -l 3000 .
```

Frontend: http://localhost:3000

---

## Если «вообще не запускается»

### Backend не стартует
1. Проверь Java:
   ```bash
   java -version
   ```
2. Проверь доступ к PostgreSQL:
   ```bash
   psql -h localhost -U postgres -d calculator_db -c "select 1;"
   ```
3. Если Maven wrapper не может скачать зависимости (`Failed to fetch ... apache-maven-*.zip`), это сеть/прокси. Варианты:
   - настроить proxy/mirror для Maven;
   - использовать другой интернет-канал;
   - если Maven установлен локально, запускать `mvn spring-boot:run`.

### Frontend не стартует
1. Проверь, что порт свободен:
   ```bash
   lsof -i :3000
   ```
2. Подними сервер другим способом (`python3 -m http.server 3000` или `npx serve -l 3000 .`).
3. Если страница открывается, но API не отвечает — backend должен работать на `http://localhost:8080`.

### Быстрая проверка связки
- Открой frontend, выполни вход (`manager` / `password`).
- На вкладке **Материалы** нажми «Подтянуть с backend».
- Если данные пришли — фронт+бэк связаны корректно.
