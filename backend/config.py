from pathlib import Path

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    auth_token: str = "dev-token"
    storage_path: Path = Path("./data/photos")
    db_path: Path = Path("./data/pixelvault.db")
    port: int = 8000

    model_config = SettingsConfigDict(env_file=".env")


settings = Settings()
