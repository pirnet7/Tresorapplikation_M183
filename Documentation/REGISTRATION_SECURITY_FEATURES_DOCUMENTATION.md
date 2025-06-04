# Registration Security Features Documentation

This document outlines the security features implemented during the user registration process for the Tresor Application, covering password strength validation and reCAPTCHA v3 integration.

## 1. Password Strength Validation

### 1.1. Requirement

During user registration, the strength of the chosen password must be validated in both the frontend and backend components to ensure users create secure passwords.

### 1.2. Password Rules Enforced

The following criteria must be met for a password to be considered strong:
-   Minimum length: 8 characters.
-   Contains at least one uppercase letter (A-Z).
-   Contains at least one lowercase letter (a-z).
-   Contains at least one digit (0-9).
-   Contains at least one special character (e.g., !@#$%^&*).

### 1.3. Frontend Implementation (Password Strength)

Client-side validation provides immediate, interactive feedback to the user within the registration form.

-   **File:** `183_12_2_tresorfrontend_rupe-master/src/pages/user/RegisterUser.js`
-   **Key Functions & State:**
    -   `passwordCriteriaStatus` (state): An object that tracks the fulfillment status (true/false) of each individual password rule.
    -   `updatePasswordCriteria(password)` (function): Called on every change to the password input field. It evaluates the current password against all rules and updates the `passwordCriteriaStatus` state.
    -   `isPasswordStrong(password)` (function): Called during form submission. It checks if all password criteria are met.
-   **Behavior - Interactive Checklist:**
    -   Below the password input field, a list of all password requirements is displayed.
    -   As the user types, each requirement in the list visually changes from a standard indicator (•) to a green checkmark (✓) when met.
    -   This provides real-time feedback, guiding the user to create a strong password.
-   **Behavior - Form Submission (Password Strength):**
    -   If the password does not meet all requirements upon submission, an error message is displayed, and the form submission is prevented.
    -   The input field types for password and password confirmation are set to `password`.

### 1.4. Backend Implementation (Password Strength)

Server-side validation ensures that password rules are enforced even if client-side checks are bypassed.

-   **File:** `183_12_1_tresorbackend_rupe-master/src/main/java/ch/bbw/pr/tresorbackend/service/UserServiceImpl.java`
-   **Method:** Password validation logic is integrated into the `registerUser` method.
-   **Behavior:**
    -   The password from `UserRegistrationDto` is checked against the rules.
    -   Validation occurs before password hashing and user persistence.
    -   If rules are not met, registration is aborted, and an error is returned.

## 2. reCAPTCHA v3 Integration

### 2.1. Requirement

To protect against spam and abuse, user registration is verified using Google reCAPTCHA v3. This provides a frictionless experience for users by analyzing behavior in the background.

### 2.2. Frontend Implementation (reCAPTCHA)

-   **Library:** `react-google-recaptcha-v3`
-   **Setup:**
    -   The main application in `src/index.js` is wrapped with `GoogleReCaptchaProvider`, configured with the site key: `6Ld0WlUrAAAAAFOp4pajzCxW_Nw7XU0NAwu-JnIb`.
-   **File:** `183_12_2_tresorfrontend_rupe-master/src/pages/user/RegisterUser.js`
    -   The `useGoogleReCaptcha` hook provides the `executeRecaptcha` function.
    -   In the `handleSubmit` function, `executeRecaptcha('register')` is called to obtain a token.
-   **Data Submission:**
    -   The obtained reCAPTCHA token is included in the registration data sent to the backend via the `postUser` function in `src/comunication/FetchUser.js`.
    -   The `postUser` function was updated to accept and send this token in the request payload as `recaptchaToken`.

### 2.3. Backend Implementation (reCAPTCHA)

-   **Configuration (`application.properties`):**
    -   Site Key: `6Ld0WlUrAAAAAFOp4pajzCxW_Nw7XU0NAwu-JnIb`
    -   Secret Key: `6Ld0WlUrAAAAAI0rKjDucvBAsGHorUoNLn_fuGbQ`
    -   Verification URL: `https://www.google.com/recaptcha/api/siteverify`
-   **DTO:**
    -   `ch.bbw.pr.tresorbackend.dto.UserRegistrationDto.java` includes a `recaptchaToken` field to receive the token from the frontend.
-   **Service Layer:**
    -   `ch.bbw.pr.tresorbackend.service.RecaptchaService.java` handles the verification of the token by making a request to Google's verification URL.
    -   `ch.bbw.pr.tresorbackend.service.UserServiceImpl.java`, in its `registerUser` method, utilizes `RecaptchaService` to validate the token before proceeding with user creation.
    -   If reCAPTCHA validation fails, the registration is aborted.
-   **Beans:** A `RestTemplate` bean is configured for the `RecaptchaService` to communicate with Google.

### 2.4. Critical Discussion on reCAPTCHA (based on Claus Nehring)

While reCAPTCHA is effective against automated abuse, its use, particularly versions 2 and 3, raises important considerations:

-   **Data Collection & Privacy:** Google uses reCAPTCHA to collect extensive user interaction data across numerous websites. There are concerns that this data could be used for creating detailed user profiles for targeted advertising or other purposes not explicitly stated. The lack of transparency from Google regarding the specifics of data collection and usage is a significant privacy concern.
-   **User Experience & Performance:** Although reCAPTCHA v3 aims to be frictionless, the underlying analysis can still impact website load times and increase HTTP requests.
-   **Dependence on Google:** Relying on a third-party service like Google for a critical function like bot detection creates a dependency.
-   **Alternatives:** While not implemented in this project due to requirements, it's worth noting that alternatives exist, though they may have their own trade-offs in terms of effectiveness and implementation complexity.

The decision to use reCAPTCHA v3 was made balancing its effectiveness against spam with these considerations. For this project, its benefits in preventing abuse were deemed to outweigh the potential downsides, but these concerns are acknowledged.

This documentation outlines the essential aspects of the registration security features.
