from flask import jsonify
from .model import predict_disease
from .utils import normalize_string
from .constants import DYNAMIC_DIAGNOSTIC_QUESTIONS, URGENT_SYMPTOMS
import google.generativeai as genai
from flask import current_app
import os

user_sessions = {}

def get_gemini_model():
    api_key = os.environ.get("GEMINI_API_KEY")
    if not api_key:
        api_key = current_app.config.get("GEMINI_API_KEY")
        if not api_key:
            raise ValueError("Gemini API key not found in environment variables or Flask config.")
    genai.configure(api_key=api_key)
    model = genai.GenerativeModel('gemini-1.5-flash')
    return model

def handle_prediction(data):
    # Ici tu mets la logique que tu as dans ton app.py (sessions, RAG, Cohere…)
    # mais organisée en fonctions
    return jsonify({"message": "TODO"})

def handle_gemini_chat(message, session_id):
    if not message:
        return jsonify({"error": "No message provided"}), 400

    if session_id not in user_sessions:
        user_sessions[session_id] = {
            "history": [],
            "symptoms": []
        }

    session = user_sessions[session_id]

    # Convert history to the format expected by Gemini
    gemini_history = []
    for msg in session["history"]:
        role = "user" if msg["role"] == "USER" else "model"
        gemini_history.append({"role": role, "parts": [msg["message"]]})

    # Construct a more detailed prompt
    history_items = session["history"][-5:] # Get the last 5 messages
    history_str = "\n".join([f"Role: {msg['role']}, Message: {msg['message']}" for msg in history_items])

    prompt = f"""
    Vous êtes un assistant médical conversationnel. Votre objectif est de dialoguer avec le patient pour comprendre ses symptômes de manière naturelle et empathique.
    Voici les derniers échanges de notre conversation :
    {history_str}

    Patient: {message}

    Commencez par une salutation amicale. Posez des questions claires et simples pour aider le patient à décrire ses symptômes. Évitez le jargon médical. Montrez de l'empathie.
    Par exemple, au lieu de lister des questions, demandez : "Bonjour ! Je suis là pour vous aider. Pourriez-vous me dire ce qui vous amène aujourd'hui ? N'hésitez pas à décrire ce que vous ressentez avec vos propres mots."
    Répondez en français.
    """

    try:
        model = get_gemini_model()
        chat = model.start_chat(history=gemini_history)
        response = chat.send_message(prompt)
        bot_response = response.text
        session["history"].append({"role": "USER", "message": message})
        session["history"].append({"role": "CHATBOT", "message": bot_response})
        return jsonify({"message": bot_response})
    except Exception as e:
        print(f"Gemini API Error in handle_gemini_chat: {e}")
        return jsonify({"error": "An error occurred while communicating with the AI service. Please try again."}), 500
