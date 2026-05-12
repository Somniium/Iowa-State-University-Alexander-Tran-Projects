### Names:
# Christian Salazar : jarando
# Alex Tran : atran25

# Mini-Assignment 3 Backend

This folder contains the backend for Mini-Assignment 3, including database integration and full CRUD functionality for managing games. The backend connects to MongoDB using Mongoose and provides RESTful API endpoints for games.

---

## Project Structure

```
models/Game.js               # Mongoose schema for games
routes/gameRoutes.js         # API routes for CRUD operations
controllers/gameController.js # Logic for handling CRUD requests
config/server.js             # Server entry point
.env                         # Environment variables (DB connection, port, etc.)
```

### File Descriptions

- **models/Game.js** – Defines the schema for games, including fields like title, platform, release date, rating, and publisher.
- **routes/gameRoutes.js** – Defines API endpoints for all CRUD operations on games.
- **controllers/gameController.js** – Implements the logic for each CRUD operation.
- **config/server.js** – Sets up the Express server and connects to MongoDB.
- **.env** – Stores environment variables like `PORT` and `MONGO_URI`.

---

## Setup Instructions

### 1. Install Dependencies

```bash
npm install
```

### 2. Configure Environment Variables

Create a `.env` file in the root directory with the following values:

```
PORT=5000
MONGO_mongodb+srv://somniiiium_db_user:i4CILDHvY9KEVogs@coms319.arrlsmz.mongodb.net/
```

### 3. Start the Server

```bash
npm run dev
```

## Functionality

The backend connects to MongoDB using Mongoose and provides full CRUD operations for games.

### CRUD Endpoints

| Method | Endpoint        | Description           |
|--------|----------------|---------------------|
| GET    | `/games`       | List all games       |
| GET    | `/games/:id`   | Get a game by ID     |
| POST   | `/games`       | Add a new game       |
| PUT    | `/games/:id`   | Update a game        |
| DELETE | `/games/:id`   | Delete a game        |

### Notes

- All routes expect and return JSON.
- Mongoose handles validation and schema enforcement.
- Ensure your MongoDB URI is correct in `.env` to avoid connection errors.
- You can test endpoints with tools like Postman or curl.
