# Simple User Profile Management API and UI

This project contains a REST API that can be used to create, edit, view, and delete user profiles. 
It also includes a simple example of a front end integration of this API.

---

### Back end (API)

- Java Spring Boot API 
- Uses the firestore client to persist data in the Google datastore
- Uses a caching mechanism to improve the performance of user profile searches by ID

### Endpoints

The application exposes the following RESTful endpoints for managing user profiles:

- **GET `/users/{id}`**  
  Retrieves the profile information for a single user by ID (excluding the SSN).  
  Responses for repeated requests with the same ID are cached for performance.

- **GET `/users`**  
  Retrieves a list of all user profiles.  
  Supports optional query parameters:
  - 'search' (String)

- **POST `/users`**  
  Creates a new user profile.  
  The request body must include:
  - `username` (String)
  - `email` (String)
  - `ssn` (String)

- **PUT `/users/{id}`**  
  Updates the profile for an existing user by ID.  
  The request body may include:
  - `username` (String)
  - `email` (String)

- **DELETE `/users/{id}`**  
  Deletes the user profile with the specified ID.


### Code Structure

I divided the classes for this project into different packages.  
Each package serves a specific purpose in maintaining separation of concerns and code organization.

- **controller**:  
  Defines the REST API endpoints that handle incoming HTTP requests and delegates logic to the service layer.

- **service**:  
  Contains business logic that is applied before accessing data.

- **config**:  
  Contains configuration classes for the application, such as the caching configuration and the jackson configuration.

- **exception**:  
  Defines custom exception classes used throughout the application.

- **domain**:  
  Contains the core domain models (entities, DTOs, etc.) that represent the data used by the application.

- **advice**:  
  Contains exception handlers (annotated with `@ControllerAdvice`) to manage error responses and return the correct status codes.



