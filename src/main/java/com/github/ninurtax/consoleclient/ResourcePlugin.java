package com.github.ninurtax.consoleclient;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public abstract class ResourcePlugin {

    public void saveResource(@NotNull String resourcePath, boolean replace) {
        if (!resourcePath.equals("")) {
            resourcePath = resourcePath.replace('\\', '/');
            InputStream in = this.getResource(resourcePath);
            if (in == null) {
                throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in ");
            } else {
                File outFile = new File(resourcePath);
                int lastIndex = resourcePath.lastIndexOf(47);
                File outDir = new File(resourcePath.substring(0, Math.max(lastIndex, 0)));
                if (!outDir.exists()) {
                    boolean mkdirs = outDir.mkdirs();
                }

                try {
                    if (!(outFile.exists() && !replace)) {

                        OutputStream out = new FileOutputStream(outFile);
                        byte[] buf = new byte[1024];

                        int len;
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }

                        out.close();
                        in.close();
                    }
                } catch (IOException ignored) {
                }

            }
        } else {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }
    }

    @Nullable
    public InputStream getResource(@NotNull String filename) {
        try {
            URL url = this.getClassLoader().getResource(filename);
            if (url == null) {
                return null;
            } else {
                URLConnection connection = url.openConnection();
                connection.setUseCaches(false);
                return connection.getInputStream();
            }
        } catch (IOException var4) {
            return null;
        }
    }

    @NotNull
    protected final ClassLoader getClassLoader() {
        return this.getClass().getClassLoader();
    }


}
