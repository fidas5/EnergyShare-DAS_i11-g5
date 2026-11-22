
from flask import Flask, jsonify
from flask_cors import CORS

from app.config import Config
from app.routes.chat_routes import chat_bp


def create_app():
    app = Flask(__name__)

    app.config.from_object(Config)

    CORS(
        app,
        resources={
            r"/api/*": {
                "origins": ["http://localhost:5173"]
            }
        }
    )

    app.register_blueprint(chat_bp)

    @app.route("/health", methods=["GET"])
    def health():
        return jsonify({
            "service": "energyshare-rag-assistant",
            "status": "UP",
            "message": "Flask service is running"
        }), 200

    return app
