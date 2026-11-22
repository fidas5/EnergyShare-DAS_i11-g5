
import os

from app import create_app
import py_eureka_client.eureka_client as eureka_client

app = create_app()

if __name__ == "__main__":
    port = int(os.getenv("PORT", "5005"))
    debug = os.getenv("FLASK_DEBUG", "false").lower() == "true"

    eureka_server = os.getenv(
        "EUREKA_SERVER",
        "http://localhost:8761/eureka/"
    )

    # Register the Flask microservice with Eureka
    # When the debug reloader is active, register only in its child process.
    if not debug or os.environ.get("WERKZEUG_RUN_MAIN") == "true":
        eureka_client.init(
            eureka_server=eureka_server,
            app_name="rag-assistant-service",
            instance_port=port,
            instance_host="localhost",
            instance_ip="127.0.0.1"
        )

        print("RAG Assistant registered with Eureka")

    app.run(
        host="127.0.0.1",
        port=port,
        debug=debug
    )
