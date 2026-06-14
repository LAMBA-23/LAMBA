# MVP v0 Report

## Цель

Документ фиксирует минимальный сценарий MVP v0 и статус его проверки.

## Smoke-Check Scenario

Целевой сценарий:

1. Запустить backend.
2. Открыть `GET /health`.
3. Открыть Android app.
4. Войти с demo credentials.
5. Перейти в `История`.
6. Открыть форму добавления события.
7. Добавить событие заправки.
8. Сохранить событие через backend.
9. Убедиться, что событие видно в истории.
10. Убедиться, что статистика обновилась.
11. Открыть `Чат`.
12. Отправить сообщение и проверить ответ или понятную ошибку.

## Current Status

- TODO: зафиксировать дату фактического smoke-check
- TODO: приложить evidence по каждому шагу
- TODO: указать, какие части уже интегрированы Android -> FastAPI -> PostgreSQL

## Demo Credentials

- Demo user: `demo`
- Demo password: `demo`
- Temporary Android compatibility password: `password`

## Run Instructions

- Backend and PostgreSQL: [root README](../../README.md)
- API contract: [../../docs/api-contract.md](../../docs/api-contract.md)

## Runnable Artifact / Deployment

- Локальный runnable artifact: `docker-compose.yml`
- TODO: добавить deployed URL или явно указать, что деплой отсутствует

## Risks / Limitations

- TODO: перечислить незавершенные интеграции, если они останутся перед сдачей
- TODO: указать статус Mistral integration and fallback behavior
