from flask import Blueprint, request, jsonify
from .services import handle_prediction, handle_gemini_chat

diagnosis_bp = Blueprint("diagnosis", __name__)

@diagnosis_bp.route("/predict", methods=["POST"])
def predict():
    return handle_prediction(request.get_json())

@diagnosis_bp.route("/gemini_chat", methods=["POST"])
def gemini_chat():
    data = request.get_json()
    message = data.get("message")
    session_id = data.get("session_id")
    return handle_gemini_chat(message, session_id)
