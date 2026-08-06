# platform-app

`platform-app` is the executable Spring Boot host for GASI API. It owns runtime
bootstrap only: HTTP startup, PF4J plugin loading, plugin component scanning,
combined migrations, i18n wiring, and platform-level diagnostics.

Business features should live in plugins, not in this module.

## Scope

Use this module for:

- Spring Boot application startup.
- PF4J plugin load, start, stop, and unload lifecycle.
- Platform, core-starter, and plugin component scan setup.
- Plugin metadata lookup.
- Platform and plugin Flyway migration wiring.
- Platform and plugin message bundle wiring.
- Platform-level endpoints such as health/status.

Do not put feature-specific resource logic, workflow rules, or plugin business
services here.

## Packages

```text
gasi.one.platform
├── bootstrap
├── infrastructure
│   ├── classloader
│   └── config
└── presentation
    └── controller
```

- `bootstrap` contains PF4J startup, plugin metadata, component scanning, and lifecycle management.
- `infrastructure` contains platform runtime configuration and classloader support.
- `presentation` contains platform-level HTTP endpoints.

## Runtime

Plugins are loaded before Spring Boot starts:

```text
loadPlugins()
startPlugins()
build composite classloader
start Spring Boot
register PluginManager
```

On shutdown, the platform stops and unloads plugins.

## Configuration

Important defaults:

```properties
app.plugins.path=${APP_PLUGINS_PATH:plugins}
spring.flyway.enabled=false
spring.flyway.table=schema_histories
spring.main.allow-bean-definition-overriding=false
```

The plugin directory can be configured with:

```bash
APP_PLUGINS_PATH=/path/to/plugins
```

or:

```bash
-Dapp.plugins.path=/path/to/plugins
```

## Plugin Conventions

Generated plugins should live under:

```text
gasi.one.plugins.{pluginCode}
```

Plugin Spring packages are declared through `AppExtension#getBasePackages()`.
Plugin migrations are declared through `FlywayMigrationExtension`.
Plugin message bundles are declared through `I18nExtension`.

## Verify

From the repository root:

```bash
mvn test -pl platform-app -am
```
