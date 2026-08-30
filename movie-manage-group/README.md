# Movie Manager — Group Practice (AI Fundamentals, Lesson 1)

A Spring Boot movie-tracking app, combined from the group's individual Movie Manager
projects (Phase 1), enhanced with an AI-powered feature (Phase 2).

## Features

**Base app (CRUD):**
- Add, view, edit, and delete movies
- Track genre, director, release year, watched status, personal rating, and notes

**Group AI enhancement — "AI Movie Assistant":**
The group discussed what would help a user *choose or review* movies, and landed on
two complementary AI features built on top of an LLM (OpenAI's chat completion API):

1. **AI Summary** — generates a short, spoiler-free blurb about the movie, useful when
   you've forgotten what a title in your list is even about.
2. **Watch Next Recommendations** — given a movie you just watched, the AI suggests
   3 similar movies you might enjoy next, with a one-sentence reason for each,
   aware of what's already in your collection.

Both are triggered by buttons on the movie's detail page and are cached on the
`Movie` record so you don't re-call the API every time you view the page.

## Tech Stack
- Java 17, Spring Boot 3.2
- Spring MVC + Thymeleaf (server-rendered views)
- Spring Data JPA + H2 in-memory database
- Bean Validation for form input
- Java's built-in `HttpClient` for calling the OpenAI API (no extra HTTP library needed)

## Setup

1. Clone the repo and open it in your IDE.
2. **Set your OpenAI API key** as an environment variable (do NOT hardcode it or commit it):
   ```bash
   export OPENAI_API_KEY=sk-...your-key-here...
   ```
   If no key is set, the AI buttons still work but return a friendly placeholder
   message instead of calling the API — the rest of the app functions normally.
3. Run the app:
   ```bash
   ./mvnw spring-boot:run
   ```
4. Visit `http://localhost:8080/movies`

## Project structure

```
src/main/java/com/launchcode/moviemanager/
├── MovieManagerApplication.java
├── models/
│   ├── Movie.java                 # entity
│   └── data/MovieRepository.java  # Spring Data JPA repo
├── services/
│   └── AiMovieService.java        # calls OpenAI, has graceful fallback
└── controllers/
    └── MovieController.java       # CRUD + AI routes

src/main/resources/
├── application.properties
├── static/css/styles.css
└── templates/
    ├── fragments/layout.html
    └── movies/{index,add,edit,view}.html
```

## Group Notes (Phase 1 & 2)

- **Phase 1:** Each member demoed their individual Movie Manager app. We picked the
  cleanest CRUD implementation as our base and merged in the best ideas from
  each person's version (e.g., the `watched` flag and star rating field).
- **Phase 2:** We brainstormed features that help a user *choose or review* movies
  and agreed an AI assistant that summarizes movies and recommends what to watch
  next was the most broadly useful addition, so we built the `AiMovieService`
  together and wired it into the movie detail page.

## Submission
Push this repo to a **public** GitHub repository named:
`practice-movie-app-group[FirstName]-[LastInitial]`
and submit the repo URL.