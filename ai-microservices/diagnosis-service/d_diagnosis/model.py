import os
import json
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity

class DiseaseModel:
    def __init__(self, data_path):
        self.vectorizer = TfidfVectorizer()
        self.tfidf_matrix = None
        self.data = None
        self._load_and_build_model(data_path)

    def _load_and_build_model(self, data_path):
        """Loads the disease data and builds the TF-IDF model."""
        try:
            with open(data_path, 'r', encoding='utf-8') as f:
                diseases_data = json.load(f)["diseases"]
            
            # Convert to DataFrame
            df = pd.DataFrame(list(diseases_data.items()), columns=['disease', 'symptoms'])
            df['symptoms_text'] = df['symptoms'].apply(lambda x: ' '.join(x))
            
            self.data = df
            self.tfidf_matrix = self.vectorizer.fit_transform(df['symptoms_text'])
            print("Disease model built successfully.")
        except Exception as e:
            print(f"Error building disease model: {e}")

    def predict(self, symptoms_list):
        """Predicts the most likely disease based on a list of symptoms."""
        if self.tfidf_matrix is None or self.data is None:
            return "Model not loaded. Cannot predict."

        query = ' '.join(symptoms_list)
        query_vector = self.vectorizer.transform([query])
        
        similarities = cosine_similarity(query_vector, self.tfidf_matrix)
        
        most_similar_index = similarities.argmax()
        
        return self.data.iloc[most_similar_index]['disease']

# --- Model Singleton ---
# Load the model once when the module is imported.
try:
    # Construct the path to the data file relative to this script's location
    script_dir = os.path.dirname(os.path.abspath(__file__))
    json_path = os.path.join(script_dir, '..', 'data', 'diseases_symptoms.json')
    
    _model_instance = DiseaseModel(json_path)
except Exception as e:
    _model_instance = None
    print(f"Failed to initialize DiseaseModel singleton: {e}")


def predict_disease(symptoms):
    """
    A simple wrapper function to call the model's predict method.
    This is the function that will be imported by the service.
    """
    if _model_instance:
        return _model_instance.predict(symptoms)
    else:
        return "Error: Disease model is not available."
