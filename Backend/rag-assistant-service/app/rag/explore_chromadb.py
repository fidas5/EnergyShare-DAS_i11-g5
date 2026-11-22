
import json
from collections import Counter
from datetime import datetime
from pathlib import Path

from app.rag.vector_store import (
    BASE_DIR,
    get_vector_store,
)


def explore_chromadb():
    output_dir = BASE_DIR / "chroma_export"
    output_dir.mkdir(parents=True, exist_ok=True)

    db = get_vector_store()

    # Retrieve stored chunks and metadata
    data = db.get(
        include=["documents", "metadatas"]
    )

    ids = data["ids"]
    documents = data["documents"]
    metadatas = data["metadatas"]

    # Build export records
    records = []

    for index, chunk_id in enumerate(ids):
        records.append({
            "id": chunk_id,
            "content": documents[index],
            "metadata": metadatas[index] or {},
        })

    # Group chunks by project
    projects = Counter(
        record["metadata"].get("project", "unknown")
        for record in records
    )

    # General statistics
    stats = {
        "total_chunks": len(records),
        "unique_ids": len(set(ids)),
        "projects": dict(projects),
        "embedding_model": "all-MiniLM-L6-v2",
        "collection": "energyshare_kb",
    }

    # Export statistics
    (output_dir / "statistiques.json").write_text(
        json.dumps(stats, indent=2, ensure_ascii=False),
        encoding="utf-8",
    )

    # Export all chunks
    export = {
        "export_date": datetime.now().isoformat(),
        "statistics": stats,
        "documents": records,
    }

    (output_dir / "chromadb_export_complet.json").write_text(
        json.dumps(export, indent=2, ensure_ascii=False),
        encoding="utf-8",
    )

    # Export IDs
    (output_dir / "tous_les_ids.txt").write_text(
        "\n".join(ids),
        encoding="utf-8",
    )

    # Export readable chunks
    with (output_dir / "chunks.txt").open(
        "w", encoding="utf-8"
    ) as file:
        for index, record in enumerate(records, start=1):
            meta = record["metadata"]

            file.write(f"CHUNK {index}\n")
            file.write(f"ID: {record['id']}\n")
            file.write(
                f"Source: {meta.get('source', 'unknown')}\n"
            )
            file.write(
                f"Category: {meta.get('category', 'unknown')}\n"
            )
            file.write(
                f"Content:\n{record['content']}\n"
            )
            file.write("-" * 60 + "\n\n")

    # Export a readable summary
    summary = [
        "# EnergyShare ChromaDB Export",
        "",
        f"Total chunks: {len(records)}",
        f"Unique IDs: {len(set(ids))}",
        "",
        "## Projects",
    ]

    for project, count in projects.items():
        summary.append(f"- {project}: {count} chunks")

    (output_dir / "RESUME.md").write_text(
        "\n".join(summary),
        encoding="utf-8",
    )

    print(json.dumps(stats, indent=2))
    print(f"Export saved to: {output_dir}")

    return stats


if __name__ == "__main__":
    explore_chromadb()
