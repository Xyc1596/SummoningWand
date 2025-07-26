package xyc.summoningwand;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.versions.forge.ForgeVersion;

import java.util.function.BiFunction;
import java.util.function.Function;

@SuppressWarnings("removal")
public class ResourceLocationUtils
{
    public static ResourceLocation fromNamespaceAndPath(String namespace, String path)
    {
        return GET2.apply(namespace, path);
    }

    public static ResourceLocation withDefaultNamespace(String path)
    {
        return GET.apply(path);
    }

    private final static boolean useNewMethods = Integer.parseInt(ForgeVersion.getVersion().split("\\.")[1]) > 3;
    private final static BiFunction<String, String, ResourceLocation> GET2 = useNewMethods
        ? ResourceLocation::fromNamespaceAndPath
        : ResourceLocation::new;
    private final static Function<String, ResourceLocation> GET = useNewMethods
        ? ResourceLocation::withDefaultNamespace
        : (path) -> new ResourceLocation("minecraft", path);
}
