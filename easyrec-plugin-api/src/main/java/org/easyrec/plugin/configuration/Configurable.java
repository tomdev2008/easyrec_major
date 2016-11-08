package org.easyrec.plugin.configuration;

public interface Configurable<C extends Configuration> {

    /**
     * Returns the current configuration. Implementations must never return
     * null. If no implentation is currently set, a call to this method creates
     * a new configuration using default values, sets it as the
     * {@link Configurable}'s configuration and returns it.
     *
     *
     */
    public C getConfiguration();

    /**
     * Configures this <code>Configurable</code> instance.
     *
     * @param <C>
     * @param config
     */
    public void setConfiguration(C config);

    /**
     *
     */
    public C newConfiguration();

    /**
     *
     */
    public Class<C> getConfigurationClass();

}
