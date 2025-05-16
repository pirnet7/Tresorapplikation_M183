package ch.bbw.pr.tresorbackend.controller;

import ch.bbw.pr.tresorbackend.model.Secret;
import ch.bbw.pr.tresorbackend.model.NewSecret;
import ch.bbw.pr.tresorbackend.model.EncryptCredentials;
import ch.bbw.pr.tresorbackend.model.User;
import ch.bbw.pr.tresorbackend.service.SecretService;
import ch.bbw.pr.tresorbackend.service.UserService;
import ch.bbw.pr.tresorbackend.util.EncryptUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SecretController
 * @author Peter Rutschmann
 */
@RestController
@AllArgsConstructor
@RequestMapping("api/secrets")
public class SecretController {

   private SecretService secretService;
   private UserService userService;

   // create secret REST API
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @PostMapping
   public ResponseEntity<String> createSecret2(@Valid @RequestBody NewSecret newSecret, BindingResult bindingResult) {
      System.out.println("SecretController.createSecret2 - Received NewSecret: " +
              "Title='" + (newSecret.getTitle() == null ? "null" : newSecret.getTitle()) + "', " +
              "Email='" + (newSecret.getEmail() == null ? "null" : newSecret.getEmail()) + "', " +
              "EncryptPassword is " + (newSecret.getEncryptPassword() == null || newSecret.getEncryptPassword().isEmpty() ? "null or empty" : "present") + ", " +
              "Content is " + (newSecret.getContent() == null ? "null" : "present"));

      //input validation
      if (bindingResult.hasErrors()) {
         List<String> errors = bindingResult.getFieldErrors().stream()
                 .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                 .collect(Collectors.toList());
         System.out.println("SecretController.createSecret " + errors);

         JsonArray arr = new JsonArray();
         errors.forEach(arr::add);
         JsonObject obj = new JsonObject();
         obj.add("message", arr);
         String json = new Gson().toJson(obj);

         System.out.println("SecretController.createSecret, validation fails: " + json);
         return ResponseEntity.badRequest().body(json);
      }
      System.out.println("SecretController.createSecret, input validation passed");

      User user = userService.findByEmail(newSecret.getEmail());
      if (user == null || user.getUserSalt() == null || user.getUserSalt().isEmpty()) {
         // Handle case where user or salt is not found
         System.out.println("SecretController.createSecret, user or user salt not found for email: " + newSecret.getEmail());
         JsonObject errorResponse = new JsonObject();
         errorResponse.addProperty("message", "User or user salt not found.");
         return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Gson().toJson(errorResponse));
      }

