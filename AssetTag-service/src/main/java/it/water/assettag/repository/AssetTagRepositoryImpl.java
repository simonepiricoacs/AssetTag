package it.water.assettag.repository;

import it.water.assettag.api.AssetTagRepository;
import it.water.assettag.model.AssetTag;
import it.water.core.interceptors.annotations.FrameworkComponent;
import it.water.repository.jpa.WaterJpaRepositoryImpl;

/**
 * Repository Class for AssetTag entity.
 * Only handles CRUD operations. Manager logic is in AssetTagSystemServiceImpl.
 */
@FrameworkComponent
public class AssetTagRepositoryImpl extends WaterJpaRepositoryImpl<AssetTag>
        implements AssetTagRepository {

    private static final String ASSETTAG_PERSISTENCE_UNIT = "assetTag-persistence-unit";

    public AssetTagRepositoryImpl() {
        super(AssetTag.class, ASSETTAG_PERSISTENCE_UNIT);
    }
}
