# Password Strength Validation Documentation

## 1. Requirement

During user registration, the strength of the chosen password must be validated in both the frontend and backend components of the Tresor Application. This is to ensure users create secure passwords.

## 2. Password Rules Enforced

The following criteria must be met for a password to be considered strong:
-   Minimum length: 8 characters.
-   Contains at least one uppercase letter (A-Z).
-   Contains at least one lowercase letter (a-z).
-   Contains at least one digit (0-9).
-   Contains at least one special character (e.g., !@#$%^&*).

## 3. Frontend Implementation

Client-side validation provides immediate, interactive feedback to the user within the registration form.

-   **File:** `183_12_2_tresorfrontend_rupe-master/src/pages/user/RegisterUser.js`
-   **Key Functions & State:**
    -   `passwordCriteriaStatus` (state): An object that tracks the fulfillment status (true/false) of each individual password rule (e.g., `minLength`, `hasUpperCase`).
    -   `updatePasswordCriteria(password)` (function): Called on every change to the password input field. It evaluates the current password against all rules and updates the `passwordCriteriaStatus` state.
    -   `isPasswordStrong(password)` (function): Called during form submission. It checks if all password criteria are met.
-   **Behavior - Interactive Checklist:**
    -   Below the password input field, a list of all password requirements is displayed.
    -   As the user types, each requirement in the list visually changes:
        -   Initially, or if a rule is not met, it's displayed with a standard indicator (e.g., •) and color.
        -   Once a rule is met, its indicator changes to a checkmark (✓) and the text turns green.
    -   This provides real-time feedback, guiding the user to create a strong password.
-   **Behavior - Form Submission:**
    -   When the user attempts to submit the form, the `isPasswordStrong` function is called.
    -   If the password does not meet all requirements, a general error message is displayed (e.g., "Please ensure all password requirements are met (see checklist below)."), and the form submission is prevented.
    -   The input field types for password and password confirmation are set to `password` to mask input.

## 4. Backend Implementation

Server-side validation ensures that the password rules are enforced even if client-side checks are bypassed.

-   **File:** `183_12_1_tresorbackend_rupe-master/src/main/java/ch/bbw/tresorbackend/service/UserServiceImpl.java`
-   **Method:** Password validation logic is integrated into the `registerUser` method (or a helper method called by it).
-   **Behavior:**
    -   The password from the registration request (`UserRegistrationDto`) is checked against the same set of rules.
    -   This validation occurs before the password is hashed and the user entity is persisted.
    -   If the password does not meet the requirements, the registration process is aborted, and an error is returned to the client.

This documentation outlines the essential aspects of the password strength validation feature.
