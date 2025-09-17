import re
import unicodedata

def normalize_string(s):
    """
    Normalizes a string by:
    1. Converting to lowercase.
    2. Removing accents and diacritics.
    3. Removing non-alphanumeric characters (except spaces).
    4. Removing extra whitespace.
    """
    if not isinstance(s, str):
        return ""
    
    # Normalize to decomposed Unicode (NFD) and remove diacritics
    s = unicodedata.normalize('NFD', s)
    s = re.sub(r'[\u0300-\u036f]', '', s)
    
    # Convert to lowercase
    s = s.lower()
    
    # Remove non-alphanumeric characters (keep spaces)
    s = re.sub(r'[^a-z0-9\s]', '', s)
    
    # Remove extra whitespace
    s = re.sub(r'\s+', ' ', s).strip()
    
    return s
