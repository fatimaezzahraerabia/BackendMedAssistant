from flask import Flask
from r_rag.routes import rag_bp
from r_rag.utils import load_knowledge_base

def create_app():
    """
    Application factory for the RAG service.
    """
    app = Flask(__name__)

    # Load the knowledge base once at startup
    df_combined, tfidf_matrix, vectorizer = load_knowledge_base()
    
    if df_combined is None:
        print("CRITICAL: Knowledge base failed to load. The service will not be able to respond to queries.")
        # Store empty KB to prevent crashes, but endpoints will return errors
        app.config["KNOWLEDGE_BASE"] = {}
    else:
        # Make the loaded data available to the blueprints/routes via the app context
        app.config["KNOWLEDGE_BASE"] = {
            "df_combined": df_combined,
            "tfidf_matrix": tfidf_matrix,
            "vectorizer": vectorizer
        }

    # Register the blueprint for RAG endpoints
    app.register_blueprint(rag_bp, url_prefix="/api/rag")

    return app

if __name__ == "__main__":
    app = create_app()
    # Running on port 5002 to avoid conflict with the diagnosis service
    app.run(debug=True, port=5002)
