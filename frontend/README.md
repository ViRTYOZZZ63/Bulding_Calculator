# BuildFlow Frontend (React)

Фронтенд сделан на React (через CDN) и интегрирован с backend API.

## Запуск

```bash
cd frontend
python3 -m http.server 3000
```

После этого открой `http://localhost:3000`.

> Backend должен быть запущен на `http://localhost:8080`.

## Что есть

- лендинг с анимированным hero-блоком;
- вход через `/api/auth/login`;
- работа с клиентами и расчётами (`/api/calculations/*`);
- каталог материалов и корзина в localStorage;
- раздел личного кабинета с историей расчётов.
