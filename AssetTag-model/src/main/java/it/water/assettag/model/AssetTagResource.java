package it.water.assettag.model;

import com.fasterxml.jackson.annotation.JsonView;
import it.water.core.api.permission.ProtectedEntity;
import it.water.core.api.service.rest.WaterJsonView;
import it.water.core.permission.action.CrudActions;
import it.water.core.permission.annotations.AccessControl;
import it.water.core.validation.annotations.NoMalitiusCode;
import it.water.core.validation.annotations.NotNullOnPersist;
import it.water.repository.jpa.model.AbstractJpaExpandableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;


/**
 * AssetTagResource Entity Class.
 * Maps the association between any resource (entity) and a tag.
 * This allows generic tagging of any entity in the system.
 */
//JPA
@Entity
@Table(name = "asset_tag_resource", uniqueConstraints = @UniqueConstraint(columnNames = {"resourceName", "resourceId", "tag_id"}))
@Access(AccessType.FIELD)
//Lombok
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter(AccessLevel.PROTECTED)
@ToString(exclude = {"tag"})
@EqualsAndHashCode(callSuper = true, of = {"resourceName", "resourceId"})
//Actions - internal entity, managed through AssetTag
@AccessControl(availableActions = {CrudActions.SAVE, CrudActions.UPDATE, CrudActions.FIND, CrudActions.FIND_ALL, CrudActions.REMOVE})
public class AssetTagResource extends AbstractJpaExpandableEntity implements ProtectedEntity, it.water.core.api.model.AssetTagResource {

    /**
     * Resource name - typically the entity class name
     */
    @JsonView({WaterJsonView.Extended.class})
    @NotNullOnPersist
    @NotEmpty
    @NoMalitiusCode
    @NotBlank
    @Setter
    private String resourceName;

    /**
     * Entity primary key
     */
    @JsonView({WaterJsonView.Extended.class})
    @Setter
    private long resourceId;

    /**
     * The associated tag
     */
    @ManyToOne(targetEntity = AssetTag.class)
    @JoinColumn(name = "tag_id")
    @Setter
    private AssetTag tag;

    /**
     * Constructor for creating a new AssetTagResource
     */
    public AssetTagResource(String resourceName, long resourceId, AssetTag tag) {
        this.resourceName = resourceName;
        this.resourceId = resourceId;
        this.tag = tag;
    }

    /**
     * @return the tag id (implements interface method)
     */
    @Override
    public long getTagId() {
        return tag != null ? tag.getId() : 0;
    }
}
