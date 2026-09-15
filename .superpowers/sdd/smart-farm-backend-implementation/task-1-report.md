# Task 1 Report: Project Skeleton and Domain Persistence

## Files Changed

- `backend/pom.xml`: Java 8 / Spring Boot 2.7.18 Maven project with Web, JPA, Security Crypto, H2, MySQL, and test dependencies.
- `backend/src/main/java/com/campus/farm/SmartFarmApplication.java`: application bootstrap.
- `backend/src/main/java/com/campus/farm/auth/User.java`: persisted user identity, BCrypt hash, and role.
- `backend/src/main/java/com/campus/farm/auth/UserRepository.java`: username lookup repository.
- `backend/src/main/java/com/campus/farm/device/Device.java`: persisted device code, name, type, and status.
- `backend/src/main/java/com/campus/farm/device/DeviceRepository.java`: device code and type query repository.
- `backend/src/main/java/com/campus/farm/config/DemoDataInitializer.java`: idempotent seed of `admin`, `operator`, a photovoltaic device, and an irrigation controller. Seed passwords are BCrypt-hashed before persistence.
- `backend/src/main/resources/application.yml`: H2 default configuration.
- `backend/src/main/resources/application-mysql.yml`: MySQL profile configuration driven by `MYSQL_URL`, `MYSQL_USERNAME`, and `MYSQL_PASSWORD`.
- `backend/src/test/java/com/campus/farm/ApplicationContextTest.java`: real Spring context smoke test.
- `backend/src/test/java/com/campus/farm/DomainPersistenceTest.java`: real H2/JPA repository tests for seed lookup and photovoltaic type query.

## RED Evidence

After adding only the build descriptor and test sources, the following command failed as expected:

```sh
/Users/yokna/.trae/tools/maven/latest/bin/mvn test
```

Maven exited with code 1 during `testCompile`. The expected failures were missing `com.campus.farm.auth` and `com.campus.farm.device` packages, including unresolved `UserRepository`, `DeviceRepository`, and `User` symbols. This established that the tests required the missing persistence contracts.

## GREEN Evidence

After the minimal application, entities, repositories, configuration, and seed initializer were added:

```sh
/Users/yokna/.trae/tools/maven/latest/bin/mvn test
```

Maven exited with code 0. Surefire reported:

```text
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

The suite started a real Spring Boot application context and used the configured H2 in-memory database.

## Package Evidence

```sh
/Users/yokna/.trae/tools/maven/latest/bin/mvn clean package
```

Maven exited with code 0, reran the same three tests with zero failures/errors, and produced:

```text
backend/target/smart-farm-backend-0.0.1-SNAPSHOT.jar
BUILD SUCCESS
```

## Concerns

- MySQL is selected by setting `SPRING_PROFILES_ACTIVE=mysql`; its URL and credentials are environment-driven and no credentials are logged or committed.
- The demo seed credentials are deliberately development defaults (`admin123` and `operator123`) and are stored only as BCrypt hashes. Production deployment should replace or disable these seed credentials before exposure.
- No Maven wrapper was added because Maven is intentionally supplied outside the repository at `/Users/yokna/.trae/tools/maven/latest/bin/mvn`.

## Review Fix Evidence

### Changes

- `backend/src/main/resources/application-mysql.yml` now uses the documented `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` environment variables.
- `backend/src/main/java/com/campus/farm/auth/User.java` marks `getPasswordHash()` with `@JsonIgnore`, preventing accidental Jackson JSON serialization while leaving JPA persistence unchanged.
- `backend/src/test/java/com/campus/farm/DomainPersistenceTest.java` now proves that the seeded `admin123` and `operator123` passwords match their stored BCrypt hashes and that a user JSON payload cannot contain a password hash.
- `backend/src/test/java/com/campus/farm/MysqlConfigurationTest.java` verifies that the MySQL profile resolves its datasource properties from the documented `DB_*` placeholders.

### RED

After adding the review tests and before changing production code/configuration, this command exited with code 1:

```sh
/Users/yokna/.trae/tools/maven/latest/bin/mvn test
```

Surefire reported `Tests run: 5, Failures: 2, Errors: 0, Skipped: 0`. The expected failures were:

```text
DomainPersistenceTest.passwordHashIsNeverSerialized
actual JSON: {"id":null,"username":"operator","passwordHash":"secret-hash","role":"OPERATOR"}

MysqlConfigurationTest.mysqlProfileUsesDocumentedDatabaseEnvironmentVariables
actual URL: ${MYSQL_URL:jdbc:mysql://localhost:3306/smart_farm?...}
expected prefix: ${DB_URL:
```

The BCrypt assertions for the real seeded admin and operator passwords passed during this RED run, confirming that the pre-existing seed initializer already produced valid BCrypt hashes.

### GREEN

After adding `@JsonIgnore` and renaming the MySQL placeholders, the same command was rerun:

```sh
/Users/yokna/.trae/tools/maven/latest/bin/mvn test
```

It exited with code 0 and reported:

```text
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Seed Identity Regression Evidence

`DomainPersistenceTest.seedCreatesDemoUsersAndDevices` was strengthened to use real `UserRepository` and `DeviceRepository` lookups rather than an arbitrary device count or type-only query. It now asserts:

- `admin` has role `ADMIN` and its stored BCrypt hash matches `admin123`.
- `operator` has role `OPERATOR` and its stored BCrypt hash matches `operator123`.
- `PV-001` has name `Solar Array`, type `PHOTOVOLTAIC`, and status `ONLINE`.
- `IRR-001` has name `Irrigation Controller`, type `IRRIGATION`, and status `ONLINE`.

### RED

The new test was mutation-checked by temporarily changing the seeded name for `PV-001` from `Solar Array` to `Incorrect Array`. The following command exited with code 1:

```sh
/Users/yokna/.trae/tools/maven/latest/bin/mvn -Dtest=DomainPersistenceTest#seedCreatesDemoUsersAndDevices test
```

Surefire reported:

```text
Tests run: 1, Failures: 1, Errors: 0, Skipped: 0
expected: "Solar Array"
 but was: "Incorrect Array"
```

### GREEN

After restoring the required `Solar Array` seed value, the full suite was run:

```sh
/Users/yokna/.trae/tools/maven/latest/bin/mvn test
```

It exited with code 0 and reported:

```text
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```
