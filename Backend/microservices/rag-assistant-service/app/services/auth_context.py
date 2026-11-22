
import os

from flask import current_app


def current_owner_id():
    """
    Local-development identity only.

    Replace this with a verified Keycloak JWT subject
    before deploying the service publicly.
    """
    if not current_app.debug:
        raise RuntimeError(
            "Authentication is not configured"
        )

    return os.getenv(
        "DEV_USER_ID",
        "energyshare-dev-user",
    )
