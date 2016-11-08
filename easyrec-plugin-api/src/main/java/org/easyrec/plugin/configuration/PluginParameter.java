package org.easyrec.plugin.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PluginParameter {

    /**
     * The name to be displayed in the UI
     */
    String displayName();

    /**
     * The short description of the parameter, should be < 50 chars.
     */
    String shortDescription();

    /**
     * The long description of the parameter. Any length is ok.
     */
    String description();

    /**
     * If set, determines the display ordering of the parameter in the admin interface.
     *
     */
    int displayOrder() default -1;
    /**
     *
     */
    boolean optional() default false;

    /**
     * If set to <code>true</code> the parameter is displayed as a text area instead
     * of the input field. Default value is <code>false</code>
     *
     * @return boolean value; <code>true</code> if text area should be used
     */
    boolean asTextArea() default false;

}
