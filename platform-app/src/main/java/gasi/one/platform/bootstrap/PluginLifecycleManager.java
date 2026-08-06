package gasi.one.platform.bootstrap;

import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

/**
 * Stops and unloads PF4J plugins when the Spring application context shuts down.
 *
 * @since 1.0.0
 */
public class PluginLifecycleManager implements DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(PluginLifecycleManager.class);

    private final PluginManager pluginManager;

    /**
     * Creates a plugin lifecycle manager.
     *
     * @param pluginManager PF4J plugin manager
     */
    public PluginLifecycleManager(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    @Override
    public void destroy() {
        LOG.info("Stopping PF4J plugins.");
        pluginManager.stopPlugins();
        pluginManager.unloadPlugins();
        LOG.info("PF4J plugins stopped and unloaded.");
    }
}
