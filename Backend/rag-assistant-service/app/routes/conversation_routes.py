
from flask import Blueprint, jsonify, request, current_app

from app.extensions import db
from app.services.auth_context import current_owner_id
from app.services.conversation_service import (
    create_conversation,
    get_conversation,
    list_conversations,
    list_messages,
    rename_conversation,
    delete_conversation,
    serialize_conversation,
    serialize_message,
    serialize_pagination,
)


conversation_bp = Blueprint(
    "conversations",
    __name__,
    url_prefix="/api/ai/conversations",
)


def get_owner():
    try:
        return current_owner_id()
    except RuntimeError:
        return None


def parse_pagination(default_limit=10, max_limit=50):
    """Validate page and limit query parameters."""

    try:
        page = int(request.args.get("page", "1"))
        limit = int(
            request.args.get("limit", str(default_limit))
        )
    except (ValueError, TypeError):
        return None

    if page < 1 or not 1 <= limit <= max_limit:
        return None

    return page, limit


# -------------------------------------
# CREATE CONVERSATION
# POST /api/ai/conversations
# -------------------------------------

@conversation_bp.post("")
def create():
    owner = get_owner()

    if owner is None:
        return jsonify({
            "error": "Authentication is not configured"
        }), 503

    data = request.get_json(silent=True)

    if data is None:
        data = {}

    if not isinstance(data, dict):
        return jsonify({
            "error": "JSON object required"
        }), 400

    title = data.get("title", "New conversation")

    if (
        not isinstance(title, str)
        or not title.strip()
        or len(title.strip()) > 255
    ):
        return jsonify({
            "error": "Title must contain 1 to 255 characters"
        }), 400

    try:
        conversation = create_conversation(
            owner_user_id=owner,
            title=title,
        )

        db.session.commit()

        return jsonify({
            "conversation": serialize_conversation(
                conversation
            )
        }), 201

    except Exception:
        db.session.rollback()
        current_app.logger.exception(
            "Failed to create conversation"
        )

        return jsonify({
            "error": "Failed to create conversation"
        }), 500


# -------------------------------------
# LIST CONVERSATIONS
# GET /api/ai/conversations
# -------------------------------------

@conversation_bp.get("")
def get_all():
    owner = get_owner()

    if owner is None:
        return jsonify({
            "error": "Authentication is not configured"
        }), 503

    params = parse_pagination()

    if params is None:
        return jsonify({
            "error": "Invalid page or limit"
        }), 400

    page, limit = params

    pagination = list_conversations(
        owner_user_id=owner,
        page=page,
        limit=limit,
    )

    return jsonify({
        "conversations": [
            serialize_conversation(item)
            for item in pagination.items
        ],
        "pagination": serialize_pagination(pagination),
    }), 200


# -------------------------------------
# GET CONVERSATION + MESSAGES
# GET /api/ai/conversations/<id>
# -------------------------------------

@conversation_bp.get("/<int:conversation_id>")
def get_one(conversation_id):
    owner = get_owner()

    if owner is None:
        return jsonify({
            "error": "Authentication is not configured"
        }), 503

    conversation = get_conversation(
        conversation_id,
        owner,
    )

    if conversation is None:
        return jsonify({
            "error": "Conversation not found"
        }), 404

    params = parse_pagination(
        default_limit=20,
        max_limit=100,
    )

    if params is None:
        return jsonify({
            "error": "Invalid page or limit"
        }), 400

    page, limit = params

    pagination = list_messages(
        conversation_id=conversation.id,
        page=page,
        limit=limit,
    )

    return jsonify({
        "conversation": serialize_conversation(
            conversation
        ),
        "messages": [
            serialize_message(message)
            for message in pagination.items
        ],
        "pagination": serialize_pagination(pagination),
    }), 200


# -------------------------------------
# UPDATE CONVERSATION
# PATCH /api/ai/conversations/<id>
# -------------------------------------

@conversation_bp.patch("/<int:conversation_id>")
def update(conversation_id):
    owner = get_owner()

    if owner is None:
        return jsonify({
            "error": "Authentication is not configured"
        }), 503

    conversation = get_conversation(
        conversation_id,
        owner,
    )

    if conversation is None:
        return jsonify({
            "error": "Conversation not found"
        }), 404

    data = request.get_json(silent=True)

    if not isinstance(data, dict):
        return jsonify({
            "error": "JSON object required"
        }), 400

    title = data.get("title")

    if (
        not isinstance(title, str)
        or not title.strip()
        or len(title.strip()) > 255
    ):
        return jsonify({
            "error": "Title must contain 1 to 255 characters"
        }), 400

    try:
        rename_conversation(
            conversation,
            title,
        )

        db.session.commit()

        return jsonify({
            "conversation": serialize_conversation(
                conversation
            )
        }), 200

    except Exception:
        db.session.rollback()
        current_app.logger.exception(
            "Failed to rename conversation"
        )

        return jsonify({
            "error": "Failed to rename conversation"
        }), 500


# -------------------------------------
# DELETE CONVERSATION
# DELETE /api/ai/conversations/<id>
# -------------------------------------

@conversation_bp.delete("/<int:conversation_id>")
def delete(conversation_id):
    owner = get_owner()

    if owner is None:
        return jsonify({
            "error": "Authentication is not configured"
        }), 503

    conversation = get_conversation(
        conversation_id,
        owner,
    )

    if conversation is None:
        return jsonify({
            "error": "Conversation not found"
        }), 404

    try:
        delete_conversation(conversation)
        db.session.commit()

        return jsonify({
            "message": "Conversation deleted successfully",
            "conversation_id": conversation_id,
        }), 200

    except Exception:
        db.session.rollback()
        current_app.logger.exception(
            "Failed to delete conversation"
        )

        return jsonify({
            "error": "Failed to delete conversation"
        }), 500
