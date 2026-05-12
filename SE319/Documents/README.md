# HonestCritic – Final Project README

## Names
- Christian Salazar (jarando)  
- Alex Tran (atran25)

## Table of Contents
- [Introduction](#introduction)  
- [Project Description](#project-description)  
- [File Structure](#file-structure)  
- [Code & Logic](#code--logic)  
- [Screenshots](#screenshots)  
- [Setup](#setup)  
- [Contributions](#contributions)  
- [API Setup](#api-setup)  
- [Usage Example](#usage-example)

## Introduction
HonestCritic is a game review and discovery web application designed to help users explore video games through curated reviews, ratings, and media previews. The project targets casual and enthusiast gamers who want a clean, visually engaging way to browse games, compare scores, and learn more before purchasing.  
The goal of the project is to demonstrate strong UI/UX design, dynamic data handling, and interactive frontend behavior using modern web technologies. HonestCritic is an original project, inspired by professional review platforms such as Metacritic and IGN, but implemented with a custom layout, interaction flow, and visual identity.

## Project Description
HonestCritic is a multi-page full-stack application with the following main features:

### Images

## Login Page
-The Login Page allows registered users to access their accounts.
![Login](image-2.png)

## Create Account Page
-The Create Account Page enables new users to register for an account.
![Create Account](image-1.png)

## Home Page
- Featured carousel (“Tonight’s Picks”) and horizontally scrollable rails for new and beloved games.
![Home top](image-3.png)
![Home bottom](image-4.png)
![new](image-12.png)

## Developer Reviews Page
- Grid-based gallery of reviewed games with detailed views.
![Developer Reviews](image-5.png)

## Game Gallery Page
- Full library of all games in the dataset using a reusable card and modal system.

![Game Gallery](image-6.png)

## Upcoming Games Page
- Preview of unreleased games with trailers and screenshots.
![Upcoming](image-7.png)

## Honest Bot
- Personalized chatbot powered by the Gemini API for game recommendations.  
- Connects to RAWG to fetch game data and applies a forced template for consistent responses.
![Honest Bot Icon](image-10.png)
![Starting Page](image.png)
![Default if Error](image-8.png)
![GameModal](image-9.png)


## User Game Cards
- Visual profiles showcasing user experience with games.
![User Game Cards](image-11.png)

### File Structure

## Frontend

frontend/
├─ public/
│  └─ images/
├─ src/
│  ├─ api/
│  │  └─ client.js
│  ├─ assets/
│  ├─ auth/
│  │  ├─ AuthContext.jsx
│  │  ├─ RequireAdmin.jsx
│  │  └─ RequireAuth.jsx
│  ├─ components/
│  │  ├─ ChatbotButton.jsx
│  │  ├─ ChatbotGameCard.jsx
│  │  ├─ Footer.jsx
│  │  ├─ GameCard.jsx
│  │  ├─ GameModal.jsx
│  │  ├─ Navbar.jsx
│  │  ├─ ReviewForm.jsx
│  │  └─ ReviewList.jsx
│  └─ pages/
│     ├─ About.jsx
│     ├─ AdminPanel.jsx
│     ├─ ChatbotPage.jsx
│     ├─ Checkout.jsx
│     ├─ Confirmation.jsx
│     ├─ Dashboard.jsx
│     ├─ DeveloperReviews.jsx
│     ├─ Gallery.jsx
│     ├─ GameDetailEdit.jsx
│     ├─ Home.jsx
│     ├─ Login.jsx
│     ├─ Settings.jsx
│     ├─ Signup.jsx
│     ├─ Unauthorized.jsx
│     └─ Upcoming.jsx
├─ App.jsx
├─ index.css
└─ main.jsx


## Backend

backend/
├─ api/
│  ├─ AdminCarousel.js
│  ├─ AdminCarouselSelection.js
│  ├─ authMiddleware.js
│  ├─ ChatBotSDK.js
│  ├─ DeveloperCards.js
│  ├─ DeveloperCardsAdmin.js
│  ├─ Games.js
│  ├─ Game.js
│  ├─ Rawg.js
│  ├─ Review.js
│  ├─ ReviewerRequest.js
│  ├─ Reviews.js
│  ├─ User.js
│  ├─ UserCards.js
│  ├─ UserGameCard.js
│  └─ Users.js
├─ config/
│  ├─ db.js
│  └─ server.js



### Documents
- Architecture PDFs, planning files, demo video

## Code & Logic
- React frontend uses state, props, and React Hooks (useState, useEffect).  
- Backend routes implement full CRUD functionality.  
- MongoDB database stores all game data.  
- Honest Bot queries Gemini API + RAWG and returns structured game recommendations.

## Screenshots
- Include full-page screenshots of all views with annotations:
  - What the page does
  - Allowed user actions
  - Post-interaction behavior (form submission, modal open, etc.)

## Setup
1. Install dependencies for both /backend & /frontend:
```bash```
npm install

2. Create a .env file with:

PORT=5000
MONGO_URI=mongodb+srv://somniiiium_db_user:i4CILDHvY9KEVogs@coms319.arrlsmz.mongodb.net/

3. Start backend server:

npm run dev

4. Start frontend server:

npm run dev (yes, twice).

### Contributions

- Christian Salazar: Gemini chatbot setup, related chatbot pages/components

- Alex Tran: Ported old design to React, implemented all pages and routing.

#### API Setup

### CRUD routes for games:

## GET /games 

– list all games

## GET /games/:id 

– get a game by ID

## POST /games 

– add a game

## PUT /games/:id 

– update a game

## DELETE /games/:id 

– delete a game

# Honest Bot API connects Gemini responses to RAWG data and formats output in a template.

### Usage Example

Honest Bot Interaction

    Navigate to Home Page and click the Honest Bot icon.

    Type a query, e.g.,

"I'm looking for a fun RPG game released in 2025."

    Bot fetches recommendations from Gemini, queries RAWG, and returns:

Title: Eldoria Chronicles
Genre: RPG
Release Date: May 12, 2025
Rating: 92%
Trailer: [Watch Here](link-to-trailer)
Summary: A story-driven adventure in a magical realm. Features turn-based combat and deep character customization.

    Click the game title to view its Game Card with detailed info.

### CRUD Example: Managing Games

## Add a New Game

## POST /games
{
  "title": "Mystic Quest",
  "genre": "Adventure",
  "releaseDate": "2025-09-10",
  "rating": 88,
  "summary": "Explore a magical world full of puzzles and hidden secrets."
}

View All Games

## GET /games

Update a Game

## PUT /games/:id
{
  "rating": 91
}

Delete a Game

## DELETE /games/:id

    This workflow demonstrates full-stack interaction: React frontend communicates with backend routes, which update the MongoDB database. Users can browse, search, and interact with games while also receiving AI-powered recommendations through Honest Bot.