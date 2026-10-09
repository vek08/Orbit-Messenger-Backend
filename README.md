# Orbit Messenger - Backend

The complete Spring Boot backend for Orbit Messenger.

## Technologies Used
- Java 21 & Spring Boot 3/4
- Spring Security + JWT Authentication
- Spring Data JPA + Hibernate
- PostgreSQL
- Spring WebSocket + STOMP
- Jakarta Validation

## Setup Instructions

### 1. Database
You must have a PostgreSQL database running locally named `orbit_messenger`.
For convenience, a `docker-compose.yml` is provided. Run:
```bash
docker-compose up -d
```
This will start a PostgreSQL container on port 5432 with credentials `postgres/postgres`.

### 2. Configuration
The application is configured in `src/main/resources/application.properties`. It connects to `jdbc:postgresql://localhost:5432/orbit_messenger`.
It will automatically create the database tables on startup (`hibernate.ddl-auto=update`).

### 3. Running the Application
From the `backend` directory, run:
```bash
./mvnw spring-boot:run
```
The server will start on `http://localhost:8080`. You can verify by visiting `http://localhost:8080/api/health`.

## API Documentation (REST)

### Authentication
- `POST /api/auth/register`
  - Body: `{ "username": "John", "email": "j@test.com", "password": "pass" }`
  - Returns: `{ "token": "...", "user": { ... } }`
- `POST /api/auth/login`
  - Body: `{ "email": "j@test.com", "password": "pass" }`
- `GET /api/users/me` (Requires Bearer Token)

### Users
- `GET /api/users/search?query=name` (Requires Bearer Token)
- `GET /api/users/{userId}/online` (Requires Bearer Token): Returns `{ "online": true/false }` based on WebSocket presence.

### Conversations
All require Bearer Token.
- `GET /api/conversations`: List all your conversations.
- `POST /api/conversations/direct`: Create a 1-on-1 chat. Body: `{ "targetUserId": "UUID" }`
- `POST /api/conversations/group`: Create group. Body: `{ "name": "Group", "memberIds": ["UUID1", "UUID2"] }`
- `GET /api/conversations/{id}/members`: List members of a conversation.

### Messages
- `GET /api/conversations/{id}/messages?page=0&size=30`: Get paginated chat history.

## WebSocket Real-Time Messaging

- **Endpoint**: `/ws` (SockJS enabled)
- **Authentication**: You MUST pass your JWT in the `Authorization` header during the STOMP `CONNECT` frame. E.g., `Authorization: Bearer <token>`.
- **Destinations**:
  - **Send Message**: Send a JSON payload to `/app/chat.send`.
    - Payload: `{ "conversationId": "UUID", "content": "Hello!" }`
  - **Subscribe to Conversation**: Subscribe to `/topic/conversation.{conversationId}` to receive live messages. The backend verifies you are a member before allowing the subscription.

## Testing with Postman
1. Register two users.
2. Grab the JWT for both.
3. User A creates a direct conversation with User B.
4. User A sends a message to the WebSocket `/app/chat.send` destination.
5. User B subscribes to the `/topic/conversation.{conversationId}` to see it.
