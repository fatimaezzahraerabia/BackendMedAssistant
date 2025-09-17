import os
import re
import json
import pandas as pd
import nltk
from PyPDF2 import PdfReader
from nltk.corpus import stopwords
from nltk.tokenize import word_tokenize
from sklearn.feature_extraction.text import TfidfVectorizer

# Ensure NLTK data is downloaded
try:
    nltk.data.find('tokenizers/punkt')
except nltk.downloader.DownloadError:
    nltk.download('punkt', quiet=True)
try:
    nltk.data.find('corpora/stopwords')
except nltk.downloader.DownloadError:
    nltk.download('stopwords', quiet=True)

def extract_text_from_pdf(pdf_path):
    """Extracts text from a given PDF file."""
    text = ""
    try:
        with open(pdf_path, 'rb') as file:
            reader = PdfReader(file)
            for page in reader.pages:
                text += page.extract_text() or ""
    except Exception as e:
        print(f"Error extracting text from PDF {pdf_path}: {e}")
    return text

def preprocess_text(text):
    """Cleans and preprocesses text by lowercasing, removing non-alphabetic characters, and stop words."""
    stop_words = set(stopwords.words('french'))
    text = text.lower()
    text = re.sub(r'\W', ' ', text)  # Remove punctuation
    tokens = word_tokenize(text, language='french')
    tokens = [word for word in tokens if word.isalpha() and word not in stop_words]
    return ' '.join(tokens)

def load_knowledge_base():
    """Loads data from CSV, JSON, and PDFs, preprocesses it, and creates a TF-IDF matrix."""
    try:
        script_dir = os.path.dirname(os.path.abspath(__file__))
        
        # Define paths relative to this script's location
        csv_path = os.path.join(script_dir, '..', '..', 'data', 'Data in text format (CSV).csv')
        json_path = os.path.join(script_dir, '..', '..', 'diagnosis-service', 'data', 'diseases_symptoms.json')
        pdf_dir = os.path.join(script_dir, '..', '..', 'data')

        # Load and process CSV data
        df = pd.read_csv(csv_path, sep=';')
        df.fillna('', inplace=True)
        df['combined_text'] = df['cause_initiale_classe'] + ' ' + df['cause_initiale_bloc'] + ' ' + df['cause_initiale_chapitre']
        df['processed_text'] = df['combined_text'].apply(preprocess_text)
        
        # Load and process JSON data
        with open(json_path, 'r', encoding='utf-8') as f:
            diseases_data = json.load(f)
        diseases_list = [{'disease_name': k, 'symptoms': ' '.join(v)} for k, v in diseases_data['diseases'].items()]
        df_diseases = pd.DataFrame(diseases_list)
        df_diseases['processed_text'] = df_diseases['symptoms'].apply(preprocess_text)

        # Load and process PDF data
        pdf_files = [f for f in os.listdir(pdf_dir) if f.endswith('.pdf')]
        pdf_list = [{'pdf_name': f, 'content': extract_text_from_pdf(os.path.join(pdf_dir, f))} for f in pdf_files]
        df_pdfs = pd.DataFrame(pdf_list)
        df_pdfs['processed_text'] = df_pdfs['content'].apply(preprocess_text)

        # Combine all data sources
        df['source'] = 'csv'
        df_diseases['source'] = 'json'
        df_pdfs['source'] = 'pdf'

        df_combined = pd.concat([
            df.rename(columns={'combined_text': 'original_text', 'cause_initiale_classe': 'title'}),
            df_diseases.rename(columns={'symptoms': 'original_text', 'disease_name': 'title'}),
            df_pdfs.rename(columns={'content': 'original_text', 'pdf_name': 'title'})
        ], ignore_index=True)

        # Vectorize the processed text
        vectorizer = TfidfVectorizer()
        tfidf_matrix = vectorizer.fit_transform(df_combined['processed_text'].tolist())
        
        print("Knowledge base loaded and vectorized successfully.")
        return df_combined, tfidf_matrix, vectorizer

    except Exception as e:
        print(f"Failed to load knowledge base: {e}")
        return None, None, None
