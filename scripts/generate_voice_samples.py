import requests
import json
import csv
import os

# Configuration
API_KEY = os.getenv('ELEVENLABS_API_KEY')  # Make sure to set this environment variable
OUTPUT_DIR = "voice_samples"

# Create output directory if it doesn't exist
os.makedirs(OUTPUT_DIR, exist_ok=True)

# Sample text from AIServiceImpl
SAMPLE_TEXT = """Hello everyone! I'm excited to share my thoughts on this topic. 
Let me walk you through my perspective and experience. 
I believe this discussion will be both informative and engaging for our listeners. 
I look forward to exploring these ideas together."""

def read_voice_settings():
    """Read voice settings from CSV file"""
    with open('scripts/standard-voices-list.csv', 'r', encoding='utf-8-sig') as file:
        reader = csv.DictReader(file, delimiter=';')
        voices = []
        for row in reader:
            voice = {
                'voice_id': row['external_voice_ID'],
                'settings': {
                    'stability': float(row['stability']),
                    'similarity_boost': float(row['similarity_boost']),
                    'style_exaggeration': float(row['style_exaggeration']),
                    'speaker_boost': row['speaker_boost'].lower() == 'true'
                }
            }
            voices.append(voice)
        return voices

def generate_audio(voice_id, voice_settings):
    """Generate audio using ElevenLabs API"""
    url = f"https://api.elevenlabs.io/v1/text-to-speech/{voice_id}"
    
    headers = {
        "xi-api-key": API_KEY,
        "Content-Type": "application/json"
    }
    
    data = {
        "text": SAMPLE_TEXT,
        "model_id": "eleven_multilingual_v2",
        "voice_settings": voice_settings
    }

    output_filename = f"{voice_id}.mp3"
    print(f"Generating audio for voice ID: {voice_id}")
    
    response = requests.post(url, json=data, headers=headers)
    
    if response.status_code == 200:
        output_path = os.path.join(OUTPUT_DIR, output_filename)
        with open(output_path, 'wb') as f:
            f.write(response.content)
        print(f"Successfully generated: {output_filename}")
    else:
        print(f"Error generating audio: {response.status_code}")
        print(response.text)

def main():
    voices = read_voice_settings()
    
    # Generate audio for each voice using the same text
    for voice in voices:
        generate_audio(
            voice_id=voice['voice_id'],
            voice_settings=voice['settings']
        )

if __name__ == "__main__":
    main()
