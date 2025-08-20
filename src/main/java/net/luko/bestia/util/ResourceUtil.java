package net.luko.bestia.util;

import net.luko.bestia.Bestia;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Method;

public class ResourceUtil {
    public static ResourceLocation fromNamespaceAndPath(String namespace, String path){
        try {
            Method method = ResourceLocation.class.getMethod("fromNamespaceAndPath", String.class, String.class);
            return (ResourceLocation) method.invoke(null, namespace, path);
        } catch (Exception e){
            Bestia.LOGGER.debug("Method fromNamespaceAndPath not found, falling back");
            return new ResourceLocation(namespace, path);
        }
    }
}
