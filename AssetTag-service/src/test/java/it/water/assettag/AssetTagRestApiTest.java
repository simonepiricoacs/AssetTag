
package it.water.assettag;

import it.water.core.api.service.Service;
import it.water.core.api.registry.ComponentRegistry;
import it.water.assettag.api.AssetTagApi;
import com.intuit.karate.junit5.Karate;
import it.water.core.interceptors.annotations.Inject;
import it.water.core.testing.utils.junit.WaterTestExtension;
import it.water.core.testing.utils.bundle.TestRuntimeInitializer;
import it.water.core.testing.utils.runtime.TestRuntimeUtils;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;

@ExtendWith(WaterTestExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AssetTagRestApiTest implements Service {

    @Inject
    @Setter
    private ComponentRegistry componentRegistry;

    @Inject
    @Setter
    private AssetTagApi assetTagApi;

    @BeforeAll
    void cleanupBeforeTests() {
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
        // Clean up any existing entities before running REST tests
        assetTagApi.findAll(null, -1, -1, null).getResults().forEach(entity -> {
            try {
                assetTagApi.remove(entity.getId());
            } catch (Exception e) {
                // Ignore errors during cleanup
            }
        });
    }

    @BeforeEach
    void impersonateAdmin() {
        //jwt token service is disabled, we just inject admin user for bypassing permission system
        //just remove this line if you want test with permission system working
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
    }

    @Karate.Test
    Karate restInterfaceTest() {
        return Karate.run("classpath:karate")
                .systemProperty("webServerPort", TestRuntimeInitializer.getInstance().getRestServerPort())
                .systemProperty("host", "localhost")
                .systemProperty("protocol", "http");
    }
}
