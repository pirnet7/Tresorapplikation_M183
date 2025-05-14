package ch.bbw.pr.tresorbackend.model;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * NewSecret
 * @author Peter Rutschmann
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewSecret {
   @NotEmpty (message="Title is required.")
   @Size(max = 255, message = "Title cannot exceed 255 characters.")
   private String title;

   @NotEmpty (message="email is required.")
   private String email;

   @NotNull (message="secret is required.")
   private JsonNode content;

   @NotEmpty (message="encryption password id is required.")
   private String encryptPassword;
}