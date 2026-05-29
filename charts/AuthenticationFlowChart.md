## Authentication Flow Chart


### Phase 1: Login Phase
###### Happens only once when the user logs in.
Example:
```
POST /auth/login
```
Body:
```
{
    "username":"username",
    "password":"secret"
}
```
Flow:
```
[CLIENT]
│
│ POST /auth/login (username, password)
▼
[AuthController.login()]
│
│ Calls authService.login()
▼
[AuthService.login()]
│
├─► authenticate(username, password)
│       │
│       │ Creates UsernamePasswordAuthenticationToken
│       ▼
│   [AuthenticationManager.authenticate()]
│       │
│       │ Delegates to DaoAuthenticationProvider
│       ▼
│   [DaoAuthenticationProvider]
│       │
│       ├─► Calls userDetailsService.loadUserByUsername()
│       │       │
│       │       ▼
│       │   [ShoppiqUserDetailService]
│       │       │
│       │       │ Queries UserRepository
│       │       ▼
│       │   [UserRepository.findUserByUsername()]
│       │
│       └─► Validates password using PasswordEncoder
│
├─► (on success) userDetailsService.loadUserByUsername() again
│       │
│       ▼
│   [UserDetails object]
│
└─► jwtAuthenticationUtils.generateToken(userDetails)
│
│ Builds JWT with subject, issuedAt, expiration
▼
[Signed JWT token]

Return JwtResponse(token) to client
│
▼
[CLIENT stores token]
```
After this phase, Spring Security forgets everything because the app is stateless.

### Phase 2: Request Authentication Phase
###### Happens on every protected request.
Now the client no longer sends username/password.
Instead, in header we send:
```
Authorization: Bearer eyJhbGc...
```
Flow:
```
[CLIENT]
│
│ Request with Authorization: Bearer <token>
▼
[JwtAuthenticationFilter.doFilterInternal()]
│
├─► Extracts token from header
├─► Calls jwtAuthenticationUtils.getUsernameFromToken()
│       │
│       └─► getClaimsFromToken() → parses & verifies signature
│
├─► If username exists & not already authenticated:
│       │
│       ├─► userDetailsService.loadUserByUsername(username)
│       │
│       └─► jwtAuthenticationUtils.validateToken(token, userDetails)
│               │
│               ├─► Compare username
│               └─► Check expiration (isTokenExpired())
│
├─► Creates UsernamePasswordAuthenticationToken
├─► Stores it in SecurityContextHolder
│
▼
[Request proceeds to secured resource]
```
---
Also why do we load user again?
We already authenticated here:
```
Login
↓
AuthenticationManager
```
So why later:
```
userDetailsService.loadUserByUsername(...)
```
again? Because the JWT only contains:
```
{
    "sub":"prabhat",
    "exp":123456789
}
```
It does NOT contain:
```
Roles
Authorities
Permissions
Account status
```
Spring Security needs:
```
userDetails.getAuthorities()
```
to support:
```
@PreAuthorize
hasRole("ADMIN")
```
So every request:
```
JWT
↓
Username
↓
Load UserDetails
↓
Get Roles
```
---
Think of it like this
Login
```
Passport Office
```
We prove who we are. Then receive:
```
Passport (JWT)
```
Every future request
```
Airport Security
```
We don't prove identity again with birth certificate. We show:
```
Passport (JWT)
```
Security checks:
```
Passport valid?
Expired?
Tampered?
```
and lets us through.