# Task: Build a User Authentication API using JWT (Access & Refresh Tokens)

## **Objective**:

Create a REST API that allows users to sign up, log in, and log out using JWT for authentication and authorization.
Implement Access and Refresh tokens for session management and build a secure API that retrieves userAccount information
once
authenticated.

## Requirements:

1. ### ~~Sign-up API~~:
    1. ~~Create a new userAccount with a username, email, and password.~~
    2. ~~Hash the password before saving it to the database.~~
2. ### ~~Login API~~:
    1. ~~Authenticate the userAccount using their email and password.~~
    2. ~~Generate both Access and Refresh tokens after successful login.~~
    3. ~~Return these tokens in the response.~~
3. ### **Logout API**:
    1. Invalidate the refresh token (e.g., store invalid tokens in a blacklist or delete from database).
4. ### ~~Refresh Token API~~:
    1. ~~Provide a route to refresh the Access token using the Refresh token~~.
    2. Ensure that the Access token has a short expiry time (e.g., 15 minutes) and the Refresh token has a longer
       expiry (e.g., 7 days).
5. ### User Info API (Protected API):
    1. Implement a route that returns the authenticated userAccount's information (e.g., username, email) when accessed
       with a
       valid Access token.
        * Take the payload and check if the token is still valid from the expiry time.
    2. This API should return a 401 Unauthorized error if the Access token is expired or invalid.

## Constraints:

* Use **JWT** for generating both Access and Refresh tokens.
    * **Access Token**: Include userAccount information and set a short expiration time.
    * **Refresh Token**: Use to generate new Access tokens without requiring login and set a longer expiration time.

## Bonus:

* Add refresh tokens to the database.
* Integrate mail sender to the new project.
* Implement role-based access control (e.g., differentiate between regular users and admins).
* ~~Implement a verification email system.~~

## Technology Requirements:

* ### Database:
*
    * ~~Use PostgreSQL to store userAccount credentials and tokens if needed.~~

