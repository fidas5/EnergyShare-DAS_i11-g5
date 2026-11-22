
import hashlib
import json
from pathlib import Path

from langchain_community.document_loaders import TextLoader
from langchain_text_splitters import RecursiveCharacterTextSplitter

from app.rag.vector_store import (
    KNOWLEDGE_BASE_DIR,
    get_vector_store,
)


SUPPORTED_EXTENSIONS = {".txt", ".md",".rst", ".csv", ".json", ".jsonl", ".xml", ".html", ".htm",".pdf", ".docx", ".pptx", ".xlsx", ".odt", ".ods", ".odp", ".epub"}

EXCLUDED_DIRS = {
    ".git",
    ".venv",
    "venv",
    "__pycache__",
    "node_modules",
    "dist",
    "build",
}


def ingest_energyshare(
    project_name="energyshare",
    chunk_size=500,
    chunk_overlap=50,
):
    """
    Load EnergyShare documents, split them into chunks,
    generate embeddings, and store them in ChromaDB.

    Re-ingestion replaces the existing chunks for the
    specified project.
    """

    # STEP 1: Locate the knowledge base
    project_path = KNOWLEDGE_BASE_DIR / project_name

    if not project_path.is_dir():
        raise ValueError(
            f"Knowledge base not found: {project_path}"
        )

    if not 0 <= chunk_overlap < chunk_size:
        raise ValueError("Invalid chunk size or overlap")

    print(f"Loading knowledge base: {project_path}")

    # STEP 2: Discover supported documents
    documents = []

    for file_path in sorted(project_path.rglob("*")):
        if not file_path.is_file():
            continue

        if any(
            part in EXCLUDED_DIRS
            for part in file_path.parts
        ):
            continue

        if file_path.suffix.lower() not in SUPPORTED_EXTENSIONS:
            continue

        try:
            # STEP 3: Load text
            loader = TextLoader(
                str(file_path),
                encoding="utf-8"
            )

            loaded_docs = loader.load()

            # STEP 4: Enrich metadata
            relative_path = file_path.relative_to(
                KNOWLEDGE_BASE_DIR
            ).as_posix()

            for doc in loaded_docs:
                doc.metadata = {
                    "project": project_name,
                    "category": file_path.stem,
                    "source": relative_path,
                    "file_type": file_path.suffix.lower(),
                }

                if doc.page_content.strip():
                    documents.append(doc)

            print(f"Loaded: {relative_path}")

        except Exception as error:
            print(f"Error loading {file_path.name}: {error}")

    if not documents:
        raise ValueError(
            "No valid documents found in the knowledge base"
        )

    print(f"Documents loaded: {len(documents)}")

    # STEP 5: Split documents into chunks
    splitter = RecursiveCharacterTextSplitter(
        chunk_size=chunk_size,
        chunk_overlap=chunk_overlap,
        separators=["\n\n", "\n", ". ", " ", ""],
    )

    chunks = splitter.split_documents(documents)

    # Prepare stable IDs and chunk metadata
    chunk_ids = []

    for index, chunk in enumerate(chunks):
        source = chunk.metadata["source"]

        chunk.metadata["chunk_index"] = index
        chunk.metadata["embedding_model"] = (
            "all-MiniLM-L6-v2"
        )

        raw_id = (
            f"{project_name}:{source}:{index}:"
            f"{chunk.page_content}"
        )

        chunk_id = hashlib.sha256(
            raw_id.encode("utf-8")
        ).hexdigest()

        chunk_ids.append(chunk_id)

    print(f"Chunks created: {len(chunks)}")

    # STEP 6 + 7: Initialize embeddings and ChromaDB
    db = get_vector_store()

    # STEP 8: Replace previously indexed project chunks
    # Embed first, before modifying the stored collection.
    embeddings = db._embedding_function.embed_documents(
        [chunk.page_content for chunk in chunks]
    )

    # Chroma's native upsert handles deterministic IDs.
    # First remove old chunks belonging only to this project.
    existing = db.get(
        where={"project": project_name},
        include=["metadatas"],
    )

    if existing["ids"]:
        db.delete(ids=existing["ids"])
        print(
            f"Removed {len(existing['ids'])} old chunks"
        )

    print("Saving embeddings into ChromaDB...")

    # Use batches to avoid oversized writes.
    batch_size = 100

    for start in range(0, len(chunks), batch_size):
        end = start + batch_size
        batch = chunks[start:end]

        db._collection.upsert(
            ids=chunk_ids[start:end],
            documents=[doc.page_content for doc in batch],
            metadatas=[doc.metadata for doc in batch],
            embeddings=embeddings[start:end],
        )

    total = db._collection.count()

    print("EnergyShare ingestion completed!")
    print(f"Chunks indexed: {len(chunks)}")
    print(f"Total chunks in collection: {total}")

    return {
        "status": "success",
        "project": project_name,
        "documents_loaded": len(documents),
        "chunks_added": len(chunks),
        "total_chunks": total,
    }


if __name__ == "__main__":
    result = ingest_energyshare()
    print(json.dumps(result, indent=2))
