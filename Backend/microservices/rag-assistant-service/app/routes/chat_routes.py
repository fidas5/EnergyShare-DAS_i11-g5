
from flask import Blueprint, jsonify, request, current_app

from app.extensions import db
from app.rag.chatbot import ask_question
from app.services.auth_context import current_owner_id
from app.services.conversation_service import (
    create_conversation,
    get_conversation,
    get_history,
    format_history,
    save_message,
)


chat_bp = Blueprint(
    "chat",
    __name__,
    url_prefix="/api/ai",
)


@chat_bp.post("/chat")
def chat():
    try:
        owner = current_owner_id()
    except RuntimeError:
        return jsonify({
            "error": "Authentication is not configured"
        }), 503

    data = request.get_json(silent=True)

    if not isinstance(data, dict):
        return jsonify({
            "error": "JSON object required"
        }), 400

    question = data.get("question")
    lang = data.get("lang", "en")
    conversation_id = data.get("conversation_id")

    if (
        not isinstance(question, str)
        or not question.strip()
    ):
        return jsonify({
            "error": "Question is required"
        }), 400

    question = question.strip()

    if len(question) > 4000:
        return jsonify({
            "error": "Question is too long"
        }), 400

    if lang not in {"en", "fr", "ar"}:
        return jsonify({
            "error": "Unsupported language"
        }), 400

    conversation = None
    history_text = ""

    if conversation_id is not None:
        if (
            type(conversation_id) is not int
            or conversation_id <= 0
        ):
            return jsonify({
                "error": "Invalid conversation_id"
            }), 400

        conversation = get_conversation(
            conversation_id,
            owner,
        )

        if conversation is None:
            return jsonify({
                "error": "Conversation not found"
            }), 404

        history = get_history(conversation.id)
        history_text = format_history(history)

    try:
        # Generate RAG response
        result = ask_question(
            query=question,
            lang=lang,
            project="energyshare",
            chat_history=history_text,
        )

        # Create conversation if needed
        if conversation is None:
            conversation = create_conversation(
                owner_user_id=owner,
                title=question[:100],
            )

        # Save both messages
        save_message(
            conversation.id,
            "user",
            question,
        )

        save_message(
            conversation.id,
            "assistant",
            result["answer"],
        )

        db.session.commit()

        return jsonify({
            "conversation_id": conversation.id,
            "question": result["question"],
            "answer": result["answer"],
            "sources": result["sources"],
            "chunks_retrieved": result["chunks_retrieved"],
        }), 200

    except Exception:
        db.session.rollback()
        current_app.logger.exception(
            "Chat request failed"
        )

        return jsonify({
            "error": "Failed to generate or save response"
        }), 503
