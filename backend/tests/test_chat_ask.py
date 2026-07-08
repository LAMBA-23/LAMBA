from __future__ import annotations

import importlib
import os
import sys
from pathlib import Path

from fastapi.testclient import TestClient


TEST_DB_PATH = Path(__file__).resolve().parent / "test_chat_ask.db"
os.environ["DATABASE_URL"] = f"sqlite:///{TEST_DB_PATH.as_posix()}"
os.environ.pop("TIMEWEB_API_KEY", None)
os.environ.pop("TIMEWEB_AGENT_ID", None)

for module_name in ["app.main", "app.database", "app.models", "app.deepseek_chat"]:
    sys.modules.pop(module_name, None)

main_module = importlib.import_module("app.main")
database_module = importlib.import_module("app.database")

database_module.Base.metadata.create_all(bind=database_module.engine)

client = TestClient(main_module.app)


def teardown_module() -> None:
    client.close()
    database_module.engine.dispose()
    if TEST_DB_PATH.exists():
        TEST_DB_PATH.unlink()


def _register_and_get_user_id(username: str = "ask-user") -> int:
    resp = client.post(
        "/auth/register",
        json={"username": username, "password": "password123"},
    )
    return resp.json()["user_id"]


def _create_event(
    user_id: int,
    *,
    event_type: str,
    description: str,
    amount: int | None = None,
    fuel_liters: int | None = None,
    mileage: int | None = None,
) -> None:
    response = client.post(
        f"/events?user_id={user_id}",
        json={
            "type": event_type,
            "description": description,
            "amount": amount,
            "fuel_liters": fuel_liters,
            "mileage": mileage,
        },
    )
    assert response.status_code == 200


def test_chat_ask_returns_answer(monkeypatch):
    user_id = _register_and_get_user_id("ask-user-1")

    def fake_ask(message: str, vehicle_context: str | None = None) -> str:
        return f"Ответ на: {message}"

    monkeypatch.setattr(main_module, "ask_deepseek", fake_ask)

    resp = client.post(
        f"/chat/ask?user_id={user_id}",
        json={"message": "Какой пробег?"},
    )

    assert resp.status_code == 200
    data = resp.json()
    assert "answer" in data
    assert data["answer"] == "Ответ на: Какой пробег?"


def test_chat_ask_includes_vehicle_context(monkeypatch):
    user_id = _register_and_get_user_id("ask-user-2")

    client.post(
        "/vehicle",
        json={
            "user_id": user_id,
            "brand": "Toyota",
            "model": "Camry",
            "production_year": 2023,
            "current_mileage": 50000,
        },
    )
    client.post(
        f"/events?user_id={user_id}",
        json={
            "type": "fuel",
            "description": "Заправка",
            "amount": 3000,
            "mileage": 50000,
        },
    )

    captured = {}

    def fake_ask(message: str, vehicle_context: str | None = None) -> str:
        captured["context"] = vehicle_context
        return "OK"

    monkeypatch.setattr(main_module, "ask_deepseek", fake_ask)

    resp = client.post(
        f"/chat/ask?user_id={user_id}",
        json={"message": "Какой текущий статус машины?"},
    )

    assert resp.status_code == 200
    ctx = captured["context"]
    assert "Toyota" in ctx
    assert "Camry" in ctx
    assert "50000" in ctx
    assert "Заправка" in ctx


def test_chat_ask_with_no_events(monkeypatch):
    user_id = _register_and_get_user_id("ask-user-3")

    captured = {}

    def fake_ask(message: str, vehicle_context: str | None = None) -> str:
        captured["context"] = vehicle_context
        return "Нет данных"

    monkeypatch.setattr(main_module, "ask_deepseek", fake_ask)

    resp = client.post(
        f"/chat/ask?user_id={user_id}",
        json={"message": "Что нового по машине?"},
    )

    assert resp.status_code == 200
    ctx = captured["context"]
    assert "Not set" not in ctx


