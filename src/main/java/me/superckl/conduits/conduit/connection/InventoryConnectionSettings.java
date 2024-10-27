package me.superckl.conduits.conduit.connection;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;
import java.util.function.ToIntFunction;

@NoArgsConstructor
@AllArgsConstructor
public class InventoryConnectionSettings {

    public static final Codec<InventoryConnectionSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("accept_priority").forGetter(InventoryConnectionSettings::getAcceptPriority),
                    Codec.INT.fieldOf("provide_priority").forGetter(InventoryConnectionSettings::getProvidePriority),
                    RedstoneMode.CODEC.fieldOf("accept_redstone_mode").forGetter(InventoryConnectionSettings::getAcceptRedstoneMode),
                    RedstoneMode.CODEC.fieldOf("provide_redstone_mode").forGetter(InventoryConnectionSettings::getProvideRedstoneMode),
                    DestinationMode.CODEC.fieldOf("destination_mode").forGetter(InventoryConnectionSettings::getDestinationMode))
            .apply(instance, InventoryConnectionSettings::new));

    @Getter
    protected int acceptPriority = 0;
    @Getter
    protected int providePriority = 0;
    @Getter
    protected RedstoneMode acceptRedstoneMode = RedstoneMode.IGNORED;
    @Getter
    protected RedstoneMode provideRedstoneMode = RedstoneMode.DISABLED;
    @Getter
    protected DestinationMode destinationMode = DestinationMode.NEAREST;

    private Consumer<Setting> updateListener = null;

    public InventoryConnectionSettings(final int acceptPriority, final int providePriority, final RedstoneMode acceptRedstoneMode,
                                       final RedstoneMode provideRedstoneMode, final DestinationMode destinationMode) {
        this.acceptPriority = acceptPriority;
        this.providePriority = providePriority;
        this.acceptRedstoneMode = acceptRedstoneMode;
        this.provideRedstoneMode = provideRedstoneMode;
        this.destinationMode = destinationMode;
    }

    public InventoryConnectionSettings setAcceptPriority(final int priority) {
        final int oldValue = this.acceptPriority;
        this.acceptPriority = priority;
        if (oldValue != this.acceptPriority && this.updateListener != null)
            this.updateListener.accept(Setting.ACCEPT_PRIORITY);
        return this;
    }

    public InventoryConnectionSettings setProvidePriority(final int priority) {
        final int oldValue = this.providePriority;
        this.providePriority = priority;
        if (oldValue != this.providePriority && this.updateListener != null)
            this.updateListener.accept(Setting.PROVIDE_PRIORITY);
        return this;
    }

    public InventoryConnectionSettings setAcceptRedstoneMode(final RedstoneMode redstoneMode) {
        final RedstoneMode oldValue = this.acceptRedstoneMode;
        this.acceptRedstoneMode = redstoneMode;
        if (oldValue != this.acceptRedstoneMode && this.updateListener != null)
            this.updateListener.accept(Setting.ACCEPT_REDSTONE_MODE);
        return this;
    }

    public InventoryConnectionSettings setProvideRedstoneMode(final RedstoneMode redstoneMode) {
        final RedstoneMode oldValue = this.provideRedstoneMode;
        this.provideRedstoneMode = redstoneMode;
        if (oldValue != this.provideRedstoneMode && this.updateListener != null)
            this.updateListener.accept(Setting.PROVIDE_REDSTONE_MODE);
        return this;
    }

    public InventoryConnectionSettings setDestinationMode(final DestinationMode destinationMode) {
        final DestinationMode oldValue = this.destinationMode;
        this.destinationMode = destinationMode;
        if (oldValue != this.destinationMode && this.updateListener != null)
            this.updateListener.accept(Setting.DESTINATION_MODE);
        return this;
    }

    public boolean isAccepting() {
        return this.acceptRedstoneMode != RedstoneMode.DISABLED;
    }

    public boolean isProviding() {
        return this.provideRedstoneMode != RedstoneMode.DISABLED;
    }

    public InventoryConnectionSettings copy(final Consumer<Setting> updateListener) {
        return new InventoryConnectionSettings(this.acceptPriority, this.providePriority, this.acceptRedstoneMode,
                this.provideRedstoneMode, this.destinationMode, updateListener);
    }

    @Getter
    @RequiredArgsConstructor
    public enum Setting {

        ACCEPT_PRIORITY(new DataSyncHelper(InventoryConnectionSettings::setAcceptPriority, InventoryConnectionSettings::getAcceptPriority)),
        PROVIDE_PRIORITY(new DataSyncHelper(InventoryConnectionSettings::setProvidePriority, InventoryConnectionSettings::getProvidePriority)),
        ACCEPT_REDSTONE_MODE(new DataSyncHelper((inv, v) -> inv.setAcceptRedstoneMode(RedstoneMode.values()[v]), inv -> inv.getAcceptRedstoneMode().ordinal())),
        PROVIDE_REDSTONE_MODE(new DataSyncHelper((inv, v) -> inv.setProvideRedstoneMode(RedstoneMode.values()[v]), inv -> inv.getProvideRedstoneMode().ordinal())),
        DESTINATION_MODE(new DataSyncHelper((inv, v) -> inv.setDestinationMode(DestinationMode.values()[v]), inv -> inv.getDestinationMode().ordinal()));

        private final DataSyncHelper syncHelper;

    }

    @RequiredArgsConstructor
    public static class DataSyncHelper {

        private final ObjIntConsumer<InventoryConnectionSettings> setter;
        private final ToIntFunction<InventoryConnectionSettings> getter;

        public void setFor(final InventoryConnectionSettings settings, final int value) {
            this.setter.accept(settings, value);
        }

        public int get(final InventoryConnectionSettings settings) {
            return this.getter.applyAsInt(settings);
        }

    }

}
