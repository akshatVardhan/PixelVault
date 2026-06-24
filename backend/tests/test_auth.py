import pytest
from fastapi import HTTPException
from fastapi.security import HTTPAuthorizationCredentials

from backend.api.auth import get_current_user
from backend.config import settings


@pytest.mark.asyncio
async def test_auth_valid_token():
    settings.auth_token = "test-token"
    creds = HTTPAuthorizationCredentials(scheme="Bearer", credentials="test-token")
    result = await get_current_user(credentials=creds)
    assert result == "test-token"


@pytest.mark.asyncio
async def test_auth_invalid_token():
    settings.auth_token = "test-token"
    creds = HTTPAuthorizationCredentials(scheme="Bearer", credentials="wrong-token")
    with pytest.raises(HTTPException) as exc:
        await get_current_user(credentials=creds)
    assert exc.value.status_code == 401
