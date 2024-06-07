package io.github._4drian3d.unsignedvelocity.listener.event;

import com.google.inject.Inject;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.crypto.IdentifiedKey;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import io.github._4drian3d.unsignedvelocity.UnSignedVelocity;
import io.github._4drian3d.unsignedvelocity.configuration.Configuration;
import io.github._4drian3d.unsignedvelocity.listener.EventListener;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;

public class ConnectListener implements EventListener {
    //private static final MethodHandle KEY_SETTER;
    private static final Field playerKey;

    static {
        try {
            playerKey = ConnectedPlayer.class.getDeclaredField("playerKey");
            playerKey.setAccessible(true);
            removeFinal(playerKey);

            //final var lookup = MethodHandles.privateLookupIn(ConnectedPlayer.class, MethodHandles.lookup());
            //KEY_SETTER = lookup.findSetter(ConnectedPlayer.class, "playerKey", IdentifiedKey.class);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
    @Inject
    private EventManager eventManager;
    @Inject
    private UnSignedVelocity plugin;
    @Inject
    private Configuration configuration;

    @Subscribe
    void onJoin(PostLoginEvent event) throws Throwable {
        if (!configuration.removeSignedKey()) {
            return;
        }
        final Player player = event.getPlayer();
        if (player.getIdentifiedKey() != null) {
            playerKey.set(player, null);
            //KEY_SETTER.invoke(player, null);
        }
    }

    @Override
    public void register() {
        eventManager.register(plugin, this);
    }

    @Override
    public boolean canBeLoaded() {
        return true;
    }

    /**
     * FOR THE FOLLOWING CODE TO WORK, YOU MUST SUPPLY THESE ARGUMENTS THROUGH COMMAND LINE
     * --add-opens=java.base/java.lang.invoke=ALL-UNNAMED
     * --add-exports=java.base/java.lang.invoke=ALL-UNNAMED
     * --add-exports=java.base/jdk.internal.access=ALL-UNNAMED
     * --add-exports=java.base/sun.nio.ch=ALL-UNNAMED
     * --add-opens=java.base/java.lang=ALL-UNNAMED
     * --add-opens=java.base/java.lang.reflect=ALL-UNNAMED
     * --add-opens=java.base/java.io=ALL-UNNAMED
     * --add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED
     * @param field The field to make remove final from
     * @throws Throwable if the function fails to remove final modifier
      */
    public static void removeFinal(Field field) throws Throwable {
        Method[] classMethods = Class.class.getDeclaredMethods();
        Method declaredFieldMethod = Arrays.stream(classMethods).filter(x -> Objects.equals(x.getName(), "getDeclaredFields0")).findAny().orElseThrow();
        declaredFieldMethod.setAccessible(true);
        Field[] declaredFieldsOfField = (Field[]) declaredFieldMethod.invoke(Field.class, false);
        Field modifiersField = Arrays.stream(declaredFieldsOfField).filter(x -> Objects.equals(x.getName(), "modifiers")).findAny().orElseThrow();
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    }
}
