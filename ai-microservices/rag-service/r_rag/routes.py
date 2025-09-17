from flask import Blueprint, request, jsonify, current_app

rag_bp = Blueprint("rag", __name__)

@rag_bp.route("/query", methods=["POST"])
def query_rag():
    """
    Handles incoming queries to the RAG service.
    Expects a JSON payload with a "query" key.
    """
    data = request.get_json()
    if not data or "query" not in data:
        return jsonify({"error": "Missing 'query' in request body"}), 400

    user_query = data["query"]

    # The knowledge base is loaded at startup and attached to the app context
    kb = current_app.config.get("KNOWLEDGE_BASE")
    if not kb or "df_combined" not in kb:
        return jsonify({"error": "Knowledge base is not available"}), 503 # Service Unavailable

    df_combined = kb["df_combined"]
    tfidf_matrix = kb["tfidf_matrix"]
    vectorizer = kb["vectorizer"]
    
    # Delegate the core logic to the service function
    from .services import find_relevant_info
    response_message = find_relevant_info(user_query, df_combined, tfidf_matrix, vectorizer)

    return jsonify(response_message)
