# NASA Asteroids API

## Overview

This project is a Spring Boot-based REST API that interacts with NASA's Near Earth Object Web Service (NeoWs) to retrieve information about asteroids approaching Earth. It fetches raw data from the NASA API, processes it, and exposes several endpoints to provide useful asteroid data in different formats and levels of detail.

---

## Features

- **Fetch raw asteroid data** for a configurable number of upcoming days (up to 7 days).

- **Simplified asteroid data** representation with key information like name, average diameter, speed, close approach date, and orbiting planet.

- **Top dangerous asteroids** endpoint to list the largest potentially hazardous asteroids within the requested period.

- **Centralized exception handling** to provide meaningful HTTP responses for errors, including invalid parameters or external API failures.


---

## Architecture & Design

- **Separation of concerns**:

    - **NasaClient** handles external API communication using Spring's `RestTemplate`.

    - **AsteroidsService** processes raw JSON data, maps it to DTOs, and applies business logic such as filtering hazardous asteroids and sorting by size.

    - **AsteroidsController** exposes REST endpoints for different data views.

- **Use of modern Java features**:

    - Java Records for immutable DTOs.

    - `ObjectMapper` for JSON parsing and data extraction from NASA API responses.

- **Configurable API access** through properties (`nasa.api.url`, `nasa.api.key`), enabling easy switching of environments or API keys.

- **Robust error handling** with custom exceptions (`NasaApiException`) and a global exception handler for clean API responses.


---

## Available Endpoints

|Endpoint|Description|Parameters|
|---|---|---|
|`GET /api/v1/asteroids`|Returns raw asteroid data from NASA API|`days` (int, 1-7)|
|`GET /api/v1/asteroids/simplify`|Returns simplified asteroid data|`days` (int, 1-7)|
|`GET /api/v1/asteroids/top-dangerous`|Returns top 3 largest potentially hazardous asteroids|`days` (int, 1-7)|

---

## Requirements

- Java 17+

- Spring Boot 3.x

- Maven or Gradle for build

- NASA API key (free registration at NASA Open APIs portal)


---

## How to Run

1. Clone the repository.

2. Set your NASA API key and API URL in `application.properties` or environment variables:

   properties

   CopiarEditar

   `nasa.api.url=https://api.nasa.gov/neo/rest/v1/feed nasa.api.key=YOUR_API_KEY`

3. Build and run the Spring Boot application:

   bash

   CopiarEditar

   `./mvnw spring-boot:run`

4. Access endpoints via `http://localhost:8080/api/v1/asteroids?days=3` (or other endpoints as needed).