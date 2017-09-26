package com.sky.jenkins;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import org.apache.commons.lang.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;


public class JenkinsUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(JenkinsUtils.class);
    private final static String WORKSPACE = "WORKSPACE";
    private final static String RIOT_PROP_FILENAME = "matrix.properties";
    private final static String PREFIX_ENV = "env.";
    private final static String PREFIX_RIOT = "matrix.";

    /*
     * Jenkins Environment Variables : ${env.BUILD_PROJECT}, ${env.BUILD_NUMBER} ...
     * Matrix properties : ${Matrix.PROPERTY_KEY}
     */
    public static Properties getPropsFromEnvAndRiotPropFile(AbstractBuild build, BuildListener listener) {

        Properties props = new Properties();
        EnvVars envVars = null;
        try {
            envVars = build.getEnvironment(listener);
            for (Map.Entry entry : envVars.entrySet()) {
                LOGGER.info("[ENV]" + entry.getKey() + ":" + entry.getValue());
                props.put(PREFIX_ENV + entry.getKey(), entry.getValue().toString().trim());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        try {
            File propFile = new File(envVars.get(WORKSPACE) + File.separator + RIOT_PROP_FILENAME);
            if (propFile.exists()) {
                Properties tmpProps = new Properties();
                tmpProps.load(new FileInputStream(propFile));
                for (Map.Entry entry : tmpProps.entrySet()) {
                    LOGGER.info("[MATRIX]" + entry.getKey() + ":" + entry.getValue());
                    props.put(PREFIX_RIOT + entry.getKey(), entry.getValue().toString().replaceAll("\n|\r", "").trim());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return props;
    }

    public static String renderMessage(Properties props, String message) {
        StrSubstitutor sub = new StrSubstitutor(props);
        return sub.replace(message);
    }
}
