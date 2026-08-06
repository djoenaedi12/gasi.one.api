# gasi.one.api

Modular backend foundation for GASI. The project is built around a small core,
reusable Spring/JPA starter code, and a Spring Boot platform host that loads
business features as plugins.

## Architecture Diagram

```text
       ┌──────────────┐
       │ database     │
       └──────▲───────┘
              │ JPA/Flyway
       ┌──────┴───────┐   loads    ┌──────────────┐
       │ platform-app │ ─────────▶ │ plugin jars  │
       │ Spring Boot  │            │ features     │
       └──────┬───────┘            └──────┬───────┘
              │ uses                      │ uses
              ▼                           ▼
       ┌──────────────┐  implements ┌──────────────┐
       │ core-starter │ ─────────▶ │ core-api     │
       │ reusable impl│            │ contracts    │
       └──────────────┘            └──────────────┘
```

## Modules

```text
gasi.one.api
├── core-api
├── core-starter
└── platform-app
```

- `core-api`: shared contracts and extension points.
- `core-starter`: reusable implementation for generated resources and plugins.
- `platform-app`: executable Spring Boot host and plugin runtime.

Business features such as auth, audit, storage, upload, or domain resources
should live in plugins, not in `platform-app`.

## Tech Stack

- Java 25
- Spring Boot 4
- Spring Web
- Spring Data JPA
- Spring Security method authorization
- Hibernate
- Flyway
- PF4J and PF4J Spring
- MapStruct
- Lombok
- Sqids
- Maven
- JUnit 5

## Compile

Compile all modules:

```bash
mvn clean compile
```

Run tests:

```bash
mvn test
```

Run only platform tests with required modules:

```bash
mvn test -pl platform-app -am
```

Install core modules locally for generated plugin builds:

```bash
mvn -DskipTests install -pl core-api,core-starter -am
```

## Run Platform

By default, `platform-app` loads plugin jars from:

```text
plugins
```

Run with default plugin path:

```bash
mvn -pl platform-app spring-boot:run
```

Run with a custom plugin path:

```bash
mvn -pl platform-app spring-boot:run \
  -Dspring-boot.run.jvmArguments="-Dapp.plugins.path=/path/to/plugins"
```

Or use environment variable:

```bash
APP_PLUGINS_PATH=/path/to/plugins mvn -pl platform-app spring-boot:run
```

Make sure datasource settings match your local database before starting the
platform.

## Plugin Build And Run

Generated plugins should use this package convention:

```text
gasi.one.plugins.{pluginCode}
```

Typical plugin flow:

```text
generate plugin code
        ↓
generate resource code
        ↓
build plugin jar
        ↓
copy jar to platform plugin directory
        ↓
run platform-app
```

Build a generated plugin:

```bash
mvn -DskipTests package
```

Copy the plugin jar into the platform plugin directory:

```bash
cp target/{plugin-artifact}-1.0.0.jar /path/to/gasi.one.api/plugins/
```

Then run `platform-app`. During startup, the platform will:

- Load the plugin jar with PF4J.
- Register plugin Spring packages from `AppExtension`.
- Register plugin migrations from `FlywayMigrationExtension`.
- Register plugin messages from `I18nExtension`.

## More Details

- `core-api/README.md`
- `core-starter/README.md`
- `platform-app/README.md`
