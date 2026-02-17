package it.water.assettag;

import it.water.core.api.asset.AssetTagManager;
import it.water.core.api.model.AssetTagResource;
import it.water.core.api.model.PaginableResult;
import it.water.core.api.bundle.Runtime;
import it.water.core.api.model.Role;
import it.water.core.api.role.RoleManager;
import it.water.core.api.user.UserManager;
import it.water.core.api.registry.ComponentRegistry;
import it.water.core.api.repository.query.Query;
import it.water.core.api.service.Service;
import it.water.core.api.permission.PermissionManager;
import it.water.core.testing.utils.bundle.TestRuntimeInitializer;
import it.water.core.interceptors.annotations.Inject;
import it.water.core.model.exceptions.ValidationException;
import it.water.core.model.exceptions.WaterRuntimeException;
import it.water.core.permission.exceptions.UnauthorizedException;
import it.water.core.testing.utils.runtime.TestRuntimeUtils;
import it.water.repository.entity.model.exceptions.DuplicateEntityException;
import it.water.repository.entity.model.exceptions.NoResultException;

import it.water.core.testing.utils.junit.WaterTestExtension;

import it.water.assettag.api.*;
import it.water.assettag.model.*;

import lombok.Setter;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Generated with Water Generator.
 * Test class for AssetTag Services.
 * 
 * Please use AssetTagRestTestApi for ensuring format of the json response
 

 */
