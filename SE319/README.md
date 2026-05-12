# Use this README to document how your code in this folder is organized.

# Final Project – [MS_8's HonestCritic Website]

## Table of Contents

- [Introduction](#introduction)
- [Project Description](#project-description)
- [File Structure](#file-structure)
- [Code & Logic](#code--logic)
- [Screenshots](#screenshots)
- [Setup](#setup)
- [Contributions](#contributions)
- [API Setup](#api-setup)

## Introduction

HonestCritic is a game review and discovery web application designed to help users explore video games through curated reviews, ratings, and media previews. The project targets casual and enthusiast gamers who want a clean, visually engaging way to browse games, compare scores, and learn more before purchasing.

The goal of the project is to demonstrate strong UI/UX design, dynamic data handling, and interactive frontend behavior using modern web technologies. HonestCritic is an original project, inspired by professional review platforms such as Metacritic and IGN, but implemented with a custom layout, interaction flow, and visual identity. While we initially did not port this website during our mini-assignments, we believed learning the skills first from the mini-assignments were far more important.

## Project Description

HonestCritic is a multi-page frontend application with the following main features:

Home Page

Featured carousel (“Tonight’s Picks”)

*Horizontally scrollable rails for new and beloved games

Developer Reviews Page

*Grid-based gallery of reviewed games

*Modal view with ratings, media, summaries, and store links

Game Gallery Page

*Full gallery of all games in the dataset

*Reusable card and modal system

Upcoming Games Page

*Preview of unreleased games with trailers and screenshots

Live Search

*Real-time search with debouncing across the entire site

FAQ Page

*Accessible accordion for commonly asked questions

Authors Page

*Team member information and attribution

Game data (titles, ratings, media, summaries, and categories) is loaded dynamically from structured JSON files. User interaction flows include browsing, searching, opening modals, and navigating between pages. While this project focuses on frontend functionality, it simulates real-world CRUD-style data handling through structured datasets.

## File Structure
```
frontend/
├── public/
│   └── images/
├── src/
│   ├── api/
│   │   └── client.js            # Axios / fetch wrapper
│   ├── assets/
│   │   └── react.svg
│   ├── auth/
│   │   ├── AuthContext.jsx      # Auth state management
│   │   ├── RequireAuth.jsx      # Route guard
│   │   └── RequireAdmin.jsx     # Admin-only guard
│   ├── components/
│   │   ├── Footer.jsx
│   │   ├── GameCard.jsx
│   │   ├── GameModal.jsx
│   │   ├── Navbar.jsx
│   │   ├── ProtectedRoute.jsx
│   │   ├── ReviewForm.jsx
│   │   ├── ReviewList.jsx
│   │   └── SearchBar.jsx
│   ├── pages/
│   │   ├── Home.jsx
│   │   ├── Gallery.jsx
│   │   ├── DeveloperReviews.jsx
│   │   ├── Upcoming.jsx
│   │   ├── GameDetailEdit.jsx
│   │   ├── Dashboard.jsx
│   │   ├── AdminPanel.jsx
│   │   ├── Login.jsx
│   │   ├── Signup.jsx
│   │   ├── Checkout.jsx
│   │   ├── Confirmation.jsx
│   │   ├── Settings.jsx
│   │   ├── About.jsx
│   │   └── Unauthorized.jsx
│   ├── App.jsx                  # Router + layout
│   ├── main.jsx                 # React entry
│   └── index.css
│
├── index.html
├── vite.config.js
├── package.json
└── README.md
│
├── assets/
│   ├── images/         # Game covers, logos, screenshots
│   ├── styles-member1.css
│   └── styles-member2.css
│
backend/
├── api/
│   ├── authMiddleware.js        # Auth & role protection
│   ├── Game.js                  # Single game model
│   ├── Games.js                 # Game routes
│   ├── Review.js                # Single review model
│   ├── Reviews.js               # Review routes
│   ├── ReviewerRequest.js       # Reviewer request model
│   ├── ReviewerRequests.js      # Reviewer request routes
│   ├── User.js                  # User model
│   ├── Users.js                 # User routes
│   ├── UserCards.js             # User dashboard cards
│   └── UserGameCard.js          # User-game relationship logic
│
├── config/
│   ├ db.js     # Database connection
│   └ server.js # Express server entry point                
│                  
├── package.json
├── .env
└── README.md
│
Documents/
│   └── Final Project Requirements.pdf
```

## Code & Logic

Frontend Logic

The frontend uses vanilla JavaScript to dynamically load and render game data from data.json. Core responsibilities include:

*Fetching and parsing JSON data

*Creating reusable game cards

*Opening and populating modals

*Handling carousel navigation and autoplay

*Managing live search with debouncing

Example (data loading):

async function loadGamesData() {
  const res = await fetch("data.json");
  const json = await res.json();
  return json.games || [];
}

Modal System

*A single modal component is reused across pages. When a game card is clicked, its data attributes are read and injected into the modal dynamically, including images, YouTube embeds, ratings, and metadata.

Database Usage

*This project uses external JSON files as a data source instead of a database. The structure mirrors how data would be returned from a backend API and allows clean separation between data and UI logic.

## Screenshots

Add 4 screenshots (2 per member) with short captions describing each page.

## Setup

Steps to run the app:

1. `npm install`
2. Add `.env` file
3. `npm run dev`

## Contributions

Alexander Tran

Core JavaScript logic (data loading, cards, modals, carousel)

Game Gallery page

Shared CSS system and UI components

Live search functionality

Christian Salazar

Developer Reviews page

Upcoming Games page

FAQ page and accordion behavior

Additional styling and layout refinement

Both members contributed to frontend logic, styling, and UI decisions.
## API Setup

If your project uses external APIs (for example: OpenAI, Google Maps, or Weather API), include clear instructions for each one.

For every API used:

1. Explain how to sign up on the provider’s website.
2. Describe how to create or generate an API key.
3. If the API is paid or has usage limits, clearly mention that.
4. Note any extra steps needed (like enabling specific services, adding billing info, or setting access permissions).
5. Add where and how the key should be stored (for example, in the `.env` file).
