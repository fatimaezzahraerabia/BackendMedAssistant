from sklearn.metrics.pairwise import cosine_similarity
from .utils import preprocess_text

def find_relevant_info(query, df_combined, tfidf_matrix, vectorizer):
    """
    Finds the most relevant information from the knowledge base for a given query
    and formats it into a user-friendly message.
    """
    if not query:
        return {"message": "Veuillez poser une question."}

    # Preprocess the user's query
    processed_query = preprocess_text(query)
    query_vector = vectorizer.transform([processed_query])

    # Calculate cosine similarity
    similarities = cosine_similarity(query_vector, tfidf_matrix)
    
    # Get the index of the most similar document
    most_similar_index = similarities.argmax()
    similarity_score = float(similarities[0, most_similar_index])

    # Retrieve the most relevant information if similarity is above a threshold
    if similarity_score > 0.1:  # Increased threshold for better relevance
        relevant_info = df_combined.iloc[most_similar_index]
        
        title = relevant_info.get('title', 'N/A')
        source = relevant_info.get('source', 'N/A')
        
        # Format the response into a readable string
        if source == 'csv':
            chapitre = relevant_info.get('cause_initiale_chapitre', 'N/A')
            message = f"D'après nos données, voici une information liée à '{title}': {chapitre}."
        elif source in ['json', 'pdf']:
            original_text = relevant_info.get('original_text', '')
            extrait = original_text[:200] + "..." if len(original_text) > 200 else original_text
            message = f"Dans le document '{title}', j'ai trouvé le passage suivant : \"{extrait}\""
        else:
            message = f"J'ai trouvé une information pertinente dans '{title}'."

        response = {"message": message}
    
    else:
        response = {"message": "Je suis désolé, je n'ai pas trouvé d'information précise à ce sujet. Pourriez-vous reformuler votre question ?"}

    return response
