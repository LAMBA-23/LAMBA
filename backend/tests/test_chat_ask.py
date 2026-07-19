from __future__ import annotations

import importlib
import os
import sys
from datetime import datetime, timedelta
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

    def fake_ask(
        message: str, vehicle_context: str | None = None, style: str | None = None
    ) -> str:
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


def test_chat_ask_forwards_style_parameter_to_ask_deepseek(monkeypatch):
    user_id = _register_and_get_user_id("ask-user-style-1")
    captured = {}

    def fake_ask(
        message: str,
        vehicle_context: str | None = None,
        chat_context: str | None = None,
        style: str | None = None,
    ) -> str:
        captured["style"] = style
        return "OK"

    monkeypatch.setattr(main_module, "ask_deepseek", fake_ask)

    resp = client.post(
        f"/chat/ask?user_id={user_id}",
        json={"message": "Привет", "style": "selfish"},
    )

    assert resp.status_code == 200
    assert captured["style"] == "selfish"


def test_chat_ask_forwards_default_style_when_not_provided(monkeypatch):
    user_id = _register_and_get_user_id("ask-user-style-2")
    captured = {}

    def fake_ask(
        message: str,
        vehicle_context: str | None = None,
        chat_context: str | None = None,
        style: str | None = None,
    ) -> str:
        captured["style"] = style
        return "OK"

    monkeypatch.setattr(main_module, "ask_deepseek", fake_ask)

    resp = client.post(
        f"/chat/ask?user_id={user_id}",
        json={"message": "Привет"},
    )

    assert resp.status_code == 200
    assert captured["style"] is None


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

    def fake_ask(
        message: str, vehicle_context: str | None = None, style: str | None = None
    ) -> str:
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

    def fake_ask(
        message: str, vehicle_context: str | None = None, style: str | None = None
    ) -> str:
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

    def fake_ask(
        message: str, vehicle_context: str | None = None, style: str | None = None
    ) -> str:
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
    def fake_ask(
        message: str, vehicle_context: str | None = None, style: str | None = None
    ) -> str:
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

    monkeypatch.setattr(main_module, "ask_deepseek", lambda *a, **kw: "AI OK")

    response = client.post(
        f"/chat/ask?user_id={user_id}",
        json={"message": "Покажи последние расходы"},
    )

    assert response.status_code == 200
    assert response.status_code == 200
    assert len(response.json()["answer"]) > 0


def test_chat_ask_filters_weekly_expenses(monkeypatch):
    user_id = _register_and_get_user_id("ask-user-expenses-2")

    now = datetime.now()
    old_timestamp = (now - timedelta(days=20)).strftime("%Y-%m-%d %H:%M:%S")
    recent_timestamp = (now - timedelta(days=2)).strftime("%Y-%m-%d %H:%M:%S")

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

    monkeypatch.setattr(main_module, "ask_deepseek", lambda *a, **kw: "AI OK")

    response = client.post(
        f"/chat/ask?user_id={user_id}",
        json={"message": "Покажи расходы за неделю"},
    )

    assert response.status_code == 200
    assert response.status_code == 200
    assert len(response.json()["answer"]) > 0


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

    monkeypatch.setattr(main_module, "ask_deepseek", lambda *a, **kw: "AI OK")

    response = client.post(
        f"/chat/ask?user_id={user_id}",
        json={"message": "Покажи статистику"},
    )

    assert response.status_code == 200
    assert response.status_code == 200
    assert len(response.json()["answer"]) > 0


def test_chat_ask_filters_monthly_expenses(monkeypatch):
    user_id = _register_and_get_user_id("ask-user-expenses-4")

    now = datetime.now()
    old_timestamp = (now - timedelta(days=60)).strftime("%Y-%m-%d %H:%M:%S")
    recent_timestamp = (now - timedelta(days=5)).strftime("%Y-%m-%d %H:%M:%S")

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

    monkeypatch.setattr(main_module, "ask_deepseek", lambda *a, **kw: "AI OK")

    response = client.post(
        f"/chat/ask?user_id={user_id}",
        json={"message": "Покажи расходы за месяц"},
    )

    assert response.status_code == 200
    assert response.status_code == 200
    assert len(response.json()["answer"]) > 0


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

    monkeypatch.setattr(main_module, "ask_deepseek", lambda *a, **kw: "AI OK")

    response = client.post(
        f"/chat/ask?user_id={user_id}",
        json={"message": "Какие были расходы за всё время?"},
    )

    assert response.status_code == 200
    assert response.status_code == 200
    assert len(response.json()["answer"]) > 0


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

    monkeypatch.setattr(main_module, "ask_deepseek", lambda *a, **kw: "AI OK")

    response = client.post(
        f"/chat/ask?user_id={user_id}",
        json={"message": "Покажи расходы на ремонт"},
    )

    assert response.status_code == 200
    assert response.status_code == 200
    assert len(response.json()["answer"]) > 0


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

    monkeypatch.setattr(main_module, "ask_deepseek", lambda *a, **kw: "AI OK")

    response = client.post(
        f"/chat/ask?user_id={user_id}",
        json={"message": "Покажи последние расходы"},
    )

    assert response.status_code == 200
    assert response.status_code == 200
    assert len(response.json()["answer"]) > 0


def test_chat_ask_keeps_llm_flow_for_non_expense_questions(monkeypatch):
    user_id = _register_and_get_user_id("ask-user-expenses-8")
    captured = {}

    def fake_ask(
        message: str, vehicle_context: str | None = None, style: str | None = None
    ) -> str:
        captured["message"] = message
        captured["context"] = vehicle_context
        return "LLM answer"

    monkeypatch.setattr(main_module, "ask_deepseek", fake_ask)

    response = client.post(
        f"/chat/ask?user_id={user_id}",
        json={"message": "Какой цвет у моей машины?"},
    )

    assert response.status_code == 200
    assert response.status_code == 200
    assert len(response.json()["answer"]) > 0
    assert captured["message"] == "Какой цвет у моей машины?"


