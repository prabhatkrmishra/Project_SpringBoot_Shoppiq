## Authentication Flow Chart

### Phase 1: Login Phase

###### Happens only once when the user logs in.

Example:

```
POST /auth/login
```

Body:

```json
{
  "username": "username",
  "password": "secret",
  "rememberMe": true
}
```

Flow:

```
[CLIENT]
│
│ POST /auth/login (username, password, rememberMe)
│ credentials: "include"   ← tells browser to accept Set-Cookie
▼
[AuthController.login(jwtRequest, HttpServletResponse)]
│
│ Calls authService.login(request, response)
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
│       │   [CustomUserDetailService]
│       │       │
│       │       │ Queries UserRepository
│       │       ▼
│       │   [UserRepository.findUserByUsername()]
│       │
│       └─► Validates password using PasswordEncoder (BCrypt)
│
├─► (on success) userDetailsService.loadUserByUsername() again
│       │
│       ▼
│   [UserDetails object]
│
├─► jwtAuthenticationUtils.generateToken(userDetails, expiryMs)
│       │
│       │ Builds JWT with subject, issuedAt, expiration
│       ▼
│   [Signed JWT string]
│
└─► buildJwtCookie(token, maxAge)
        │
        │ HttpOnly; Secure; SameSite=Strict; Path=/
        │ rememberMe=true  → Max-Age=<seconds>  (persistent, survives browser restart)
        │ rememberMe=false → no Max-Age          (session cookie, cleared on browser close)
        ▼
    response.addCookie(cookie)
        │
        ▼
    Set-Cookie: jwt=<token>; HttpOnly; Secure; SameSite=Strict; Path=/

Return JwtResponse { "message": "Login successful" } to client
│
│ NOTE: the token is NOT in the response body.
│       the browser stores the cookie automatically.
│       JavaScript cannot read it (HttpOnly).
▼
[CLIENT — cookie stored by browser, invisible to JS]
```

After this phase, Spring Security forgets everything because the app is stateless.

---

### Phase 2: Request Authentication Phase

###### Happens on every protected request.

Previously the client sent the token in a header:

```
Authorization: Bearer eyJhbGc...       ← old approach, removed
```

Now the browser attaches the cookie automatically — no JavaScript involved:

```
Cookie: jwt=eyJhbGc...                 ← sent by browser on every request
```

Flow:

```
[CLIENT]
│
│ GET /item/all
│ Cookie: jwt=eyJhbGc...   ← browser attaches automatically (credentials: "include")
│ No Authorization header
▼
[JwtAuthenticationFilter.doFilterInternal()]
│
├─► extractJwtFromCookies(request)
│       │
│       └─► Arrays.stream(request.getCookies())
│               .filter(c -> "jwt".equals(c.getName()))
│               .map(Cookie::getValue)
│               .findFirst()
│
├─► jwtAuthenticationUtils.getUsernameFromToken(token)
│       │
│       └─► getClaimsFromToken() → Jwts.parser().verifyWith(key).parseSignedClaims()
│
├─► If username found & SecurityContext not yet populated:
│       │
│       ├─► userDetailsService.loadUserByUsername(username)
│       │
│       └─► jwtAuthenticationUtils.validateToken(token, userDetails)
│               │
│               ├─► Compare username against token subject
│               └─► isTokenExpired() → checks expiration claim
│
├─► Creates UsernamePasswordAuthenticationToken(userDetails, null, authorities)
├─► Stores it in SecurityContextHolder
│
▼
[Request proceeds to secured resource]
```

---

#### Why do we load the user again on every request?

We already authenticated here:

```
Login → AuthenticationManager
```

So why call `userDetailsService.loadUserByUsername(...)` again on every request?

Because the JWT only contains:

```json
{
  "sub": "prabhat",
  "exp": 123456789
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

to enforce:

```
@PreAuthorize("hasRole('ADMIN')")
hasAnyRole("USER", "ADMIN")
```

So every request follows this chain:

```
JWT cookie
↓
Extract username (sub claim)
↓
loadUserByUsername(username)   ← re-fetch roles from DB
↓
validateToken()                ← username match + not expired
↓
SecurityContextHolder          ← roles now available for @PreAuthorize
```

---

### Phase 3: Logout Phase

###### New — required because HttpOnly cookies cannot be cleared by JavaScript.

Previously logout was handled entirely in the browser:

```javascript
localStorage.removeItem("jwtToken")   ← old
approach, removed
```

Because the cookie is `HttpOnly`, `document.cookie` cannot read or delete it.
The server must expire it by responding with `Max-Age=0`.

```
[CLIENT]
│
│ POST /auth/logout
│ credentials: "include"
▼
[AuthController.logout(HttpServletResponse)]
│
│ authService.logout(response)
▼
[AuthService.logout()]
│
└─► buildJwtCookie("", 0)
        │
        │ Same attributes as login cookie, but:
        │   value   = ""        (empty)
        │   Max-Age = 0         (browser deletes immediately)
        ▼
    Set-Cookie: jwt=; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=0
        │
        ▼
[Browser receives response → deletes jwt cookie]
        │
        ▼
[Next request carries no cookie → filter finds nothing → 401]
```

> `/auth/logout` is a public endpoint (no valid JWT required).
> The user's cookie may already be expired when they click Logout.
> Requiring a valid JWT to log out would mean an expired-session user
> could never reach a clean logged-out state.

---

### Think of it like this

**Login**

```
Passport Office
```

We prove who we are once. We receive:

```
Passport (JWT) — sealed inside an envelope the holder cannot open (HttpOnly cookie)
```

**Every future request**

```
Airport Security
```

We don't prove identity again with a birth certificate. The browser hands over:

```
The envelope (cookie) — security opens it, reads the passport (JWT)
```

Security checks:

```
Passport valid?
Expired?
Tampered with?
```

and lets us through.

**Logout**

```
Passport Surrender Desk
```

We can't burn the passport ourselves (HttpOnly — JS cannot touch it).
We hand the envelope to the desk, and the server:

```
Stamps it VOID (Max-Age=0) → browser shreds it
```

The next time we approach security, we have no envelope → turned away (401).