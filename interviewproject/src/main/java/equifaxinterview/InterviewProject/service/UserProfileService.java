package equifaxinterview.InterviewProject.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import equifaxinterview.InterviewProject.domain.UserProfile;
import equifaxinterview.InterviewProject.exception.BadRequestException;
import equifaxinterview.InterviewProject.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final Firestore firestore;
    private final BasicEncryptionService encryptionService;
    private static final String COLLECTION_NAME = "UserProfile";

    public UserProfile saveUserProfile(UserProfile userProfile) {
        CollectionReference userProfiles = firestore.collection("UserProfile");

        // Encrypt SSN before storing
        String encryptedSSN = encryptionService.encrypt(userProfile.getSocialSecurityNumber());

        // Check for existing user
        checkForExistingUser(userProfile);

        // Build a map of fields
        Map<String, Object> docData = new HashMap<>();
        docData.put("username", userProfile.getUsername());
        docData.put("email", userProfile.getEmail());
        docData.put("socialSecurityNumber", encryptedSSN);

        // Add the document. ID is generated automatically.
        ApiFuture<DocumentReference> addedDocRef = userProfiles.add(docData);

        // Wait for completion and get the auto-generated ID
        String generatedId;
        try {
            generatedId = addedDocRef.get().getId();
        }catch(Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error saving user profile", e);
        }

        return UserProfile.builder()
                .id(generatedId)
                .username(userProfile.getUsername())
                .email(userProfile.getEmail())
                .build();
    }

    // On the first call for a given id, this method will fetch the user profile from Firestore and cache it.
    // On subsequent calls with the same id, it will check the cache first before hitting Firestore again.
    // Cache entries expire after 10 minutes.
    // The Cache key is the user ID.
    @Cacheable(value = "userProfiles", key = "#id")
    public UserProfile getUserProfile(String id) {
        DocumentReference docRef = firestore.collection("UserProfile").document(id);
        DocumentSnapshot document;

        try {
            document = docRef.get().get();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve user profile", e);
        }

        if (!document.exists()) {
            throw new ResourceNotFoundException("User with id " + id + " not found.");
        }

        return UserProfile.builder()
                .id(document.getId())
                .username(document.getString("username"))
                .email(document.getString("email"))
                .build();
    }

    public String deleteUserProfile(String id) {
        DocumentReference docRef = firestore.collection("UserProfile").document(id);
            checkForExistingUserById(docRef.getId());

            try {
                docRef.delete().get();
            }catch(Exception e){
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting user profile", e);
            }
            return "User deleted successfully.";
    }


    public UserProfile updateUserProfile(String id, UserProfile userProfile) {
        DocumentReference docRef = firestore.collection("UserProfile").document(id);

        // Check if user exists
        checkForExistingUserById(docRef.getId());

        Map<String, Object> updates = new HashMap<>();
        updates.put("username", userProfile.getUsername());
        updates.put("email", userProfile.getEmail());

        // Update the document with the new values
        try {
            docRef.update(updates).get();
        }catch(Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating user profile", e);
        }

        return UserProfile.builder()
                .id(id)
                .username(userProfile.getUsername())
                .email(userProfile.getEmail())
                .build();
    }


    public List<UserProfile> getUserProfiles(String searchString) {
        List<UserProfile> results = new ArrayList<>();
        List<QueryDocumentSnapshot> docs;

        if (searchString == null || searchString.trim().isEmpty()) {
            // If no search string, get all user profiles
            docs = getAllUserProfiles();  // You need to implement this method to fetch all profiles
        } else {
            docs = new ArrayList<>();
            // Combine results of username and email searches
            docs.addAll(checkExistingByProperty("username", searchString));
            docs.addAll(checkExistingByProperty("email", searchString));
            // Check if any documents were found
            if(docs.isEmpty()){
                throw new ResourceNotFoundException("No user profiles found for search: " + searchString);
            }
        }



        for (QueryDocumentSnapshot doc : docs) {
            results.add(UserProfile.builder()
                    .id(doc.getId())
                    .username(doc.getString("username"))
                    .email(doc.getString("email"))
                    .build());
        }

        return results;
    }

    private void checkForExistingUser(UserProfile userProfile){
        List<QueryDocumentSnapshot> existingDocs = new ArrayList<>();
        existingDocs.addAll(checkExistingByProperty("username", userProfile.getUsername()));
        existingDocs.addAll(checkExistingByProperty("email", userProfile.getEmail()));

        if (!existingDocs.isEmpty()) {
            throw new BadRequestException("User with username or email already exists");
        }
    }

    private void checkForExistingUserById(String id){
        DocumentReference docRef = firestore.collection("UserProfile").document(id);
        DocumentSnapshot document;

        try {
            document = docRef.get().get();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve user profile", e);
        }

        if (!document.exists()) {
            throw new ResourceNotFoundException("User with id " + id + " not found.");
        }
    }

    private List<QueryDocumentSnapshot> checkExistingByProperty(String property, String value) {
        CollectionReference userProfiles = firestore.collection("UserProfile");
        ApiFuture<QuerySnapshot> query = userProfiles.whereEqualTo(property, value).get();
        List<QueryDocumentSnapshot> documents = new ArrayList<>();
        try {
            documents = query.get().getDocuments();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error checking for existing user", e);
        }
        return documents;
    }

    private List<QueryDocumentSnapshot> getAllUserProfiles(){
        try{
            CollectionReference usersCollection = firestore.collection("UserProfile");

            // Retrieve all documents in the collection
            ApiFuture<QuerySnapshot> future = usersCollection.get();

            // Block on response
            QuerySnapshot querySnapshot = future.get();

            // Return the list of documents
            return querySnapshot.getDocuments();

        } catch (Exception e) {
            throw new RuntimeException("Failed to get all user profiles", e);
        }
    }
}
