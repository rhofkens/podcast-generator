## 1. Tech stack
### 1.1. General

The project will be created with Java Spring Boot.
The frontend and backend will be deployed in a single, monolithic repo.

### 1.2. Backend

We use Spring Boot 3.2.0, java 17 and JPA.
Database is PostgreSQL.

### 1.3.  Frontend

We use React with TSX and Tailwind CSS, shadcn/ui and Vite.

### 1.4.  User management and authentication

We integrate Zitadel as the IDP.

### 1.5.  AI and LLMs

We use Spring AI https://spring.io/projects/spring-ai as the library to interact with LLMs.
We use OpenAI GPT4o as the LLM.

### 1.6. Text to speech

We use the elevenlabs.io text to voice API for TTS: https://elevenlabs.io/docs/api-reference/text-to-speech
We generate our own custom voices with the voice design API: https://elevenlabs.io/docs/api-reference/ttv-create-previews
We will generate multiple audio segments with speakers taking turns, so we will 
have to stitch the segments together. The best practice with elevenlabs API is 
documented on this page https://elevenlabs.io/docs/developer-guides/how-to-use-request-stitching

### 1.7 Subscription management and payments

We will use Stripe to define a tiered subscription model with basic-good-best plans.  
The plans have a monthly flat rate.  Invoices and payments are done via stripe checkout.

