# AI Podcast Generator

An application that leverages AI to generate engaging podcasts from user-provided topics and contexts. The system handles the entire process from content planning to audio generation, creating natural-sounding conversations between AI-generated participants.

## Features

- 🎙️ Generate multi-speaker podcasts from text descriptions
- 🗣️ Custom voice generation for participants
- 📝 AI-driven content generation and conversation flow
- 🔊 High-quality audio synthesis
- 📊 Real-time generation progress tracking
- 🎯 Context-aware discussion generation

## Installation

### Prerequisites

- Java 17 or higher
- Node.js 18 or higher
- PostgreSQL 15 or higher
- Maven 3.8+

### Database Setup

```bash
# Login as postgres user
sudo -u postgres psql

# Create database and user
CREATE DATABASE podcast_db;
CREATE USER podcastadmin WITH ENCRYPTED PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE podcast_db TO podcastadmin;
```

### Environment Variables

Create a `.env` file in the root directory with:

```properties
PODCASTGEN_DB_HOST=localhost
PODCASTGEN_DB_PORT=5432
PODCASTGEN_DB_NAME=podcast_db
PODCASTGEN_DB_USERNAME=podcastadmin
PODCASTGEN_DB_PASSWORD=your_password

OPENAI_API_KEY=your_openai_key
ELEVENLABS_API_KEY=your_elevenlabs_key

ZITADEL_DOMAIN=your_zitadel_domain
ZITADEL_CLIENT_ID=your_client_id
ZITADEL_ORG_ID=your_org_id
```

### Building and Running

1. Build and run the backend:
```bash
./mvnw spring-boot:run
```

2. Install and run the frontend:
```bash
cd src/main/frontend
npm install
npm run dev
```

## User Flow

1. **Create Podcast**
   - Enter basic podcast metadata (title, description, length)
   - Provide optional context URLs or descriptions

2. **Define Participants**
   - Add and configure podcast participants
   - Customize voice characteristics
   - Generate or select synthetic voices

3. **Review Transcript**
   - Review AI-generated conversation
   - Adjust content and flow
   - Fine-tune participant interactions

4. **Generate Podcast**
   - Monitor real-time generation progress
   - Preview generated audio
   - Download final podcast

## Architecture

### Frontend
- React with TypeScript
- TailwindCSS for styling
- WebSocket integration for real-time updates
- Component-based architecture with wizard pattern

### Backend
- Spring Boot application
- OAuth2 authentication with Zitadel
- WebSocket support for generation progress
- JPA/Hibernate for data persistence

### AI Integration
- OpenAI GPT-4 for content generation
- ElevenLabs for voice synthesis
- Custom prompt engineering for natural conversations

### Data Flow
1. User input → React frontend
2. REST API endpoints → Spring Backend
3. Content generation → OpenAI
4. Voice synthesis → ElevenLabs
5. Real-time updates → WebSocket
6. Audio delivery → Frontend player

## Potential Improvements

### Technical Improvements
- [ ] Implement caching for generated audio segments
- [ ] Add background audio mixing capabilities
- [ ] Implement batch processing for large podcasts
- [ ] Add audio post-processing options
- [ ] Implement voice cloning capabilities

### Feature Improvements
- [ ] Add collaborative editing features
- [ ] Implement podcast templates
- [ ] Add support for music integration
- [ ] Create a voice library management system
- [ ] Add export options for different platforms

### User Experience
- [ ] Add preview mode for voice selection
- [ ] Implement undo/redo functionality
- [ ] Add draft saving capabilities
- [ ] Improve error handling and recovery
- [ ] Add batch podcast generation

## Contributing

Contributions are welcome! Please read our [Contributing Guidelines](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
