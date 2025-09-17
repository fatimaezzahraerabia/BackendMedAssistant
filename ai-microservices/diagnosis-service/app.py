import os
from flask import Flask
from dotenv import load_dotenv
from d_diagnosis.routes import diagnosis_bp

# Construct the path to the .env file
script_dir = os.path.dirname(os.path.abspath(__file__))
dotenv_path = os.path.join(script_dir, '.env')

# Load environment variables from the specified .env file
load_dotenv(dotenv_path=dotenv_path)

def create_app():
    app = Flask(__name__)
    
    # Get the API key from the environment
    gemini_api_key = os.getenv("GEMINI_API_KEY")
    if not gemini_api_key:
        raise ValueError("GEMINI_API_KEY environment variable not set.")
    
    # Store the API key in the app config to make it accessible
    app.config["GEMINI_API_KEY"] = gemini_api_key
    
    app.register_blueprint(diagnosis_bp, url_prefix="/api/diagnosis")
    return app

if __name__ == "__main__":
    app = create_app()
    app.run(debug=True, port=5001)
