
import os

from dotenv import load_dotenv
from sqlalchemy.engine import URL

load_dotenv()


class Config:
    SECRET_KEY = os.getenv(
        "SECRET_KEY", "development-secret"
    )

    SQLALCHEMY_DATABASE_URI = URL.create(
        drivername="mysql+pymysql",
        username=os.getenv("MYSQL_USER", "energyshare_rag"),
        password=os.getenv("MYSQL_PASSWORD", ""),
        host=os.getenv("MYSQL_HOST", "localhost"),
        port=int(os.getenv("MYSQL_PORT", "3306")),
        database=os.getenv(
            "MYSQL_DATABASE", "energyshare_rag_db"
        ),
        query={"charset": "utf8mb4"},
    )

    SQLALCHEMY_TRACK_MODIFICATIONS = False
