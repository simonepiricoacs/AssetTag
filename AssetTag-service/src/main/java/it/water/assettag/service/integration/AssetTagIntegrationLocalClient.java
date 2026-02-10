package it.water.assettag.service.integration;

import it.water.core.api.asset.AssetTagManager;
import it.water.core.api.model.AssetTagResource;
import it.water.core.api.service.integration.AssetTagIntegrationClient;
import it.water.core.interceptors.annotations.FrameworkComponent;
import it.water.core.interceptors.annotations.Inject;
import lombok.Setter;

/**
 * Local implementation of AssetTagIntegrationClient.
 * Used when AssetTag module is in the same container as the caller.
 * Delegates all operations to AssetTagManager (implemented by AssetTagSystemServiceImpl).
 *
 * This follows the same pattern as UserIntegrationLocalClient.
 */
@FrameworkComponent(priority = 1, services = AssetTagIntegrationClient.class)
public class AssetTagIntegrationLocalClient implements AssetTagIntegrationClient {

    @Inject
    @Setter
    private AssetTagManager assetTagManager;

    @Override
    public AssetTagResource findAssetTagResource(String resourceName, long resourceId, long tagId) {
        if (assetTagManager == null) return null;
        return assetTagManager.findAssetTagResource(resourceName, resourceId, tagId);
    }

    @Override
    public void addAssetTag(String resourceName, long resourceId, long tagId) {
        if (assetTagManager != null) {
            assetTagManager.addAssetTag(resourceName, resourceId, tagId);
        }
    }

    @Override
    public void addAssetTags(String resourceName, long resourceId, long[] tagsId) {
        if (assetTagManager != null) {
            assetTagManager.addAssetTags(resourceName, resourceId, tagsId);
        }
    }

    @Override
    public long[] findAssetTags(String resourceName, long resourceId) {
        if (assetTagManager == null) return new long[0];
        return assetTagManager.findAssetTags(resourceName, resourceId);
    }

    @Override
    public void removeAssetTag(String resourceName, long resourceId, long tagId) {
        if (assetTagManager != null) {
            assetTagManager.removeAssetTag(resourceName, resourceId, tagId);
        }
    }

    @Override
    public void removeAssetTags(String resourceName, long resourceId, long[] tagsId) {
        if (assetTagManager != null) {
            assetTagManager.removeAssetTags(resourceName, resourceId, tagsId);
        }
    }
}
