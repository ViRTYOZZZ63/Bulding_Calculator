# BuildFlow Frontend (React)

## Запуск
Вариант 1 (Python):
```bash
cd frontend
python3 -m http.server 3000
```

Вариант 2 (Node.js):
```bash
cd frontend
npx serve -l 3000 .
```

Открой: `http://localhost:3000`

> Для работы API backend должен быть запущен на `http://localhost:8080`.

## Что обновлено
- Hero использует локальные изображения из `frontend/media` (без внешних CDN-картинок).
- Добавлены KPI и сценарии работы платформы.
- Есть поиск по материалам и уведомления о действиях пользователя.

## Если не запускается
1. Проверь, что порт `3000` не занят.
2. Проверь консоль браузера (`F12` → Console).
3. Если запросы к API падают, сначала подними backend и проверь `http://localhost:8080/swagger-ui.html`.
