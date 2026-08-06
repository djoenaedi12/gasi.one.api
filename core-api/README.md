# core-api

`core-api` contains public contracts shared by the platform, starters, generated
resources, and plugins.

This module should stay small and implementation-free. It defines what other
modules can depend on, but it does not decide how those contracts are executed.

## Scope

Use this module for:

- Shared interfaces and extension points.
- Public annotations used by plugins or generated code.
- Base DTOs, models, enums, and request/response contracts.
- Inbound and outbound ports used as architectural boundaries.

Do not put runtime implementation here, including services, controllers,
repositories, aspects, registries, framework adapters, parsers, or persistence
code. Those belong in `core-starter`, `platform-app`, or the owning plugin.

## Packages

```text
gasi.one.core.api
├── audit
├── common
├── plugin
└── resource
```

- `audit` contains audit contracts and annotations.
- `common` contains shared response, exception, ID, and query contracts.
- `plugin` contains plugin metadata and PF4J extension contracts.
- `resource` contains generated resource, hook, model, and port contracts.
