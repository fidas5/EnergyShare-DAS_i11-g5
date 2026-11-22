
from flask import Flask, jsonify
from flask_cors import CORS
from sqlalchemy import text

from app.config import Config
from app.extensions import db, migrate
from app.routes.chat_routes import chat_bp
from app.routes.conversation_routes import conversation_bp

import app.models


def create_app():
    app = Flask(__name__)
    app.config.from_object(Config)

    # Initialize MySQL and migrations
    db.init_app(app)
    migrate.init_app(app, db)

    # Enable CORS for React
    CORS(
        app,
        resources={
            r"/api/*": {
                "origins": ["http://localhost:5173"]
            }
        }
    )

    app.register_blueprint(chat_bp)
    app.register_blueprint(conversation_bp)
    
    @app.get("/health")
    def health():
        return jsonify({
            "service": "energyshare-rag-assistant",
            "status": "UP"
        }), 200

    @app.get("/health/db")
    def database_health():
        try:
            db.session.execute(text("SELECT 1"))

            return jsonify({
                "database": "MySQL",
                "status": "UP"
            }), 200

        except Exception:
            app.logger.exception("MySQL connection failed")

            return jsonify({
                "database": "MySQL",
                "status": "DOWN"
            }), 503

    return app