def test_chat_ask_limits_context_to_30_events(monkeypatch):
    user_id = _register_and_get_user_id("ask-user-limit")

    client.post(
        "/vehicle",
        json={
            "user_id": user_id,
            "brand": "BMW",
            "model": "M4",
            "production_year": 2020,
            "current_mileage": 100000,
        },
    )
    for i in range(60):
        client.post(
            f"/events?user_id={user_id}",
            json={
                "type": "fuel",
                "description": f"Заправка {i}",
                "amount": 1000 + i,
                "mileage": 100000 + i,
            },
        )

    captured = {}

    def fake_ask(message: str, vehicle_context: str | None = None) -> str:
        captured["context"] = vehicle_context
        return "OK"

    monkeypatch.setattr(main_module, "ask_deepseek", fake_ask)

    resp = client.post(
        f"/chat/ask?user_id={user_id}",
        json={"message": "Что было в истории авто за последний год?"},
    )

    assert resp.status_code == 200
    ctx = captured["context"]
    assert "Заправка 0" not in ctx
    assert "Заправка 29" not in ctx
    assert "Заправка 59" in ctx
    assert "Заправка 30" in ctx


def test_chat_ask_missing_user_id():
    resp = client.post(
        "/chat/ask",
        json={"message": "Вопрос"},
    )
    assert resp.status_code == 422


def test_chat_ask_missing_message():
    resp = client.post(
        "/chat/ask?user_id=1",
        json={},
    )
    assert resp.status_code == 422


def test_chat_ask_rejects_empty_message():
    resp = client.post(
        "/chat/ask?user_id=1",
        json={"message": ""},
    )
    assert resp.status_code == 422


def test_chat_ask_rejects_blank_message():
    resp = client.post(
        "/chat/ask?user_id=1",
        json={"message": "   "},
    )
    assert resp.status_code == 422


def test_chat_ask_nonexistent_user(monkeypatch):
    def fake_ask(message: str, vehicle_context: str | None = None) -> str:
        return "OK"

    monkeypatch.setattr(main_module, "ask_deepseek", fake_ask)

    resp = client.post(
        "/chat/ask?user_id=99999",
        json={"message": "Вопрос"},
    )
    assert resp.status_code == 404


def test_chat_ask_returns_latest_five_expenses_without_llm(monkeypatch):
    user_id = _register_and_get_user_id("ask-user-expenses-1")

    _create_event(
        user_id,
        event_type="fuel",
        description="Заправка 1",
        amount=1000,
        fuel_liters=20,
        mileage=100000,
    )
    _create_event(
        user_id,
        event_type="repair",
        description="Ремонт 1",
        amount=2000,
        mileage=100100,
    )
    _create_event(
        user_id,
        event_type="issue",
        description="Поломка без суммы",
        amount=0,
        mileage=100200,
    )
    _create_event(
        user_id,
        event_type="trip",
        description="Платная дорога",
        amount=1500,
        mileage=120,
    )
    _create_event(
        user_id,
        event_type="fuel",
        description="Заправка 2",
        amount=2500,
        fuel_liters=40,
        mileage=100320,
    )
    _create_event(
        user_id,
        event_type="repair",
        description="Замена масла",
        amount=7000,
        mileage=100350,
    )
    _create_event(
        user_id,
        event_type="issue",
        description="Эвакуатор",
        amount=3000,
        mileage=100360,
    )

    def fail_if_called(*args, **kwargs):
        raise AssertionError("LLM must not be called for expense queries")

    monkeypatch.setattr(main_module, "ask_deepseek", fail_if_called)

    response = client.post(
        f"/chat/ask?user_id={user_id}",
        json={"message": "Покажи последние расходы"},
    )

    assert response.status_code == 200
    assert response.json()["answer"] == (
        "Расходы за последние 5 записей: 16 000 ₽\n\n"
        "По категориям:\n\n"
        "* Топливо: 2 500 ₽\n"
        "* Ремонт: 9 000 ₽\n"
        "* Поездки: 1 500 ₽\n"
        "* Проблемы: 3 000 ₽\n\n"
        "Последние расходы:\n\n"
        "1. Эвакуатор — 3 000 ₽\n\n"
        "2. Замена масла — 7 000 ₽\n\n"
        "3. Заправка 2 — 2 500 ₽, 40 л\n\n"
        "4. Платная дорога — 1 500 ₽\n\n"
        "5. Ремонт 1 — 2 000 ₽"
    )


