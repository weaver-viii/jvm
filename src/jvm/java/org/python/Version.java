// Copyright (c) 2004-2020 Adam Karpierz
// Licensed under CC BY-NC-ND 4.0
// Licensed under proprietary License
// Please refer to the accompanying LICENSE file.

package org.python;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Properties;
import java.util.Set;

/**
 * Python version information.
 *
 * The version number and build information are loaded from the
 * version.properties file, located in this class file's directory.
 * version.properties is generated by ant.
 */
public class Version
{
    /** The current version of Python. */
    public static String PY_VERSION;

    /** Tokenized version. */
    public static int PY_MAJOR_VERSION;
    public static int PY_MINOR_VERSION;
    public static int PY_MICRO_VERSION;
    public static int PY_RELEASE_LEVEL;
    public static int PY_RELEASE_SERIAL;

    /** Timestamp of the current build. */
    public static String DATE;
    public static String TIME;

    /**
     * Current hg branch (hg id -b),
     * Current hg tag    (hg id -t), e.g. 'tip',
     * Current hg global revision id (hg id -i).
     */
    public static String HG_BRANCH;
    public static String HG_TAG;
    public static String HG_VERSION;

    static
    {
        loadProperties();
    }

    /**
     * Return the current hg version number.
     * May be an empty string on environments that can't determine it.
     */
    public static String getHGVersion()
    {
        return HG_VERSION;
    }

    /**
     * Return the current hg identifier name, either the current branch or tag.
     */
    public static String getHGIdentifier()
    {
        return "".equals(HG_TAG) || "tip".equals(HG_TAG) ? HG_BRANCH : HG_TAG;
    }

    /**
     * Return the current build information, including revision and timestamp.
     */
    public static String getBuildInfo()
    {
        String revision  = getHGVersion();
        String separator = "".equals(revision) ? "" : ":";
        String hgIdent   = getHGIdentifier();
        return String.format("%s%s%s, %.20s, %.9s",
                             hgIdent, separator, revision, DATE, TIME);
    }

    /**
     * Describe the current Java VM.
     */
    public static String getVM()
    {
        return String.format("[%s (%s)]",
                             System.getProperty("java.vm.name"),
                             System.getProperty("java.vm.vendor"));
    }

    /**
     * Return the Python version, including compiler (or in our case, the Java VM).
     */
    public static String getVersion()
    {
        return String.format("%.80s (%.80s)\n%.80s",
                             PY_VERSION, getBuildInfo(), getVM());
    }

    private static void loadProperties()
    {
        /* Load the version information from the properties file. */

        boolean loaded = false;
        final String versionProperties = "/org/python/version.properties";
        InputStream in = Version.class.getResourceAsStream(versionProperties);
        if ( in != null )
        {
            try
            {
                Properties properties = new Properties();
                properties.load(in);
                loaded = true;

                PY_VERSION        = properties.getProperty("jvm.python.version");
                PY_MAJOR_VERSION  = Integer.parseInt(properties.getProperty("jvm.python.major_version"));
                PY_MINOR_VERSION  = Integer.parseInt(properties.getProperty("jvm.python.minor_version"));
                PY_MICRO_VERSION  = Integer.parseInt(properties.getProperty("jvm.python.micro_version"));
                PY_RELEASE_LEVEL  = Integer.parseInt(properties.getProperty("jvm.python.release_level"));
                PY_RELEASE_SERIAL = Integer.parseInt(properties.getProperty("jvm.python.release_serial"));

                DATE = properties.getProperty("jvm.python.build.date");
                TIME = properties.getProperty("jvm.python.build.time");

                HG_BRANCH  = properties.getProperty("jvm.python.build.hg_branch");
                HG_TAG     = properties.getProperty("jvm.python.build.hg_tag");
                HG_VERSION = properties.getProperty("jvm.python.build.hg_version");
            }
            catch ( IOException exc )
            {
                System.err.println("There was a problem loading " + versionProperties + ":");
                exc.printStackTrace();
            }
            finally
            {
                try
                {
                    in.close();
                }
                catch ( IOException exc )
                {
                    /* ok */
                }
            }
        }
        if ( ! loaded )
        {
            /* fail with a meaningful exception (cannot use Py exceptions here) */
            throw new RuntimeException("unable to load " + versionProperties);
        }
    }
}
