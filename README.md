# podcast-generator
Web app that generates cool podcasts

### Backend Setup
1. Clone the repository:
   ```bash
   git clone https://github.com/your-org/podcast-generator.git
   cd podcast-generator
   ```

2. Create the database:
   ```bash
   # Login as postgres user
   sudo -u postgres psql

   # Create database and user
   CREATE DATABASE podcast_db;
   CREATE USER podcastadmin WITH ENCRYPTED PASSWORD 'your_password';
   GRANT ALL PRIVILEGES ON DATABASE podcast_db TO podcastadmin;
   ```

3. Build and run the application:
   ```bash
   ./mvnw spring-boot:run
   ```