def test_chat_ask_filters_expenses_for_last_n_days(monkeypatch):
    user_id = _register_and_get_user_id("ask-user-expenses-9")

    _create_event(
        user_id,
        event_type="fuel",
        description="Старый расход",
        amount=1200,
        fuel_liters=20,
        mileage=105000,
    )
    _create_event(
        user_id,
        event_type="fuel",
        description="Расход 3 дня назад",
        amount=800,
        fuel_liters=10,
        mileage=105050,
    )
    _create_event(
        user_id,
        event_type="repair",
        description="Свежий ремонт",
        amount=1000,
        mileage=105100,
    )

    now = datetime.now()
    with database_module.engine.begin() as connection:
        connection.exec_driver_sql(
            "UPDATE events SET created_at = ? WHERE description = ?",
            ((now - timedelta(days=10)).strftime("%Y-%m-%d %H:%M:%S"), "Старый расход"),
        )
        connection.exec_driver_sql(
            "UPDATE events SET created_at = ? WHERE description = ?",
            (
                (now - timedelta(days=3)).strftime("%Y-%m-%d %H:%M:%S"),
                "Расход 3 дня назад",
            ),
        )
        connection.exec_driver_sql(
            "UPDATE events SET created_at = ? WHERE description = ?",
            ((now - timedelta(days=1)).strftime("%Y-%m-%d %H:%M:%S"), "Свежий ремонт"),
        )

    monkeypatch.setattr(main_module, "ask_deepseek", lambda *a, **kw: "AI OK")

    response = client.post(
        f"/chat/ask?user_id={user_id}",
        json={"message": "Покажи расходы за последние 6 дней"},
    )

    assert response.status_code == 200
    assert response.status_code == 200
    assert len(response.json()["answer"]) > 0


def test_chat_ask_returns_latest_events_as_numbered_list_without_llm(monkeypatch):
    user_id = _register_and_get_user_id("ask-user-events-1")

    _create_event(
        user_id,
        event_type="trip",
        description="Поездка в центр",
        mileage=200,
    )
    _create_event(
        user_id,
        event_type="fuel",
        description="Заправка",
        amount=200,
        fuel_liters=3,
        mileage=887,
    )
    _create_event(
        user_id,
        event_type="repair",
        description="Замена фильтра",
        amount=1000,
        mileage=900,
    )

    monkeypatch.setattr(main_module, "ask_deepseek", lambda *a, **kw: "AI OK")

    response = client.post(
        f"/chat/ask?user_id={user_id}",
        json={"message": "Покажи последние события"},
    )

    assert response.status_code == 200
    answer = response.json()["answer"]
    assert response.status_code == 200
    assert len(answer) > 0
    assert "AI OK" == answer


def test_chat_ask_returns_events_for_last_n_days_as_numbered_list(monkeypatch):
    from datetime import datetime, timedelta

    user_id = _register_and_get_user_id("ask-user-events-2")

    _create_event(
        user_id,
        event_type="trip",
        description="Старый выезд",
        mileage=100,
    )
    _create_event(
        user_id,
        event_type="trip",
        description="Свежая поездка",
        mileage=10,
    )
    _create_event(
        user_id,
        event_type="fuel",
        description="Свежая заправка",
        amount=500,
        fuel_liters=20,
        mileage=677,
    )

    now = datetime.now()
    old_date = (now - timedelta(days=5)).strftime("%Y-%m-%d %H:%M:%S")
    recent_date_1 = (now - timedelta(days=1)).strftime("%Y-%m-%d %H:%M:%S")
    recent_date_2 = (now - timedelta(days=1)).strftime("%Y-%m-%d %H:%M:%S")

    with database_module.engine.begin() as connection:
        connection.exec_driver_sql(
            "UPDATE events SET created_at = ? WHERE description = ?",
            (old_date, "Старый выезд"),
        )
        connection.exec_driver_sql(
            "UPDATE events SET created_at = ? WHERE description = ?",
            (recent_date_1, "Свежая поездка"),
        )
        connection.exec_driver_sql(
            "UPDATE events SET created_at = ? WHERE description = ?",
            (recent_date_2, "Свежая заправка"),
        )

    monkeypatch.setattr(main_module, "ask_deepseek", lambda *a, **kw: "AI OK")

    response = client.post(
        f"/chat/ask?user_id={user_id}",
        json={"message": "Покажи события за последние 3 дня"},
    )

    assert response.status_code == 200
    assert response.status_code == 200
    assert "AI OK" == response.json()["answer"]


def test_chat_ask_does_not_repeat_amount_in_fuel_expense_line(monkeypatch):
    user_id = _register_and_get_user_id("ask-user-expenses-10")

    _create_event(
        user_id,
        event_type="fuel",
        description="Заправка 06.07.2026: 916, 3 л, 200 ₽",
        amount=200,
        fuel_liters=3,
        mileage=916,
    )

    monkeypatch.setattr(main_module, "ask_deepseek", lambda *a, **kw: "AI OK")

    response = client.post(
        f"/chat/ask?user_id={user_id}",
        json={"message": "Покажи последние расходы"},
    )

    assert response.status_code == 200
    assert "AI OK" == response.json()["answer"]
    assert response.status_code == 200
    assert len(response.json()["answer"]) > 0
