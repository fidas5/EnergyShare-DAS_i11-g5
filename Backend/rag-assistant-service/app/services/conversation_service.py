
from app.extensions import db
from app.models import Conversation, Message


# -----------------------------
# CREATE
# -----------------------------

def create_conversation(owner_user_id, title="New conversation"):
    conversation = Conversation(
        owner_user_id=owner_user_id,
        title=title.strip()[:255],
    )

    db.session.add(conversation)
    db.session.flush()

    return conversation


# -----------------------------
# READ
# -----------------------------

def get_conversation(conversation_id, owner_user_id):
    return db.session.execute(
        db.select(Conversation).where(
            Conversation.id == conversation_id,
            Conversation.owner_user_id == owner_user_id,
        )
    ).scalar_one_or_none()


def list_conversations(owner_user_id, page=1, limit=10):
    statement = (
        db.select(Conversation)
        .where(Conversation.owner_user_id == owner_user_id)
        .order_by(Conversation.created_at.desc(),
                  Conversation.id.desc())
    )

    return db.paginate(
        statement,
        page=page,
        per_page=limit,
        max_per_page=50,
        error_out=False,
    )


def get_history(conversation_id, limit=10):
    """Load latest messages in chronological order."""

    messages = db.session.execute(
        db.select(Message)
        .where(Message.conversation_id == conversation_id)
        .order_by(Message.id.desc())
        .limit(limit)
    ).scalars().all()

    return list(reversed(messages))


def list_messages(conversation_id, page=1, limit=20):
    statement = (
        db.select(Message)
        .where(Message.conversation_id == conversation_id)
        .order_by(Message.id.asc())
    )

    return db.paginate(
        statement,
        page=page,
        per_page=limit,
        max_per_page=100,
        error_out=False,
    )


def format_history(messages):
    lines = []

    for message in messages:
        role = "User" if message.role == "user" else "Assistant"
        lines.append(f"{role}: {message.content}")

    return "\n\n".join(lines)


# -----------------------------
# UPDATE
# -----------------------------

def rename_conversation(conversation, title):
    conversation.title = title.strip()
    return conversation


# -----------------------------
# DELETE
# -----------------------------

def delete_conversation(conversation):
    db.session.delete(conversation)


# -----------------------------
# MESSAGES
# -----------------------------

def save_message(conversation_id, role, content):
    if role not in {"user", "assistant"}:
        raise ValueError("Invalid message role")

    message = Message(
        conversation_id=conversation_id,
        role=role,
        content=content,
    )

    db.session.add(message)
    return message


# -----------------------------
# SERIALIZATION
# -----------------------------

def serialize_conversation(conversation):
    return {
        "id": conversation.id,
        "title": conversation.title,
        "created_at": conversation.created_at.isoformat(),
    }


def serialize_message(message):
    return {
        "id": message.id,
        "conversation_id": message.conversation_id,
        "role": message.role,
        "content": message.content,
        "created_at": message.created_at.isoformat(),
    }


def serialize_pagination(pagination):
    return {
        "page": pagination.page,
        "limit": pagination.per_page,
        "total": pagination.total,
        "total_pages": pagination.pages,
        "has_next": pagination.has_next,
        "has_prev": pagination.has_prev,
    }
