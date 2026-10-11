package com.example.tpaextension;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;

/** Standalone regression probe: optional dependency packages are deliberately unavailable. */
public final class WithoutFloodgateProbe {
    public static void main(String[] args) throws Exception {
        var urls = new ArrayList<URL>();
        urls.add(Path.of(args.length > 1 ? args[1] : "target/classes").toUri().toURL());
        for (String entry : args[0]
                .split(java.io.File.pathSeparator)) {
            urls.add(Path.of(entry).toUri().toURL());
        }
        try (var loader = new URLClassLoader(urls.toArray(URL[]::new), ClassLoader.getPlatformClassLoader()) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.startsWith("org.geysermc.")) {
                    throw new ClassNotFoundException("Deliberately absent: " + name);
                }
                return super.loadClass(name, resolve);
            }
        }) {
            Class<?> plugin = Class.forName("com.example.tpaextension.TPAExtension", true, loader);
            plugin.getDeclaredMethods();
            plugin.getDeclaredConstructors();
            for (String name : new String[]{"TPAUiService", "BedrockTPACommand", "JavaTPAGui",
                    "TPAListener", "TPARequestManager"}) {
                Class<?> type = Class.forName("com.example.tpaextension." + name, true, loader);
                type.getDeclaredMethods();
                Object instance = type.getConstructor(plugin).newInstance(new Object[]{null});
                if (name.equals("TPAUiService")) {
                    if (!Boolean.FALSE.equals(type.getMethod("isBedrockPlayer", UUID.class)
                            .invoke(instance, UUID.randomUUID()))) {
                        throw new AssertionError("Java fallback must not classify players as Floodgate players");
                    }
                    type.getMethod("closeCancelForm", UUID.class, UUID.class)
                            .invoke(instance, UUID.randomUUID(), UUID.randomUUID());
                }
                System.out.println("PASS without Floodgate: " + name);
            }
            try {
                loader.loadClass("org.geysermc.floodgate.api.FloodgateApi");
                throw new AssertionError("Floodgate unexpectedly available");
            } catch (ClassNotFoundException expected) {
                System.out.println("PASS: Floodgate API deliberately unavailable");
            }
        }
    }
}