      try {
         EncryptUtil encryptUtil = new EncryptUtil(newSecret.getEncryptPassword(), user.getUserSalt());
         //transfer secret and encrypt content
         Secret secret = new Secret();
         secret.setUserId(user.getId());
         secret.setTitle(newSecret.getTitle());
         secret.setContent(encryptUtil.encrypt(newSecret.getContent().toString()));

         secretService.createSecret(secret);
         System.out.println("SecretController.createSecret, secret saved in db");
         JsonObject obj = new JsonObject();
         obj.addProperty("answer", "Secret saved");
         String json = new Gson().toJson(obj);
         System.out.println("SecretController.createSecret " + json);
         return ResponseEntity.accepted().body(json);
      } catch (Exception e) {
         System.out.println("SecretController.createSecret, encryption/decryption error: " + e.getMessage());
         JsonObject errorResponse = new JsonObject();
         errorResponse.addProperty("message", "Error processing secret: " + e.getMessage());
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Gson().toJson(errorResponse));
      }
   }

   // Build Get Secrets by userId REST API
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @PostMapping("/byuserid")
   public ResponseEntity<List<Secret>> getSecretsByUserId(@RequestBody EncryptCredentials credentials) {
      System.out.println("SecretController.getSecretsByUserId " + credentials);

      User user = userService.getUserById(credentials.getUserId()); // Assuming a method to get user by ID
      if (user == null || user.getUserSalt() == null || user.getUserSalt().isEmpty()) {
         System.out.println("SecretController.getSecretsByUserId, user or user salt not found for id: " + credentials.getUserId());
         return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Or appropriate error response
      }

      List<Secret> secrets = secretService.getSecretsByUserId(credentials.getUserId());
      if (secrets.isEmpty()) {
         System.out.println("SecretController.getSecretsByUserId secret isEmpty");
         return ResponseEntity.notFound().build();
      }
      //Decrypt content
      try {
         EncryptUtil encryptUtil = new EncryptUtil(credentials.getEncryptPassword(), user.getUserSalt());
         for (Secret secret : secrets) {
            try {
               secret.setContent(encryptUtil.decrypt(secret.getContent()));
            } catch (Exception e) { // Catch specific crypto exceptions if preferred
               System.out.println("SecretController.getSecretsByUserId, decryption failed for secret id " + secret.getId() + ": " + e.getMessage());
               secret.setContent("DECRYPTION_FAILED"); // Indicate decryption failure
            }
         }
      } catch (Exception e) { // Catch exceptions from EncryptUtil constructor
         System.out.println("SecretController.getSecretsByUserId, error initializing EncryptUtil: " + e.getMessage());
         // Optionally mark all secrets as DECRYPTION_FAILED or return an error
         secrets.forEach(s -> s.setContent("DECRYPTION_SETUP_FAILED"));
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(secrets); // Or a more generic error
      }

      System.out.println("SecretController.getSecretsByUserId " + secrets);
      return ResponseEntity.ok(secrets);
   }

   // Build Get Secrets by email REST API
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @PostMapping("/byemail")
   public ResponseEntity<List<Secret>> getSecretsByEmail(@RequestBody EncryptCredentials credentials) {
      System.out.println("SecretController.getSecretsByEmail " + credentials);

      User user = userService.findByEmail(credentials.getEmail());
      if (user == null || user.getUserSalt() == null || user.getUserSalt().isEmpty()) {
         System.out.println("SecretController.getSecretsByEmail, user or user salt not found for email: " + credentials.getEmail());
         return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
      }

      List<Secret> secrets = secretService.getSecretsByUserId(user.getId());
      if (secrets.isEmpty()) {
         System.out.println("SecretController.getSecretsByEmail secret isEmpty");
         return ResponseEntity.notFound().build();
      }
      //Decrypt content
      try {
         EncryptUtil encryptUtil = new EncryptUtil(credentials.getEncryptPassword(), user.getUserSalt());
         for (Secret secret : secrets) {
            try {
               secret.setContent(encryptUtil.decrypt(secret.getContent()));
            } catch (Exception e) {
               System.out.println("SecretController.getSecretsByEmail, decryption failed for secret id " + secret.getId() + ": " + e.getMessage());
               secret.setContent("DECRYPTION_FAILED");
            }
         }
      } catch (Exception e) {
         System.out.println("SecretController.getSecretsByEmail, error initializing EncryptUtil: " + e.getMessage());
         secrets.forEach(s -> s.setContent("DECRYPTION_SETUP_FAILED"));
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(secrets);
      }

      System.out.println("SecretController.getSecretsByEmail " + secrets);
      return ResponseEntity.ok(secrets);
   }

   // Build Get All Secrets REST API
   // http://localhost:8080/api/secrets
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @GetMapping
   public ResponseEntity<List<Secret>> getAllSecrets() {
      List<Secret> secrets = secretService.getAllSecrets();
      // Note: Decrypting all secrets here would require a way to get each user's password & salt.
      // This endpoint might need to return secrets in their encrypted form or be redesigned.
      // For now, returning as is (likely encrypted).
      return new ResponseEntity<>(secrets, HttpStatus.OK);
   }

   // Build Update Secrete REST API
   // http://localhost:8080/api/secrets/1
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @PutMapping("{id}")
   public ResponseEntity<String> updateSecret(
           @PathVariable("id") Long secretId,
           @Valid @RequestBody NewSecret newSecret,
           BindingResult bindingResult) {
      //input validation
      if (bindingResult.hasErrors()) {
         List<String> errors = bindingResult.getFieldErrors().stream()
                 .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                 .collect(Collectors.toList());
         System.out.println("SecretController.updateSecret, validation errors: " + errors);

         JsonArray arr = new JsonArray();
         errors.forEach(arr::add);
         JsonObject errorResponse = new JsonObject();
         errorResponse.add("message", arr);
         return ResponseEntity.badRequest().body(new Gson().toJson(errorResponse));
      }

      Secret dbSecret = secretService.getSecretById(secretId);
      if (dbSecret == null) {
         System.out.println("SecretController.updateSecret, secret not found in db with id: " + secretId);
         JsonObject errorResponse = new JsonObject();
         errorResponse.addProperty("message", "Secret not found.");
         return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Gson().toJson(errorResponse));
      }

      User user = userService.findByEmail(newSecret.getEmail());
      if (user == null || user.getUserSalt() == null || user.getUserSalt().isEmpty()) {
         System.out.println("SecretController.updateSecret, user or user salt not found for email: " + newSecret.getEmail());
         JsonObject errorResponse = new JsonObject();
         errorResponse.addProperty("message", "User or user salt not found.");
         return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Gson().toJson(errorResponse));
      }

      if (!dbSecret.getUserId().equals(user.getId())) {
         System.out.println("SecretController.updateSecret, user ID mismatch.");
         JsonObject errorResponse = new JsonObject();
         errorResponse.addProperty("message", "User not authorized to update this secret.");
         return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Gson().toJson(errorResponse));
      }

      try {
         EncryptUtil encryptUtilForVerification = new EncryptUtil(newSecret.getEncryptPassword(), user.getUserSalt());
         // Try to decrypt the existing content to verify the password
         encryptUtilForVerification.decrypt(dbSecret.getContent());

         // If decryption is successful, proceed to encrypt the new content
         EncryptUtil encryptUtilForEncryption = new EncryptUtil(newSecret.getEncryptPassword(), user.getUserSalt()); // Re-init or use same
         String encryptedNewContent = encryptUtilForEncryption.encrypt(newSecret.getContent().toString());

         Secret secretToUpdate = new Secret();
         secretToUpdate.setId(secretId);
         secretToUpdate.setUserId(user.getId());
         secretToUpdate.setTitle(newSecret.getTitle());
         secretToUpdate.setContent(encryptedNewContent);

         secretService.updateSecret(secretToUpdate); // updateSecret should handle the actual DB update

         System.out.println("SecretController.updateSecret, secret updated in db");
         JsonObject obj = new JsonObject();
         obj.addProperty("answer", "Secret updated");
         return ResponseEntity.accepted().body(new Gson().toJson(obj));

      } catch (Exception e) {
         System.out.println("SecretController.updateSecret, error during decryption verification or encryption: " + e.getMessage());
         JsonObject errorResponse = new JsonObject();
         // Be careful not to leak too much info in error messages
         errorResponse.addProperty("message", "Could not update secret. Password might be incorrect or data corrupted.");
         return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Gson().toJson(errorResponse));
      }
   }

   // Build Delete Secret REST API
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @DeleteMapping("{id}")
   public ResponseEntity<String> deleteSecret(@PathVariable("id") Long secretId) {
      // TODO: Consider adding authentication/authorization here.
      // For example, verify the user requesting deletion owns the secret,
      // possibly by requiring them to provide their password to decrypt a dummy part of it.
      // For now, direct deletion.

      Secret secret = secretService.getSecretById(secretId);
      if (secret == null) {
         System.out.println("SecretController.deleteSecret, secret not found with id: " + secretId);
         return new ResponseEntity<>("Secret not found.", HttpStatus.NOT_FOUND);
      }
      // We might need user context here to ensure only the owner can delete.
      // This requires changes to how delete is authorized.

      secretService.deleteSecret(secretId);
      System.out.println("SecretController.deleteSecret successfully: " + secretId);
      return new ResponseEntity<>("Secret successfully deleted!", HttpStatus.OK);
   }
}