@ExtendWith(WaterTestExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AssetTagApiTest implements Service {
    
    @Inject
    @Setter
    private ComponentRegistry componentRegistry;
    
    @Inject
    @Setter
    private AssetTagApi assettagApi;

    @Inject
    @Setter
    private Runtime runtime;

    @Inject
    @Setter
    private AssetTagRepository assettagRepository;
    
    @Inject
    @Setter
    //default permission manager in test environment;
    private PermissionManager permissionManager;

    @Inject
    @Setter
    //test role manager
    private UserManager userManager;
    
    @Inject
    @Setter
    //test role manager
    private RoleManager roleManager;

    @Inject
    @Setter
    //test role manager
    private AssetTagManager assetTagManager;

    //admin user
    private it.water.core.api.model.User adminUser;
    private it.water.core.api.model.User assettagManagerUser;
    private it.water.core.api.model.User assettagViewerUser;
    private it.water.core.api.model.User assettagEditorUser;

    private Role assettagManagerRole;
    private Role assettagViewerRole;
    private Role assettagEditorRole;
    
    @BeforeAll
    void beforeAll() {
        //getting user
        assettagManagerRole = roleManager.getRole(AssetTag.DEFAULT_MANAGER_ROLE);
        assettagViewerRole = roleManager.getRole(AssetTag.DEFAULT_VIEWER_ROLE);
        assettagEditorRole = roleManager.getRole(AssetTag.DEFAULT_EDITOR_ROLE);
        Assertions.assertNotNull(assettagManagerRole);
        Assertions.assertNotNull(assettagViewerRole);
        Assertions.assertNotNull(assettagEditorRole);
        //impersonate admin so we can test the happy path
        adminUser = userManager.findUser("admin");
        assettagManagerUser = userManager.addUser("manager", "name", "lastname", "manager@a.com","TempPassword1_","salt", false);
        assettagViewerUser = userManager.addUser("viewer", "name", "lastname", "viewer@a.com","TempPassword1_","salt", false);
        assettagEditorUser = userManager.addUser("editor", "name", "lastname", "editor@a.com","TempPassword1_","salt", false);
        //starting with admin permissions
        roleManager.addRole(assettagManagerUser.getId(), assettagManagerRole);
        roleManager.addRole(assettagViewerUser.getId(), assettagViewerRole);
        roleManager.addRole(assettagEditorUser.getId(), assettagEditorRole);
        //default security context is admin
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
    }
    /**
     * Testing basic injection of basic component for assettag entity.
     */
    @Test
    @Order(1)
    void componentsInsantiatedCorrectly() {
        this.assettagApi = this.componentRegistry.findComponent(AssetTagApi.class, null);
        Assertions.assertNotNull(this.assettagApi);
        Assertions.assertNotNull(this.componentRegistry.findComponent(AssetTagSystemApi.class, null));
        this.assettagRepository = this.componentRegistry.findComponent(AssetTagRepository.class, null);
        Assertions.assertNotNull(this.assettagRepository);
    }

    /**
     * Testing simple save and version increment
     */
    @Test
    @Order(2)
    void saveOk() {
        AssetTag entity = createAssetTag(0);
        entity = this.assettagApi.save(entity);
        Assertions.assertEquals(1, entity.getEntityVersion());
        Assertions.assertTrue(entity.getId() > 0);
        Assertions.assertEquals("Tag0", entity.getName());
    }

    /**
     * Testing update logic, basic test
     */
    @Test
    @Order(3)
    void updateShouldWork() {
        Query q = this.assettagRepository.getQueryBuilderInstance().createQueryFilter("name=Tag0");
        AssetTag entity = this.assettagApi.find(q);
        Assertions.assertNotNull(entity);
        entity.setName("TagUpdated");
        entity = this.assettagApi.update(entity);
        Assertions.assertEquals("TagUpdated", entity.getName());
        Assertions.assertEquals(2, entity.getEntityVersion());
    }

    /**
     * Testing update logic, basic test
     */
    @Test
    @Order(4)
    void updateShouldFailWithWrongVersion() {
        Query q = this.assettagRepository.getQueryBuilderInstance().createQueryFilter("name=TagUpdated");
        AssetTag errorEntity = this.assettagApi.find(q);
        Assertions.assertEquals("TagUpdated", errorEntity.getName());
        Assertions.assertEquals(2, errorEntity.getEntityVersion());
        errorEntity.setEntityVersion(1);
        Assertions.assertThrows(WaterRuntimeException.class, () -> this.assettagApi.update(errorEntity));
    }

    /**
     * Testing finding all entries with no pagination
     */
    @Test
    @Order(5)
    void findAllShouldWork() {
        PaginableResult<AssetTag> all = this.assettagApi.findAll(null, -1, -1, null);
        Assertions.assertEquals(1,all.getResults().size());
    }

    /**
     * Testing finding all entries with settings related to pagination.
     * Searching with 5 items per page starting from page 1.
     */
    @Test
    @Order(6)
    void findAllPaginatedShouldWork() {
        for (int i = 2; i < 11; i++) {
            AssetTag u = createAssetTag(i);
            this.assettagApi.save(u);
        }
        PaginableResult<AssetTag> paginated = this.assettagApi.findAll(null, 7, 1, null);
        Assertions.assertEquals(7, paginated.getResults().size());
        Assertions.assertEquals(1, paginated.getCurrentPage());
        Assertions.assertEquals(2, paginated.getNextPage());
        paginated = this.assettagApi.findAll(null, 7, 2, null);
        Assertions.assertEquals(3, paginated.getResults().size());
        Assertions.assertEquals(2, paginated.getCurrentPage());
        Assertions.assertEquals(1, paginated.getNextPage());
    }

    /**
     * Testing removing all entities using findAll method.
     */
    @Test
    @Order(7)
    void removeAllShouldWork() {
        PaginableResult<AssetTag> paginated = this.assettagApi.findAll(null, -1, -1, null);
        paginated.getResults().forEach(entity -> {
            this.assettagApi.remove(entity.getId());
        });
        Assertions.assertEquals(0,this.assettagApi.countAll(null));
    }

    /**
     * Testing failure on duplicated entity
     */
    @Test
    @Order(8)
    void saveShouldFailOnDuplicatedEntity() {
        AssetTag entity = createAssetTag(1);
        this.assettagApi.save(entity);
        AssetTag duplicated = this.createAssetTag(1);
        //cannot insert new entity which breaks unique constraint
        Assertions.assertThrows(DuplicateEntityException.class, () -> this.assettagApi.save(duplicated));
        AssetTag secondEntity = createAssetTag(2);
        this.assettagApi.save(secondEntity);
        entity.setName("Tag2");
        //cannot update an entity colliding with other entity on unique constraint
        Assertions.assertThrows(DuplicateEntityException.class, () -> this.assettagApi.update(entity));
    }

    /**
     * Testing failure on validation failure for example code injection
     */
    @Test
    @Order(9)
    void saveShouldFailOnValidationFailure() {
        AssetTag newEntity = new AssetTag("<script>function(){alert('ciao')!}</script>", 1L);
        Assertions.assertThrows(ValidationException.class, () -> this.assettagApi.save(newEntity));
    }

    /**
     * Testing Crud operations on manager role
     */
    @Order(10)
    @Test
    void managerCanDoEverything() {
        TestRuntimeInitializer.getInstance().impersonate(assettagManagerUser, runtime);
        final AssetTag entity = createAssetTag(101);
        AssetTag savedEntity = Assertions.assertDoesNotThrow(() -> this.assettagApi.save(entity));
        savedEntity.setName("ManagerUpdatedTag");
        Assertions.assertDoesNotThrow(() -> this.assettagApi.update(savedEntity));
        Assertions.assertDoesNotThrow(() -> this.assettagApi.find(savedEntity.getId()));
        Assertions.assertDoesNotThrow(() -> this.assettagApi.remove(savedEntity.getId()));
    }

    @Order(11)
    @Test
    void viewerCannotSaveOrUpdateOrRemove() {
        TestRuntimeInitializer.getInstance().impersonate(assettagViewerUser, runtime);
        final AssetTag entity = createAssetTag(201);
        Assertions.assertThrows(UnauthorizedException.class, () -> this.assettagApi.save(entity));
        //viewer can search
        Assertions.assertEquals(0, this.assettagApi.findAll(null, -1, -1, null).getResults().size());
    }

    @Order(12)
    @Test
    void editorCannotRemove() {
        TestRuntimeInitializer.getInstance().impersonate(assettagEditorUser, runtime);
        final AssetTag entity = createAssetTag(301);
        AssetTag savedEntity = Assertions.assertDoesNotThrow(() -> this.assettagApi.save(entity));
        savedEntity.setName("EditorUpdatedTag");
        Assertions.assertDoesNotThrow(() -> this.assettagApi.update(savedEntity));
        Assertions.assertDoesNotThrow(() -> this.assettagApi.find(savedEntity.getId()));
        long savedEntityId = savedEntity.getId();
        Assertions.assertThrows(UnauthorizedException.class, () -> this.assettagApi.remove(savedEntityId));
    }

    @Order(13)
    @Test
    void ownedResourceShouldBeAccessedOnlyByOwner() {
        TestRuntimeInitializer.getInstance().impersonate(assettagEditorUser, runtime);
        final AssetTag entity = createAssetTag(401);
        //saving as editor
        AssetTag savedEntity = Assertions.assertDoesNotThrow(() -> this.assettagApi.save(entity));
        Assertions.assertDoesNotThrow(() -> this.assettagApi.find(savedEntity.getId()));
        TestRuntimeInitializer.getInstance().impersonate(assettagManagerUser, runtime);
        //find an owned entity with different user from the creator should raise a NoResultException
        long savedEntityId = savedEntity.getId();
        Assertions.assertThrows(NoResultException.class, () -> this.assettagApi.find(savedEntityId));
    }

    @Order(14)
    @Test
    void tagSameName() {
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
        AssetTag entity = createAssetTag(402);
        entity.setName("tag1");
        entity= this.assettagApi.save(entity);
        AssetTag otherEntity = createAssetTag(403);
        otherEntity.setName("tag1");
        Assertions.assertThrows(Exception.class, () -> this.assettagApi.save(otherEntity));
    }



    @Order(15)
    @Test
    void tagManagerAddFindRemove() {
        AssetTag entity = createAssetTag(404);
        entity = this.assettagApi.save(entity);

        String resourceName = "it.water.model.tag";
        long resourceId = 2L;
        this.assetTagManager.addAssetTag(resourceName,resourceId,entity.getId());
        long[] tagIds = this.assetTagManager.findAssetTags(resourceName,resourceId);
        Assertions.assertEquals(1,tagIds.length);
        Assertions.assertEquals(entity.getId(),tagIds[0]);
        this.assetTagManager.removeAssetTag(resourceName,resourceId,entity.getId());
        tagIds = this.assetTagManager.findAssetTags(resourceName,resourceId);
        Assertions.assertEquals(0,tagIds.length);


    }

    @Order(16)
    @Test
    void findAssetTagResourceShouldReturnNullWhenTagDoesNotExist() {
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
        Assertions.assertThrows(NoResultException.class,
                () -> this.assetTagManager.findAssetTagResource("it.water.missing", 1L, -1L));
    }

    @Order(17)
    @Test
    void addAssetTagShouldNotDuplicateAssociation() {
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
        AssetTag entity = this.assettagApi.save(createAssetTag(405));
        String resourceName = "it.water.model.duplicate";
        long resourceId = 3L;
        this.assetTagManager.addAssetTag(resourceName, resourceId, entity.getId());
        this.assetTagManager.addAssetTag(resourceName, resourceId, entity.getId());
        long[] tagIds = this.assetTagManager.findAssetTags(resourceName, resourceId);
        Assertions.assertEquals(1, tagIds.length);
        AssetTagResource atr = this.assetTagManager.findAssetTagResource(resourceName, resourceId, entity.getId());
        Assertions.assertNotNull(atr);
    }

    @Order(18)
    @Test
    void addAndRemoveAssetTagsBatchShouldWork() {
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
        AssetTag first = this.assettagApi.save(createAssetTag(406));
        AssetTag second = this.assettagApi.save(createAssetTag(407));
        String resourceName = "it.water.model.batch";
        long resourceId = 10L;

        this.assetTagManager.addAssetTags(resourceName, resourceId, new long[]{first.getId(), second.getId()});
        long[] found = this.assetTagManager.findAssetTags(resourceName, resourceId);
        Assertions.assertEquals(2, found.length);

        this.assetTagManager.removeAssetTags(resourceName, resourceId, new long[]{first.getId(), second.getId()});
        long[] afterRemove = this.assetTagManager.findAssetTags(resourceName, resourceId);
        Assertions.assertEquals(0, afterRemove.length);
    }

    @Order(19)
    @Test
    void removeAssetTagShouldIgnoreDifferentResource() {
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
        AssetTag entity = this.assettagApi.save(createAssetTag(408));
        String resourceName = "it.water.model.remove";
        long resourceId = 20L;
        this.assetTagManager.addAssetTag(resourceName, resourceId, entity.getId());

        this.assetTagManager.removeAssetTag(resourceName, resourceId + 1, entity.getId());
        long[] stillFound = this.assetTagManager.findAssetTags(resourceName, resourceId);
        Assertions.assertEquals(1, stillFound.length);

        this.assetTagManager.removeAssetTag(resourceName, resourceId, entity.getId());
        long[] afterRemove = this.assetTagManager.findAssetTags(resourceName, resourceId);
        Assertions.assertEquals(0, afterRemove.length);
    }

    private AssetTag createAssetTag(int seed) {
        AssetTag entity = new AssetTag("Tag" + seed, (long) (seed + 1));
        return entity;
    }
}
