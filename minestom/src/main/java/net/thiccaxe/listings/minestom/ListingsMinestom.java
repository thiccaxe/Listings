/*
 * This file is part of Listings, licensed under the MIT License.
 *
 * Copyright (c) 2021 thiccaxe
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.thiccaxe.listings.minestom;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.extensions.Extension;
import net.minestom.server.ping.ResponseData;
import net.minestom.server.utils.identity.NamedAndIdentified;
import net.thiccaxe.listings.ListingsPlugin;
import net.thiccaxe.listings.config.Configuration;
import net.thiccaxe.listings.config.ServerType;
import net.thiccaxe.listings.minestom.config.MinestomConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListingsMinestom extends Extension implements ListingsPlugin<NamedAndIdentified, Player> {
    private File dataFolder;
    private MinestomConfiguration configuration;

    private long lastCacheUpdate = 0;

    private List<NamedAndIdentified> cache = Collections.emptyList();
    private ServerType lastType;

    @Override
    public void initialize() {
        dataFolder = getDataFolderInit();
        configuration = new MinestomConfiguration(dataFolder, this, ServerType.PROXY);
        configuration.setUpFile();
        lastType = configuration.getServerType();

        MinecraftServer.getGlobalEventHandler().addEventCallback(ServerListPingEvent.class, event -> {
            //if (!(event.getPingType() == ServerListPingType.MODERN_FULL_RGB || event.getPingType() == ServerListPingType.MODERN_NAMED_COLORS)) return;
            ResponseData responseData = event.getResponseData();
            responseData.clearEntries();
            if (!cached()) {
                setCache();
                cache = handlePing(event);
            }
            responseData.addEntries(cache);
        });

        MinecraftServer.getCommandManager().register(new ListingsCommand());

    }

    @Override
    public void terminate() {

    }

    @Override
    public void log(String message) {
        MinecraftServer.LOGGER.info(message);
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public boolean cached() {
        return (System.currentTimeMillis() - lastCacheUpdate < (configuration.getCacheTimeout()*1000));
    }

    @Override
    public void setCache() {
        lastCacheUpdate = System.currentTimeMillis();
    }

    @Override
    public NamedAndIdentified fromString(String name) {
        return NamedAndIdentified.named(name);
    }

    public String fullParse(@Nullable Player player, @NotNull String string) {
        return LegacyComponentSerializer.legacySection().serialize(
                MiniMessage.get().parse(string)
        );
    }


    private List<NamedAndIdentified> handlePing(ServerListPingEvent event) {
        return handleBackendServerPing(() -> new ArrayList<>(MinecraftServer.getConnectionManager().getOnlinePlayers()));
    }

    private File getDataFolderInit() {
        return new File(MinecraftServer.getExtensionManager().getExtensionFolder(), "Listings");
    }

    public class ListingsCommand extends Command {

        public ListingsCommand() {
            super("listings");

            setDefaultExecutor((sender, context) -> {
                try {
                    if (sender.hasPermission("listings.reload") || sender.isConsole()) {
                        configuration.reload();
                        sender.sendMessage(MiniMessage.get().parse(configuration.getReloadedMessage()));
                    } else {
                        sender.sendMessage(MiniMessage.get().parse(configuration.getErrorMessage()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    sender.sendMessage(MiniMessage.get().parse(configuration.getErrorMessage()));
                }
            });
        }
    }
}
