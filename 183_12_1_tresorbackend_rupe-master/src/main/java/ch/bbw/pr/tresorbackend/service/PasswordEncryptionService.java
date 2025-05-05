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
      //todo anpassen!
   }

   public String hashPassword(String password) {
      //todo anpassen!
      return password;
   }
}