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
  - `search` (String)

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

### Caching

To improve performance and reduce the number of reads against the datastore, user profile data is cached when a user searches for a profile by ID.
The `getUserProfile(String id)` method is annotated with @Cacheable. This means:
- On the first call for a given id, this method will fetch the user profile from the datastore and cache it.
- On subsequent calls with the same id, it will check the cache first before hitting the datastore again.
- Cache entries expire after 10 minutes.
- The maximum number of entries in the cache is 1000.
- The `id` is the cache key.
The expiration time and cache size are configured in the `CacheConfig` class in the **config** layer.

### Security

This application is configured to use SSL encryption. It listens on port 443, the default for HTTPS, and it uses a PCK12 keystore to serve encrypted traffic.
- All requests to the API must use the `HTTPS://` protocol.
- Requests sent over plain `HTTP` will be rejected. SSL is strictly enforced.
This ensures that data is safe in transit.

**Note:** The current SSL configuration is suitable for development purposes. For production environments, SSL would most likely be offloaded to a different service
such as a reverse proxy, and certificate renewal would be automated in some way.

When a user is created, they provide their social security number. This is sensitive information that should be encrypted not only in transit, but also in storage.
The users' social security numbers are encrypted when they are stored, and then they are not accessible to users of the API afterwards.
I have implemented an encryption service (`BasicEncryptionService`) to encrypt social security numbers in the service layer. Here's how it works:
- **Algorithm:** AES
- **Key Generation:** A 128-bit secret key using `KeyGenerator` during service construction. This key is used for encryption and decryption.
- **Encryption Flow:**
    - The plaintext is passed to the encrypt() method.
    - It initializes a Cipher in ENCRYPT_MODE using the secret key.
    - The plaintext is encrypted into a byte array.
    - The result is Base64-encoded to produce a string-safe representation.
- **Decryption Flow:**
    - The decrypt() method takes a Base64-encoded encrypted string.
    - It decodes the string into bytes.
    - The cipher is initialized in DECRYPT_MODE using the same secret key.
    - The original plaintext is recovered.

**Note:** The key used for the `EncryptionService` is in-memory only. This is also suitable for a development environment, but not for a production environment.
In a production environment, it may be suitable to store the key in a secret storage system, such as Vault.

### Considerations for Performance

There are several code changes I could still work on to improve the performance of this API.
If I were to continue working on this, these are the next things I would improve:

1. **Blocking calls to Firestore**
   - The firestore interactions in this application use blocking `ApiFuture.get()` calls.
   - This blocks the thread until the operation completes.
   - This could be improved by using an asynchronous pattern for reads to handle firestore futures in a non-blocking way.

2. **Full Collection Scan**
   - `getUserProfiles` retrieves the entire UserProfile collection
   - For large datasets, this is expensive and can result in high response times or costly reads.
   - I could paginate the results of `getuserProfiles` using `limit()` and `startAfter()`

3. **No Batch Writes for Updates/Deletes**
   - Each document is written or deleted individually.
   - In a scenario where multiple documents need to be updated or deleted simultaneously, batch operations would reduce the number of network round trips.
   - I could use firestore's `WriteBatch` API for bulk operations when needed.

### Front End

I have included a very basic React application that is integrated with my API. It allows you to **create**, **read**, **update**, **delete**, and **search**
user records. Each user has a username, email, and social security number, however, social security numbers are not displayed to users.

  **Features**
    
    - View a list of users (excluding SSN)
    - Search for a single user by ID (result excludes SSN)
    - Edit a user's username or email
    - Create a user (includes SSN)
    - Delete a user
    - Search for users by username or email (result excludes SSN)
    - Basic form validation on required fields
  
  **Notes**
    
    - The SSN field is only required and show when creating a user
    - The API does not return SSN for security purposes
  
  **Technologies used**
    
    - React
    - Javascript
    - Fetch API for HTTP requests
    - Tailwind CSS for styling

### AI Tools

