# AI Podcast Generator
## System Architecture and Design Specification

### 1. Introduction

#### 1.1. Purpose
The AI Podcast Generator is a web-based application that enables users to automatically generate podcasts using AI technology and synthetic voices. The system reduces the time, cost, and complexity traditionally associated with podcast production.

#### 1.2. Target Users
- Content creators
- Business professionals
- Educational institutions
- Marketing teams
- Individual podcasters

#### 1.3. Core Features
- AI-driven podcast generation from text, URLs, and documents
- Automated participant and voice profile creation
- Interactive transcript editing
- Audio synthesis and processing
- Podcast metadata management
- User authentication and management

### 2. Technical Architecture

#### 2.1. Technology Stack
- **Frontend**: React, Vite, Tailwind CSS, shadcn/ui
- **Backend**: Java Spring Boot
- **Database**: 
  - PostgreSQL for data storage
  - Flyway for database schema management and migrations
- **Authentication**: Zitadel IDP
- **Deployment**: Monolithic application structure

#### 2.2. System Components

1. **Presentation Layer**
   - Single Page Application (SPA)
   - Responsive web interface
   - Real-time audio preview
   - Interactive editing tools
   - Progress tracking and notifications

2. **Application Layer**
   - RESTful API services
   - Business logic processing
   - Async task management
   - Security and authentication
   - Data validation and transformation

3. **Data Layer**
   - Relational database
   - File storage system
   - Caching mechanism
   - Data access patterns

4. **External Services Integration**
   - Identity Provider (Zitadel)
   - AI Language Model Service (Anthropic Sonnect 3.5 latest)
   - Text-to-Speech Service (elevenlabs.io)
   - Analytics Service

### 3. Core Domain Models

#### 3.1. Primary Entities
1. **User**
   - Authentication information
   - Profile details
   - Preferences
   - Usage statistics

2. **Podcast**
   * Metadata
   		* creation date
   		* last modified date
   		* length
   		* title 
   		* description
   		* icon
   * references to other entities
   		* 1:1 to context
   		* 1:n to participant
   		* 1:1 to transcript
   		* 1:n to audio combined output (we keep a history) 	
   - Status information
   - Publishing details

3. **Context**
   * Source material
   		* Description text
   		* URL
   		* link to uploaded file 	
   - Processed content
   - Metadata
   - Analysis results

4. **Participant**
   - Profile information
   - Voice characteristics
   - Role definition
   - Speaking patterns
   - reference to synthetic voice

5. **Transcript**
   - Conversation structure
   - Speaker segments
   - Timing information
   - Edit history

6. **Audio**
   - Source segments
   - Combined output
   - Format metadata
   - Metadata
   		- size
   		- length
   - Quality metrics

### 4. Key Processes

#### 4.1. Podcast Generation Pipeline
1. **Content Ingestion**
   - Text processing
   - URL content extraction
   - Document parsing
   - Content normalization

2. **AI Processing**
   - Content analysis
   - Topic extraction
   - Character development
   - Conversation structuring

3. **Voice Synthesis**
   - Voice profile matching
   - Speech generation
   - Audio processing
   - Quality assurance

4. **Output Generation**
   - Audio compilation
   - Metadata assembly
   - Quality verification
   - Distribution preparation

#### 4.2. User Workflows
1. **Content Creation**
   - Project initialization
   - Content input
   - Parameter configuration
   - Generation control

2. **Content Management**
   - Project organization
   - Version control
   - Asset management
   - Archive handling

### 5. Frontend UX / UI guidelines

#### 5.1. Sections

The UI can be broken down in the following sections:

- top navigation bar with user icon and user action menu
- left navigation bar with links to application sections
- application section 1 is a list with an overview of all podcasts.  
- application section 2 is a settings page
- all sections have a breadcrumb navigation e.g. Home > Podcasts

#### 5.2. Section 1: podcast list

- list view displaying the metadata of the podcast incl. picture.  List paging available, default 5 items per page.
- action menu for crud actions
- play button for mp3 when available.  This opens an mp3 player section below the list with standard music playing controls and a wave form visualization

#### 5.3. Section 2: settings page

- will contain user preferences, design to be defined in a later stage.  At the moment, this is a blank page with title and breadcrumb

#### 5.4. New podcast wizard

- 4 step wizard the follows the main user flow as documented in [the mermaid flow diagra](podcast-creation-flow.mermaid)
- The wizard has a process step navigation bar at the top, 4 steps
- Step 1: podcast metadata and context information
	- sub-section Settings: title, description, length 
	- sub-section Context: 
		- context description text box (required)
		- context URL (optional)
		- context file.  File upload section with drop zone or click to browse file.  Allowed formats pdf, docx, txt, ppt
	- next button
- Step 2: participant definition
	- Icon, name, gender, age, role description, voice charateristics
	- add / delete / edit particants
	- next button (min 2 particpants to activate)
- Step 3: transcript definition
	- show generated transcript in "chat style" view
	- include participant names,timings
	- edit / save actions
	- generate podcast button (active when saved)
	- this puts a podcast generation task in the queue and returns to the podcast list view.  Progress indicator of queued task visible in the list item.
- Step 4: podcast generation
  - show progress of the podcast creation in the various phases
  - use a terminal-style progress reporting
  - options to push to background, cancel and re-generate


### 5. Security Architecture

#### 5.1. Authentication & Authorization
- OAuth 2.0 integration with Zitadel
- Role-based access control
- Session management
- API security

#### 5.2. Data Security
- Encryption at rest
- Secure transmission
- Access auditing
- Privacy controls

### 6. System Integration

#### 6.1. External Services
1. **Identity Management**
   - User authentication
   - Profile management
   - Access control
   - Session handling

2. **AI Services**
   - Content processing
   - Language generation
   - Character development
   - Quality assessment

3. **Voice Services**
   - Voice synthesis
   - Audio processing
   - Quality enhancement
   - Format conversion

### 7. Data Management

#### 7.1. Data Storage
- Relational data structures
- File storage system
- Caching strategy
- Backup procedures

#### 7.2. Data Flow
- Input processing
- State management
- Cache utilization
- Output handling

### 8. System Qualities

#### 8.1. Performance
- Response time targets
- Processing throughput
- Resource utilization
- Scaling parameters

#### 8.2. Scalability
- Horizontal scaling capability
- Resource management
- Load distribution
- Growth accommodation

#### 8.3. Reliability
- Error handling
- Recovery procedures
- Data integrity
- Service availability

#### 8.4. Maintainability
- Code organization
- Documentation standards
- Monitoring setup
- Update procedures

### 9. Development Considerations

#### 9.1. Development Environment
- Version control strategy
- Build process
- Testing framework
- Deployment pipeline

#### 9.2. Quality Assurance
- Testing methodology
- Performance monitoring
- Security assessment
- Code review process

### 10. Future Considerations

#### 10.1. Extensibility
- API expansion
- Feature additions
- Integration capabilities
- Customization options

#### 10.2. Evolution Path
- Technology updates
- Capability expansion
- Performance optimization
- User experience enhancement