def test_chat_ask_filters_weekly_expenses(monkeypatch):
    user_id = _register_and_get_user_id("ask-user-expenses-2")

    old_timestamp = "2026-06-01 12:00:00"
    recent_timestamp = "2026-07-07 12:00:00"

    _create_event(
        user_id,
        event_type="repair",
        description="Старый ремонт",
        amount=9000,
        mileage=100000,
    )
    _create_event(
        user_id,
        event_type="fuel",
        description="Свежая заправка",
        amount=2500,
        fuel_liters=35,
        mileage=100100,
    )
    _create_event(
        user_id,
        event_type="issue",
        description="Свежая проблема",
        amount=1200,
        mileage=100150,
    )

    with database_module.engine.begin() as connection:
        connection.exec_driver_sql(
            "UPDATE events SET created_at = ? WHERE description = ?",
            (old_timestamp, "Старый ремонт"),
        )
        connection.exec_driver_sql(
            "UPDATE events SET created_at = ? WHERE description = ?",
            (recent_timestamp, "Свежая заправка"),
        )
        connection.exec_driver_sql(
            "UPDATE events SET created_at = ? WHERE description = ?",
            (recent_timestamp, "Свежая проблема"),
        )

    def fail_if_called(*args, **kwargs):
        raise AssertionError("LLM must not be called for expense queries")

    monkeypatch.setattr(main_module, "ask_deepseek", fail_if_called)

    response = client.post(
        f"/chat/ask?user_id={user_id}",
        json={"message": "Покажи расходы за неделю"},
    )

    assert response.status_code == 200
    assert response.json()["answer"] == (
        "Расходы за неделю: 3 700 ₽\n\n"
        "По категориям:\n\n"
        "* Топливо: 2 500 ₽\n"
        "* Проблемы: 1 200 ₽\n\n"
        "Последние расходы:\n\n"
        "1. Свежая проблема — 1 200 ₽\n\n"
        "2. Свежая заправка — 2 500 ₽, 35 л"
    )


def test_chat_ask_returns_general_statistics_summary_without_llm(monkeypatch):
    user_id = _register_and_get_user_id("ask-user-expenses-3")

    _create_event(
        user_id,
        event_type="fuel",
        description="Заправка",
        amount=2500,
        fuel_liters=40,
        mileage=100000,
    )
    _create_event(
        user_id,
        event_type="repair",
        description="Ремонт",
        amount=7000,
        mileage=100000,
    )
    _create_event(
        user_id,
        event_type="trip",
        description="Поездка 120 км",
        amount=1500,
        mileage=120,
    )
    _create_event(
        user_id,
        event_type="issue",
        description="Чек",
        amount=500,
        mileage=100120,
    )

    def fail_if_called(*args, **kwargs):
        raise AssertionError("LLM must not be called for statistics queries")

    monkeypatch.setattr(main_module, "ask_deepseek", fail_if_called)

    response = client.post(
        f"/chat/ask?user_id={user_id}",
        json={"message": "Покажи статистику"},
    )

    assert response.status_code == 200
    assert response.json()["answer"] == (
        "Краткая статистика за всё время:\n\n"
        "* Расходы: 11 500 ₽\n"
        "* Пробег: 100 120 км\n"
        "* Топливо: 40 л\n"
        "* Записей: 4"
    )


def test_chat_ask_filters_monthly_expenses(monkeypatch):
    user_id = _register_and_get_user_id("ask-user-expenses-4")

    old_timestamp = "2026-05-01 12:00:00"
    recent_timestamp = "2026-07-01 12:00:00"

    _create_event(
        user_id,
        event_type="repair",
        description="Давний ремонт",
        amount=8000,
        mileage=101000,
    )
    _create_event(
        user_id,
        event_type="fuel",
        description="Заправка за месяц",
        amount=3000,
        fuel_liters=45,
        mileage=101100,
    )
    _create_event(
        user_id,
        event_type="issue",
        description="Платная диагностика",
        amount=2200,
        mileage=101120,
    )

    with database_module.engine.begin() as connection:
        connection.exec_driver_sql(
            "UPDATE events SET created_at = ? WHERE description = ?",
            (old_timestamp, "Давний ремонт"),
        )
        connection.exec_driver_sql(
            "UPDATE events SET created_at = ? WHERE description = ?",
            (recent_timestamp, "Заправка за месяц"),
        )
        connection.exec_driver_sql(
            "UPDATE events SET created_at = ? WHERE description = ?",
            (recent_timestamp, "Платная диагностика"),
        )

    def fail_if_called(*args, **kwargs):
        raise AssertionError("LLM must not be called for expense queries")

    monkeypatch.setattr(main_module, "ask_deepseek", fail_if_called)

    response = client.post(
        f"/chat/ask?user_id={user_id}",
        json={"message": "Покажи расходы за месяц"},
    )

    assert response.status_code == 200
    assert response.json()["answer"] == (
        "Расходы за месяц: 5 200 ₽\n\n"
        "По категориям:\n\n"
        "* Топливо: 3 000 ₽\n"
        "* Проблемы: 2 200 ₽\n\n"
        "Последние расходы:\n\n"
        "1. Платная диагностика — 2 200 ₽\n\n"
        "2. Заправка за месяц — 3 000 ₽, 45 л"
    )


