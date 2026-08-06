# core-starter

`core-starter` provides reusable Spring/JPA implementation support for contracts
defined in `core-api`.

This module contains generic starter code only. Feature-specific workflows should
live in the owning plugin.

## Scope

Use this module for:

- Generic CRUD service and controller base classes.
- Resource hook registries.
- MapStruct base mapper contracts and shared mapper annotations.
- Generic JPA adapter, entity, filter, and specification support.
- Infrastructure helpers that are reusable across generated resources.

Do not put plugin-specific business flows here, including approval, custom field,
file upload, storage provider, or audit log implementations.

## Packages

```text
gasi.one.core.starter
├── application
├── infrastructure
└── presentation
```

- `application` contains reusable application-layer services, mappers, hooks, and support classes.
- `infrastructure` contains reusable JPA, crypto, ID, i18n, mapper, filter, and specification support.
- `presentation` contains reusable REST controller, response projection, and exception handling support.
