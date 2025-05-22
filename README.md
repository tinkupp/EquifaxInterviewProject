# Simple User Profile Management API and UI

This project contains a REST API that can be used to create, edit, view, and delete user profiles. 
It also includes a simple example of a front end integration of this API.

---

### Back end (API)

- Java Spring Boot API 
- Uses the firestore client to persist data in the Google datastore
- Uses a caching mechanism to improve the performance of user profile searches by ID

# Endpoints
- GET /users/{id}: retrieves the user profile information (minus SSN) for a single user profile
- GET /users: retrieves information about all user profiles. An optional search parameter can be specified for username or email.
- POST /users: Creates a new user profile. Username, email, and SSN must be provided in the request body.
- PUT /users/{id}: Updates a user profile. Username and email can be provided in the request body.
- DELETE /users/{id}: Deletes a user profile.


