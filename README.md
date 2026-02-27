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

## Быстрый старт

### 1) Запустить frontend
вариант 1:
```bash
cd frontend
python3 -m http.server 3000
```
 вариант 2:
```bash
cd frontend
npx serve -l 3000 .
```

Frontend: http://localhost:3000


### Быстрая проверка связки
- Открой frontend, выполни вход (`manager` / `password`).
- На вкладке **Материалы** нажми «Подтянуть с backend».
- Если данные пришли — фронт+бэк связаны корректно.
