# Building Calculator — локальный запуск

## Что внутри
- `CalculatorBackend-main` — Spring Boot backend.
- `frontend` — SPA на React (через CDN + Babel).

## Требования
- Java 17+
- Python 3 (для простого статического сервера фронтенда)
- PostgreSQL 14+

## 1) Подготовить PostgreSQL
Создай БД и пользователя (или используй существующего):
- DB: `calculator_db`
- user: `postgres`
- password: `postgres`
- host: `localhost`
- port: `5432`

Пример SQL:
```sql
CREATE DATABASE calculator_db;
```

## 2) Запустить backend
```bash
cd CalculatorBackend-main
chmod +x mvnw
./mvnw spring-boot:run
```

Для Windows:
```powershell
cd CalculatorBackend-main
mvnw.cmd spring-boot:run
```

После запуска backend доступен:
- API: http://localhost:8080/api
- Swagger UI: http://localhost:8080/swagger-ui.html

## 3) Запустить frontend
В новом терминале:
```bash
cd frontend
python3 -m http.server 3000
```

Открыть в браузере: http://localhost:3000

## 4) Проверка, что всё ок
- Авторизация во фронтенде: логин `manager`, пароль `password`.
- Вкладка **Калькулятор**: загрузи клиентов → создай расчёт → рассчитай каркас/фундамент.
- Вкладка **База материалов**: подтяни материалы и добавь в корзину.

## Частые проблемы
1. **Backend не стартует из-за БД**
   - Проверь, что PostgreSQL запущен.
   - Проверь логин/пароль/имя БД.

2. **Frontend не получает данные**
   - Убедись, что backend запущен на `http://localhost:8080`.
   - Проверь CORS и сообщения об ошибках в DevTools.

3. **Maven не качает зависимости**
   - Проверь сеть / прокси / mirror для Maven.
