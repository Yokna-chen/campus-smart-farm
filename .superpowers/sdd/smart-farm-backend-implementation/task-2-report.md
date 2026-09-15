# Task 2: Authentication and Authorization Report

## Scope delivered

- `POST /api/auth/login` validates the seeded user with BCrypt and returns a server-issued, 256-bit opaque bearer token plus `{id, username, role}` only.
- `TokenStore` keeps issued tokens in process memory; `BearerTokenFilter` resolves valid `Authorization: Bearer <token>` headers to Spring Security authentication with the user's role.
- Security is stateless and denies every route by default. Only `/api/auth/login` is public. `/api/auth/me` requires authentication and `/api/auth/admin-test` requires `ADMIN`.
- Authentication failures are 401 and authorization failures are 403. No passwords or password hashes are returned by login DTOs.

## Baseline evidence

Command: `/Users/yokna/.trae/tools/maven/latest/bin/mvn -q test` from `backend/`.

Result: exit 0 before Task 2 changes; the existing three tests passed.

## RED evidence

Added `AuthenticationIntegrationTest` before production authentication code. It covers successful safe login, invalid credentials, missing bearer, malformed bearer, and operator/admin enforcement.

Command: `/Users/yokna/.trae/tools/maven/latest/bin/mvn -q -Dtest=AuthenticationIntegrationTest test` from `backend/`.

Result: exit 1; 3 failures. The default Spring Security chain returned 403 for login because CSRF blocked the unimplemented route, where the new contract expects 200 for valid credentials and 401 for invalid credentials. The role test also failed while obtaining its login token. Missing and malformed bearer requests returned 401 under the existing default security chain, but no application bearer-token flow or protected endpoint existed at that point.

## GREEN evidence

After adding the auth service, opaque token store, bearer filter, controller/DTOs, and explicit stateless security configuration:

Command: `/Users/yokna/.trae/tools/maven/latest/bin/mvn -q -Dtest=AuthenticationIntegrationTest test` from `backend/`.

Result: exit 0; 5 tests passed.

## Full suite evidence

Command: `/Users/yokna/.trae/tools/maven/latest/bin/mvn -q test` from `backend/`.

Result: exit 0; 9 tests passed, 0 failures, 0 errors, 0 skipped.

## Concern

Tokens are intentionally in-memory per the architecture. They are invalidated by an application restart and have no explicit logout or expiry mechanism in this MVP.
