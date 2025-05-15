# TresorApp

A secure application for managing your secrets, built with a Java Spring Boot backend and a React frontend. It utilizes Docker for containerization and includes features for user management and secret storage. Key security aspects include secrets encryption and password hashing.

## Project Structure

The project is organized into two main components:

*   `183_12_1_tresorbackend_rupe-master/`: Contains the Java Spring Boot backend application.
*   `183_12_2_tresorfrontend_rupe-master/`: Contains the React frontend application.

Additionally, the `Documentation/` directory contains detailed information on security implementations:
*   `secrets_encryption_documentation.md`
*   `password_hashing_documentation.md`

## Features

*   User registration and login
*   Secure storage and retrieval of secrets
*   Password hashing mechanisms
*   Encryption for stored secrets

## Technologies Used

*   **Backend:**
    *   Java
    *   Spring Boot
    *   Maven
*   **Frontend:**
    *   React
    *   Node.js
*   **Database:**
    *   SQL (see `183_12_1_tresorbackend_rupe-master/tresordb.sql` for an example schema)
*   **Containerization:**
    *   Docker (for both backend and frontend)
*   **Web Server (Frontend):**
    *   Nginx (as per frontend Docker setup)

## Getting Started

Both the backend and frontend have their own detailed `README.md` files within their respective directories (`183_12_1_tresorbackend_rupe-master/README.md` and `183_12_2_tresorfrontend_rupe-master/README.md`). These provide specific setup, build, and run instructions.

### Quick Start with Docker

You can build and run both the backend and frontend using Docker:

**Backend:**

1.  Navigate to the `183_12_1_tresorbackend_rupe-master/` directory.
2.  Build the Docker image:
    ```bash
    docker build -t tresorbackendimg .
    ```
3.  Run the Docker container:
    ```bash
    docker run -p 8080:8080 --name tresorbackend tresorbackendimg
    ```
    (Ensure your database is set up and accessible as per the backend's `application.properties` and `tresordb.sql`.)

**Frontend:**

1.  Navigate to the `183_12_2_tresorfrontend_rupe-master/` directory.
2.  Build the Docker image:
    ```bash
    docker build -t tresorfrontendimg .
    ```
3.  Run the Docker container:
    ```bash
    docker run -p 80:80 --name tresorfrontend tresorfrontendimg
    ```
    (Ensure the backend is running and accessible to the frontend, configure via `.env` file if necessary.)

## API Information (Backend)

The backend provides APIs for various functionalities. Examples of API requests can be found in the `183_12_1_tresorbackend_rupe-master/httprequest/` directory:
*   `UserRequests.http`
*   `SecretRequests.http`

---
*(c) P.Rutschmann (as per sub-project READMEs)*
