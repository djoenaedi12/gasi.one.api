package gasi.one.platform.bootstrap;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.pf4j.PluginManager;

/**
 * Unit tests for {@link PluginLifecycleManager}.
 */
class PluginLifecycleManagerTest {

    @Test
    @DisplayName("destroy should stop and unload plugins")
    void destroyShouldStopAndUnloadPlugins() {
        PluginManager pluginManager = mock(PluginManager.class);
        PluginLifecycleManager lifecycleManager = new PluginLifecycleManager(pluginManager);

        lifecycleManager.destroy();

        InOrder order = inOrder(pluginManager);
        order.verify(pluginManager).stopPlugins();
        order.verify(pluginManager).unloadPlugins();
    }
}
