// src/main/java/atlan/evently/atlan/auth/web/AuthController.java
package atlan.evently.atlan.auth.controller;

import atlan.evently.atlan.auth.dto.AuthResponse;
import atlan.evently.atlan.auth.dto.RegisterRequest;
import atlan.evently.atlan.security.jwt.JwtService;
import atlan.evently.atlan.user.model.User;
import atlan.evently.atlan.user.service.UserService;
import atlan.evently.atlan.auth.dto.AuthRequest;
import atlan.evently.atlan.user.web.UserMapper;
import atlan.evently.atlan.user.web.dto.UserRegisterRequest;
import atlan.evently.atlan.user.web.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * REST Controller for handling user authentication and registration.
 * <p>
 * This controller provides public endpoints for users to log in (authenticate)
 * and register new accounts.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication Management", description = "APIs for user login and registration")
public class AuthController {


    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final PasswordEncoder encoder;
    private final UserService users;


    /**
     * Constructs an AuthController with the necessary dependencies.
     *
     * @param authManager The Spring Security AuthenticationManager to process credentials.
     * @param jwtService The service responsible for generating JWTs.
     * @param encoder The service for hashing passwords.
     * @param users The service for user data access and creation.
     */
    public AuthController(AuthenticationManager authManager, JwtService jwtService,
                          PasswordEncoder encoder, UserService users) {
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.encoder = encoder;
        this.users = users;
    }


    /**
     * Authenticates a user and returns a JWT upon success.
     *
     * @param req The login request containing the user's email and password.
     * @return A ResponseEntity containing the JWT in an AuthResponse, or a 401 Unauthorized status on failure.
     */
    @Operation(
            summary = "User Login",
            description = "Authenticates a user with their email and password. On success, it returns a JWT for use in subsequent requests."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentication successful", content = { @Content(schema = @Schema(implementation = AuthResponse.class), mediaType = "application/json") }),
            @ApiResponse(responseCode = "401", description = "Unauthorized, invalid credentials", content = { @Content(schema = @Schema()) })
    })
    @SecurityRequirements // removes inherited/global security for this operation
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest req) {
        try {
            var authToken = new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword());
            authManager.authenticate(authToken);
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).build();
        }


// After successful authentication, fetch user details from the database to build the token
        var domainUser = users.findByEmail(req.getEmail()).orElseThrow();
        var userDetails = org.springframework.security.core.userdetails.User
                .withUsername(domainUser.getEmail())
                .password(domainUser.getPasswordHash()) // Password is not exposed, just used by UserDetails
                .authorities("ROLE_" + domainUser.getRole().name())
                .build();


        String token = jwtService.generateToken(userDetails);
        return ResponseEntity.ok(new AuthResponse(token));
    }


    /**
     * Registers a new user with a securely hashed password.
     * This endpoint is a more secure alternative to the one in UserController.
     *
     * @param req The registration request containing email, password, and role.
     * @return A ResponseEntity with HTTP 200 OK status on success.
     */
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account with a securely hashed password. The role defaults to 'USER' if not specified as 'ADMIN'."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input, such as a malformed email", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "409", description = "Conflict, the email is already registered", content = { @Content(schema = @Schema()) })
    })
    @SecurityRequirements // removes inherited/global security for this operation
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegisterRequest req) {
        User.Role role = "ADMIN".equalsIgnoreCase(req.getRole()) ? User.Role.ADMIN : User.Role.USER;


// --- FIX IS HERE ---
// 1. Get the plain-text password from the request
        String plainPassword = req.getPassword();


// 2. Hash the password using the encoder
        String hashedPassword = encoder.encode(plainPassword);


// 3. Save the HASHED password to the database, not the plain one
        User entity = users.register(req.getEmail(), hashedPassword, role);
// --- END OF FIX ---


        return ResponseEntity.ok(UserMapper.toResponse(entity));
    }
}
