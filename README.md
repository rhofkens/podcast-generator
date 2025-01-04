# AI Podcast Generator

An application that leverages AI to generate engaging podcasts from user-provided topics and contexts. The system handles the entire process from content planning to audio generation, creating natural-sounding conversations between AI-generated participants.

## Goal of this app

My main goal was to create a **non-trivial application** that uses **enterprise-grade coding patterns** using a **100% Ai-driven coding** approach. The idea was to create an application that really does something useful, with coding standards and best practices that would be acceptable by a tech enterprise.  
I wanted to prove that Ai-driven coding can be used in an enterprise context, going far beyond the nice but way-too-simple demos that are circulating on youtube & co.

## Ai-driven coding setup & experience



## Features

- 🎙️ Generate multi-speaker podcasts from text descriptions
- 🎯 Context-aware discussion generation based on external sources like websites or uploaded documents
- 🗣️ Custom voice generation for participants
- 📝 AI-driven content generation and conversation flow
- 🔊 High-quality audio synthesis
- 📊 Real-time generation progress tracking


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

### Zitadel Setup

This application uses Zitadel for OAuth2 authentication. Follow these steps to set up your Zitadel instance:

1. Create a Zitadel account at [console.zitadel.ch](https://console.zitadel.ch) or set up your own instance

2. Create a new Project in Zitadel
   - Go to Projects → New
   - Give your project a name (e.g., "Podcast Generator")

3. Create an OAuth2 Application
   - In your project, go to Applications → New
   - Choose "Web Application"
   - Set the following:
     - Name: Podcast Generator
     - RedirectURLs: 
       - `http://localhost:8080/login/oauth2/code/zitadel` (development)
       - `https://your-domain/login/oauth2/code/zitadel` (production)
     - Post Logout URLs:
       - `http://localhost:8080` (development)
       - `https://your-domain` (production)
     - Enable PKCE (Proof Key for Code Exchange)

4. Note down the following values for your `.env` file:
   - ZITADEL_DOMAIN (e.g., `my-instance.zitadel.cloud`)
   - ZITADEL_CLIENT_ID (from your application settings)
   - ZITADEL_ORG_ID (your organization ID)

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