def test_chat_ask_filters_all_time_expenses(monkeypatch):
    user_id = _register_and_get_user_id("ask-user-expenses-5")

    _create_event(
        user_id,
        event_type="fuel",
        description="Старая заправка",
        amount=2000,
        fuel_liters=30,
        mileage=102000,
    )
    _create_event(
        user_id,
        event_type="repair",
        description="Старый ремонт",
        amount=6500,
        mileage=102050,
    )
    _create_event(
        user_id,
        event_type="trip",
        description="Платная парковка",
        amount=500,
        mileage=15,
    )

    def fail_if_called(*args, **kwargs):
        raise AssertionError("LLM must not be called for expense queries")

    monkeypatch.setattr(main_module, "ask_deepseek", fail_if_called)

    response = client.post(
        f"/chat/ask?user_id={user_id}",
        json={"message": "Какие были расходы за всё время?"},
    )

    assert response.status_code == 200
    assert response.json()["answer"] == (
        "Расходы за всё время: 9 000 ₽\n\n"
        "По категориям:\n\n"
        "* Топливо: 2 000 ₽\n"
        "* Ремонт: 6 500 ₽\n"
        "* Поездки: 500 ₽\n\n"
        "Последние расходы:\n\n"
        "1. Платная парковка — 500 ₽\n\n"
        "2. Старый ремонт — 6 500 ₽\n\n"
        "3. Старая заправка — 2 000 ₽, 30 л"
    )


def test_chat_ask_filters_category_specific_expenses(monkeypatch):
    user_id = _register_and_get_user_id("ask-user-expenses-6")

    _create_event(
        user_id,
        event_type="fuel",
        description="Заправка",
        amount=2800,
        fuel_liters=42,
        mileage=103000,
    )
    _create_event(
        user_id,
        event_type="repair",
        description="Ремонт подвески",
        amount=12000,
        mileage=103100,
    )
    _create_event(
        user_id,
        event_type="issue",
        description="Эвакуатор",
        amount=3500,
        mileage=103150,
    )

    def fail_if_called(*args, **kwargs):
        raise AssertionError("LLM must not be called for expense queries")

    monkeypatch.setattr(main_module, "ask_deepseek", fail_if_called)

    response = client.post(
        f"/chat/ask?user_id={user_id}",
        json={"message": "Покажи расходы на ремонт"},
    )

    assert response.status_code == 200
    assert response.json()["answer"] == (
        "Расходы за последние 5 записей: 12 000 ₽\n\n"
        "По категориям:\n\n"
        "* Ремонт: 12 000 ₽\n\n"
        "Последние расходы:\n\n"
        "1. Ремонт подвески — 12 000 ₽"
    )


def test_chat_ask_returns_no_expenses_message(monkeypatch):
    user_id = _register_and_get_user_id("ask-user-expenses-7")

    _create_event(
        user_id,
        event_type="fuel",
        description="Запись без суммы",
        amount=0,
        fuel_liters=20,
        mileage=104000,
    )
    _create_event(
        user_id,
        event_type="repair",
        description="Ремонт без суммы",
        amount=0,
        mileage=104050,
    )

    def fail_if_called(*args, **kwargs):
        raise AssertionError("LLM must not be called for expense queries")

    monkeypatch.setattr(main_module, "ask_deepseek", fail_if_called)

    response = client.post(
        f"/chat/ask?user_id={user_id}",
        json={"message": "Покажи последние расходы"},
    )

    assert response.status_code == 200
    assert response.json()["answer"] == "За выбранный период расходов не найдено."


def test_chat_ask_keeps_llm_flow_for_non_expense_questions(monkeypatch):
    user_id = _register_and_get_user_id("ask-user-expenses-8")
    captured = {}

    def fake_ask(message: str, vehicle_context: str | None = None) -> str:
        captured["message"] = message
        captured["context"] = vehicle_context
        return "LLM answer"

    monkeypatch.setattr(main_module, "ask_deepseek", fake_ask)

    response = client.post(
        f"/chat/ask?user_id={user_id}",
        json={"message": "Какой цвет у моей машины?"},
    )

    assert response.status_code == 200
    assert response.json()["answer"] == "LLM answer"
    assert captured["message"] == "Какой цвет у моей машины?"
