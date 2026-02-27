# BuildFlow Frontend (React)

## Рекомендуемый запуск всего проекта

Из корня репозитория:

```bash
docker compose up --build
```

Frontend: `http://localhost:3000`

## Локальный запуск только фронтенда

```bash
cd frontend
python3 -m http.server 3000
```

После этого открой `http://localhost:3000`.

> Backend должен быть запущен на `http://localhost:8080`.

## Если backend не поднимается

Если при запуске backend видишь `403 Forbidden` на `repo.maven.apache.org`, это сетевое ограничение для Maven-зависимостей. В этом случае настрой Maven mirror/proxy или используй сеть без блокировок.

## Что есть

- лендинг с анимированным hero-блоком;
- вход через `/api/auth/login`;
- работа с клиентами и расчётами (`/api/calculations/*`);
- каталог материалов и корзина в localStorage;
- раздел личного кабинета с историей расчётов.
