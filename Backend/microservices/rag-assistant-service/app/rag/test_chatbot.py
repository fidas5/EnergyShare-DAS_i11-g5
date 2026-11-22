
from app.rag.chatbot import ask_question


def main():
    print("=" * 60)
    print("ENERGYSHARE RAG ASSISTANT")
    print("=" * 60)

    while True:
        print("\nChoose a language:")
        print("1. English")
        print("2. French")
        print("3. Arabic")
        print("0. Exit")

        choice = input("\nChoice: ").strip()

        languages = {
            "1": "en",
            "2": "fr",
            "3": "ar",
        }

        if choice == "0":
            break

        if choice not in languages:
            print("Invalid choice.")
            continue

        lang = languages[choice]

        print("\nAsk an EnergyShare question.")
        print("Type 'exit' to change language.\n")

        while True:
            question = input("You: ").strip()

            if question.lower() == "exit":
                break

            if not question:
                continue

            try:
                result = ask_question(
                    query=question,
                    lang=lang,
                )

                print("\nEnergyShare Assistant:")
                print(result["answer"])

                print("\nSources:")
                for source in result["sources"]:
                    print(f" - {source}")

                print(
                    "\nChunks retrieved:",
                    result["chunks_retrieved"]
                )

            except Exception as error:
                print(
                    f"\nChatbot error: "
                    f"{type(error).__name__}: {error}"
                )


if __name__ == "__main__":
    main()
