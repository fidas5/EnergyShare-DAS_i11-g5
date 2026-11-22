
from datetime import datetime, timezone

from app.extensions import db


class Message(db.Model):
    __tablename__ = "messages"

    id = db.Column(
        db.BigInteger,
        primary_key=True,
        autoincrement=True
    )

    conversation_id = db.Column(
        db.BigInteger,
        db.ForeignKey("conversations.id", ondelete="CASCADE"),
        nullable=False,
        index=True
    )

    role = db.Column(
        db.String(20),
        nullable=False
    )

    content = db.Column(
        db.Text,
        nullable=False
    )

    created_at = db.Column(
        db.DateTime,
        nullable=False,
        default=lambda: datetime.now(timezone.utc).replace(tzinfo=None)
    )

    conversation = db.relationship(
        "Conversation",
        back_populates="messages"
    )

    __table_args__ = (
        db.CheckConstraint(
            "role IN ('user', 'assistant')",
            name="ck_message_role"
        ),
    )
