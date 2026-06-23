from pathlib import Path

from backend.config import Settings


def test_default_values():
    s = Settings()
    assert s.auth_token == "dev-token"
    assert s.storage_path == Path("./data/photos")
    assert s.db_path == Path("./data/pixelvault.db")
    assert s.port == 8000


def test_env_file_overrides(tmp_path):
    env_file = tmp_path / ".env"
    env_file.write_text(
        "AUTH_TOKEN=test-token\nSTORAGE_PATH=/tmp/test-storage\nDB_PATH=/tmp/test.db\nPORT=9090\n"
    )
    s = Settings(_env_file=str(env_file))
    assert s.auth_token == "test-token"
    assert s.storage_path == Path("/tmp/test-storage")
    assert s.db_path == Path("/tmp/test.db")
    assert s.port == 9090
