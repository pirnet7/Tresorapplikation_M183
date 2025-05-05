package ch.bbw.pr.tresorbackend.service;

import org.springframework.beans.factory.annotation.Value;
import java.security.SecureRandom;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.Base64;
/**
 * PasswordEncryptionService
 * @author Peter Rutschmann
 */
@Service
public class PasswordEncryptionService {
   @Value("${password.pepper}")
   private String pepper;
   private final SecureRandom secureRandom;

   public PasswordEncryptionService() {
      this.secureRandom = new SecureRandom();
   }

   /**
    * Hashe das Passwort mit BCrypt und "salt" und "pepper"
    */
   public String hashPassword(String password) {
      String pepperedPassword = password + pepper;
      String salt = BCrypt.gensalt(12);
      return BCrypt.hashpw(pepperedPassword, salt);
   }

   /**
    * Verifiziere das Passwort gegen ein Hash
    */
   public boolean verifyPassword(String password, String hashedPassword) {
      String pepperedPassword = password + pepper;

      return BCrypt.checkpw(pepperedPassword, hashedPassword);
   }
}