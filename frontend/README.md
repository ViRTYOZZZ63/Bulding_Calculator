# BuildFlow Frontend (React)

## Запуск всего проекта через Docker

```bash
docker compose up --build
```

Frontend: `http://localhost:3000`

### Если в PowerShell ошибка `docker не распознано`

Поставь Docker Desktop и проверь:

```powershell
docker --version
docker compose version
```

После этого повтори `docker compose up --build`.

## Локальный запуск только фронтенда

```bash
cd frontend
python3 -m http.server 3000
```

После этого открой `http://localhost:3000`.

> Backend должен быть запущен на `http://localhost:8080`.

## Если backend не поднимается

Если при запуске backend видишь `403 Forbidden` на `repo.maven.apache.org`, это сетевое ограничение для Maven-зависимостей. В этом случае настрой Maven mirror/proxy или используй сеть без блокировок.
