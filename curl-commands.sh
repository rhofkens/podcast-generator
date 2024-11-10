#!/bin/bash

# Function to handle API responses
handle_response() {
    local status=$1
    local response=$2
    local endpoint=$3
    
    if [ $status -eq 201 ] || [ $status -eq 200 ]; then
        echo -e "\n✅ Success creating $endpoint"
        echo "Response:"
        echo "$response" | jq '.'
    else
        echo -e "\n❌ Error creating $endpoint"
        echo "Status code: $status"
        echo "Error response:"
        echo "$response" | jq '.' || echo "$response"
    fi
}

# Create a podcast
echo -e "\n📝 Creating podcast..."
response=$(curl -s -w "\n%{http_code}" -X POST http://localhost:8080/api/podcasts \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Podcast",
    "description": "A test podcast description",
    "length": 1800,
    "status": "DRAFT",
    "userId": "test-user"
  }')
status=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$ d')
handle_response $status "$body" "podcast"

# Store podcast ID for later use
# podcast_id=$(echo "$body" | jq -r '.id')
podcast_id=1

# Create a context for the podcast
echo -e "\n📋 Creating context..."
response=$(curl -s -w "\n%{http_code}" -X POST http://localhost:8080/api/contexts \
  -H "Content-Type: application/json" \
  -d "{
    \"podcast_id\": \"1\",
    \"descriptionText\": \"Test context description\",
    \"sourceUrl\": \"https://example.com/source\",
    \"filePath\": \"/path/to/source.txt\",
    \"processedContent\": \"Processed test content\"
  }")
status=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$ d')
handle_response $status "$body" "context"

# Create participants
echo -e "\n👤 Creating participant 1..."
response=$(curl -s -w "\n%{http_code}" -X POST http://localhost:8080/api/participants \
  -H "Content-Type: application/json" \
  -d "{
    \"podcastId\": \"1\",
    \"name\": \"John Doe\",
    \"gender\": \"Male\",
    \"age\": 30,
    \"role\": \"Host\",
    \"roleDescription\": \"Main show host\",
    \"voiceCharacteristics\": \"Deep, professional voice\",
    \"syntheticVoiceId\": \"voice-1\"
  }")
status=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$ d')
handle_response $status "$body" "participant 1"

echo -e "\n👤 Creating participant 2..."
response=$(curl -s -w "\n%{http_code}" -X POST http://localhost:8080/api/participants \
  -H "Content-Type: application/json" \
  -d "{
    \"podcastId\": $podcast_id,
    \"name\": \"Jane Smith\",
    \"gender\": \"Female\",
    \"age\": 28,
    \"role\": \"Guest\",
    \"roleDescription\": \"Expert guest\",
    \"voiceCharacteristics\": \"Clear, articulate voice\",
    \"syntheticVoiceId\": \"voice-2\"
  }")
status=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$ d')
handle_response $status "$body" "participant 2"

# Create a transcript
echo -e "\n📄 Creating transcript..."
response=$(curl -s -w "\n%{http_code}" -X POST http://localhost:8080/api/transcripts \
  -H "Content-Type: application/json" \
  -d "{
    \"podcastId\": $podcast_id,
    \"content\": \"This is the transcript content\",
    \"timingInfo\": {\"start\": 0, \"end\": 1800},
    \"editHistory\": {\"version\": 1, \"lastEditor\": \"system\"}
  }")
status=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$ d')
handle_response $status "$body" "transcript"

# Create an audio output
echo -e "\n🔊 Creating audio..."
response=$(curl -s -w "\n%{http_code}" -X POST http://localhost:8080/api/audios \
  -H "Content-Type: application/json" \
  -d "{
    \"podcastId\": $podcast_id,
    \"filePath\": \"/path/to/output.mp3\",
    \"filename\": \"output.mp3\",
    \"fileSize\": 5242880,
    \"duration\": 1800,
    \"format\": \"MP3\",
    \"qualityMetrics\": {\"bitrate\": \"320kbps\", \"sampleRate\": \"44.1kHz\"}
  }")
status=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$ d')
handle_response $status "$body" "audio"
