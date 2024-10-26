## 1. **Application specification**

### 1.1. Basic information

- Application name: Podcast Generator
- Application goal: Users can generate fun podcasts with a couple of clicks.  They can define the subject, the participants, the voices and the length of the podcast.  The application generates the podcast transcript and then converts that transcript into an mp3 file that the user can download.
- Target audience: this is a B2C application, targeting non-professional computer users

### 1.2. Functional requirements

#### 1.2.1 

### 1.3 Main workflow

- Two roles:  

1. user 
2. system

- Flow

1. user creates new podcast. 
2. user adds podcast metadata
3. system saves podcast to db
4. user adds context information to podcast: a text description, a URL to a webpage, an uploaded document or a combination
5. system parses the context (if need scrapes the webpage and parses the document)
6. system creates a prompt to define two participants with a well defined role and a voice definition
7. system add two defined participants to the podcast
8. user validates and, if needed edits the participant metadata, role and voice definition.  User can add new participants or delete participants
9. user saves particpants
10. system creates prompt to generate podcast transcript with these particpants
11. system displays the transcript
12. user validates and, if needed, edits the transcripts
13. user saves transcript
14. system generates mp3 files for each section in the transcript using a TTS service.  
15. system stitches all mp3 files together to create one mp3 file and attaches this to the podcast
16. user plays podcast.  User sees podcast sound wave rendering over time.
17. system plays mp3
18. user downloads mp3
