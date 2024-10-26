#!/bin/bash

# Create a podcast
echo "Creating podcast..."
curl -X POST http://localhost:8080/api/podcasts \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Podcast",
    "description": "A test podcast description",
    "length": 1800,
    "status": "DRAFT",
    "userId": "test-user"
  }'

# Create a context for the podcast
echo -e "\nCreating context..."
curl -X POST http://localhost:8080/api/contexts \
  -H "Content-Type: application/json" \
  -d '{
    "descriptionText": "Test context description",
    "sourceUrl": "https://example.com/source",
    "filePath": "/path/to/source.txt",
    "processedContent": "Processed test content"
  }'

# Create participants
echo -e "\nCreating participant 1..."
curl -X POST http://localhost:8080/api/participants \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "gender": "Male",
    "age": 30,
    "role": "Host",
    "roleDescription": "Main show host",
    "voiceCharacteristics": "Deep, professional voice",
    "syntheticVoiceId": "voice-1"
  }'

echo -e "\nCreating participant 2..."
curl -X POST http://localhost:8080/api/participants \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Smith",
    "gender": "Female",
    "age": 28,
    "role": "Guest",
    "roleDescription": "Expert guest",
    "voiceCharacteristics": "Clear, articulate voice",
    "syntheticVoiceId": "voice-2"
  }'

# Create a transcript
echo -e "\nCreating transcript..."
curl -X POST http://localhost:8080/api/transcripts \
  -H "Content-Type: application/json" \
  -d '{
    "content": "This is the transcript content",
    "timingInfo": {"start": 0, "end": 1800},
    "editHistory": {"version": 1, "lastEditor": "system"}
  }'

# Create an audio output
echo -e "\nCreating audio..."
curl -X POST http://localhost:8080/api/audios \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "/path/to/output.mp3",
    "filename": "output.mp3",
    "fileSize": 5242880,
    "duration": 1800,
    "format": "MP3",
    "qualityMetrics": {"bitrate": "320kbps", "sampleRate": "44.1kHz"}
  }'
