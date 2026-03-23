# AssetTag Module — Resource Tagging with Colored Labels

## Purpose
Provides a flexible tagging system for Water Framework resources. Users can create colored tags and associate them with any resource type via `WaterAssetTagResource`. Tags are `OwnedResource` (per-user, not shareable). Tag names are unique per owner. Does NOT handle hierarchical categorization — that is the `AssetCategory` module.

## Sub-modules

| Sub-module | Runtime | Key Classes |
|---|---|---|
| `AssetTag-api` | All | `AssetTagApi`, `AssetTagSystemApi`, `AssetTagRestApi`, `AssetTagRepository` |
| `AssetTag-model` | All | `AssetTag`, `WaterAssetTagResource`, `AssetTagResource` |
| `AssetTag-service` | Water/OSGi | Service impl, repository, REST controller |
| `AssetTag-service-spring` | Spring Boot | Spring MVC REST controllers |

## AssetTag Entity

```java
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"name", "ownerUserId"}))
@AccessControl(
    availableActions = {CrudActions.SAVE, CrudActions.UPDATE, CrudActions.FIND, CrudActions.FIND_ALL, CrudActions.REMOVE},
    rolesPermissions = {
        @DefaultRoleAccess(roleName = "AssetTagManager", actions = {CrudActions.class}),
        @DefaultRoleAccess(roleName = "AssetTagViewer",  actions = {CrudActions.FIND, CrudActions.FIND_ALL}),
        @DefaultRoleAccess(roleName = "AssetTagEditor",  actions = {CrudActions.SAVE, CrudActions.UPDATE, CrudActions.FIND, CrudActions.FIND_ALL})
    }
)
public class AssetTag extends AbstractJpaExpandableEntity implements ProtectedEntity, OwnedResource {

    @NotNull @NotEmpty @NoMalitiusCode
    @Column(length = 255)
    private String name;                        // Tag label (unique per owner)

    @Column(length = 255)
    private String description;                 // Optional description

    @Column(length = 7)
    @Size(min = 3, max = 7)
    private String color;                       // Hex color code (e.g. "#FF5733")

    @NonNull
    @JsonIgnore
    private Long ownerUserId;                   // Owner user ID

    @OneToMany(mappedBy = "tag", fetch = FetchType.EAGER)
    private Set<WaterAssetTagResource> resources; // Resources tagged with this tag
}
```

## WaterAssetTagResource Entity

Join table mapping arbitrary resources to tags:

```java
@Entity
public class WaterAssetTagResource extends AbstractJpaEntity
    implements ProtectedEntity, AssetTagResource {

    private String resourceName;    // Fully-qualified class name of the resource
    private long resourceId;        // Resource primary key

    @ManyToOne
    @JoinColumn(name = "tag_id")
    private AssetTag tag;           // Tag this resource is associated with
}
```

## Key Operations

### AssetTagApi (permission-checked)
```java
// Inherits from BaseEntityApi<AssetTag>:
AssetTag save(AssetTag entity);
AssetTag update(AssetTag entity);
AssetTag find(long id);
PaginableResult<AssetTag> findAll(int delta, int page, Query filter, QueryOrder order);
void remove(long id);
```

### AssetTagSystemApi (bypasses permissions)
Same CRUD methods, callable without a logged-in user context.
Used internally when other modules need tag resolution.

## Key Flow

```
Client
  └─► AssetTagRestControllerImpl (@FrameworkRestController)
       └─► AssetTagServiceImpl (@FrameworkComponent)
            └─► AssetTagSystemServiceImpl
                 └─► AssetTagRepository (JPA)
                      └─► AssetTag table
```

## REST Endpoints

| Method | Path | Permission | Description |
|---|---|---|---|
| `POST` | `/assettags` | AssetTagManager | Create tag |
| `PUT` | `/assettags` | AssetTagManager / AssetTagEditor | Update tag |
| `GET` | `/assettags/{id}` | AssetTagViewer | Find by ID |
| `GET` | `/assettags` | AssetTagViewer | Find all (paginated) |
| `DELETE` | `/assettags/{id}` | AssetTagManager | Delete tag |

All endpoints require `@LoggedIn`. Responses use `@JsonView(WaterJsonView.Public.class)`.

## Default Roles

| Role | Allowed Actions |
|---|---|
| `AssetTagManager` | SAVE, UPDATE, FIND, FIND_ALL, REMOVE |
| `AssetTagViewer` | FIND, FIND_ALL |
| `AssetTagEditor` | SAVE, UPDATE, FIND, FIND_ALL |

## Color Field Conventions
- `color` stores hex codes: short form (`#FFF`) or full form (`#FFFFFF`)
- Length constraint: `@Size(min = 3, max = 7)` — covers both formats without the `#`
- The `#` prefix may or may not be stored depending on client convention — validate at REST boundary

## Dependencies
- `it.water.repository.jpa:JpaRepository-api` — `AbstractJpaExpandableEntity`
- `it.water.core:Core-permission` — `@AccessControl`, `CrudActions`, `OwnedResource`
- `it.water.rest:Rest-api` — `RestApi`, `@LoggedIn`
- `jakarta.persistence:jakarta.persistence-api` — JPA 3.0 annotations

## Testing
- Unit tests: `WaterTestExtension` — test CRUD + ownership + permission scenarios
- REST tests: **Karate only** (never JUnit direct calls to `AssetTagRestController`)
- Tag names must be unique per `ownerUserId` — test duplicate name validation
- Impersonate users with `TestRuntimeInitializer.getInstance().impersonate(user, runtime)`

## Code Generation Rules
- Role names use `PascalCase` (`AssetTagManager`) unlike most other modules (snake_case) — match exactly
- `ownerUserId` is `@JsonIgnore` — never exposed in REST responses
- `resources` is `FetchType.EAGER` — keep associated resource sets small
- `OwnedResource` impl: `ownerUserId` set automatically from logged-in user context on save
- To associate a resource with a tag, create a `WaterAssetTagResource` instance — not via `AssetTagApi`
- REST controllers tested **exclusively via Karate**
