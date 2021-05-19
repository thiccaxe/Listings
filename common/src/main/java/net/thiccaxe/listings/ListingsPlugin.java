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

package net.thiccaxe.listings;

import net.thiccaxe.listings.config.Configuration;
import net.thiccaxe.listings.config.ServerType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public interface ListingsPlugin<T, P> {

    default void log(String message) {
        System.out.println("[Listings] " +  message);
    }

    boolean cached();
    void setCache();



    String fullParse(P player, String s);

    T fromString(String name);

    // Ping

    default List<String> handleNoFormatServerPing(List<P> players) {
        Configuration configuration = getConfiguration();
        BackendInfo info = new BackendInfo();
        info.setHeader(fullParse(null, configuration.getHeader()));
        info.setFooter(fullParse(null, configuration.getFooter()));
        info.setExtra(fullParse(null, configuration.getExtra()));
        configuration.getInfo().forEach(i -> info.addInfo(fullParse(null, i)));
        players.forEach(i -> info.addPlayer(fullParse(i, configuration.getFormat())));
        return new ArrayList<>(info.format());
    }

    default List<String> handleFormatServerPing(List<P> players) {
        Configuration configuration = getConfiguration();
        String header = configuration.getHeader();
        if (header != null) {
            header = header.replace("{ONLINE}", String.valueOf(players.size()));
        }
        String footer = configuration.getHeader();
        if (footer != null) {
            footer = footer.replace("{ONLINE}", String.valueOf(players.size()));
        }
        String extra = configuration.getExtra();
        return new ArrayList<>(Formatter.format(players.stream().map(
                player -> fullParse(player, configuration.getFormat())
                ).collect(Collectors.toList()),
                configuration.getInfo().stream().map(
                        info -> fullParse(null, info)
                ).collect(Collectors.toList()),
                fullParse(null, header),
                fullParse(null, footer),
                extra != null ? fullParse(null, extra) : null,
                configuration.getMaximumColumns(),
                configuration.getMaximumRows(),
                configuration.getJustifyType()
        ));
    }

    default List<String> handleFormatBackendPing() {
        Configuration configuration = getConfiguration();
        List<BackendInfo> backends = pingBackends();

        List<List<String>> columns = new ArrayList<>();

        backends.forEach(backend -> {
            columns.add(Formatter.format(
                    backend.getPlayers(),
                    backend.getInfo(),
                    backend.getHeader(),
                    backend.getFooter(),
                    backend.getExtra(),
                    configuration.getMaximumColumns(),
                    configuration.getMaximumRows(),
                    configuration.getJustifyType()
            ));
        });


        List<String> backendHover = Formatter.joinColumns(columns);

        return new ArrayList<>(Formatter.format(
                new ArrayList<>(backendHover),
                configuration.getInfo(),
                configuration.getHeader(),
                configuration.getFooter(),
                configuration.getExtra(),
                configuration.getMaximumColumns(),
                configuration.getMaximumRows(),
                configuration.getJustifyType()
        ));
    }

    default List<BackendInfo> pingBackends() {
        return Collections.emptyList();
    }

    default List<T> handleBackendServerPing(Supplier<List<P>> playerSupplier) {
        final Configuration configuration = getConfiguration();
        List<String> response;
        List<P> players = playerSupplier.get();
        if (configuration.getServerType() == ServerType.SERVER) {
            response = handleNoFormatServerPing(players);
        } else {
            response = handleFormatServerPing(players);
        }
        return response.stream().map(this::fromString).collect(Collectors.toList());
    }

    default List<T> handleProxyServerPing(Supplier<List<P>> playerSupplier) {
        final Configuration configuration = getConfiguration();
        List<String> response;
        List<P> players = playerSupplier.get();
        if (configuration.getServerType() == ServerType.SERVER) {
            response = handleNoFormatServerPing(players);
        } else {
            response = handleFormatBackendPing();
        }
        return response.stream().map(this::fromString).collect(Collectors.toList());
    }

    Configuration getConfiguration();


    class BackendInfo {
        private @Nullable String header;
        private @Nullable String footer;
        private @Nullable String extra;
        private @NotNull
        final List<String> players = new ArrayList<>();
        private @NotNull
        final List<String> infos = new ArrayList<>();

        public @Nullable String getHeader() {
            return header;
        }

        public void setHeader(@Nullable String header) {
            this.header = header;
        }

        public @Nullable String getFooter() {
            return footer;
        }

        public void setFooter(@Nullable String footer) {
            this.footer = footer;
        }

        public @Nullable String getExtra() {
            return extra;
        }

        public void setExtra(@Nullable String extra) {
            this.extra = extra;
        }

        public @NotNull List<String> getPlayers() {
            return players;
        }

        public void addPlayer(@NotNull String player) {
            players.add(player);
        }

        public @NotNull List<String> getInfo() {
            return infos;
        }

        public void addInfo(@NotNull String info) {
            infos.add(info);
        }

        public List<String> format() {
            ArrayList<String> lines = new ArrayList<>();
            addField(lines, "[HEADER]", header);
            addField(lines, "[FOOTER]", footer);
            addField(lines, "[EXTRA]", extra);
            addFields(lines, "[INFO]", infos);
            addFields(lines, "[PLAYERS}]", players);
            return lines;
        }

        private static void addField(List<String> list, String name, String val) {
            if (val != null) {
                list.add(name);
                list.add(val);
            }
        }
        private static void addFields(List<String> list, String name, List<String> vals) {
            if (!vals.isEmpty()) {
                list.add(name);
                list.add(String.valueOf(vals.size()));
                list.addAll(vals);
            }
        }

        public static BackendInfo getData(List<String> sample) {
            //"[HEADER]", "[FOOTER]", "[EXTRA]", "[INFO]", "[PLAYERS]");
            String current = "";
            int length = 0;
            int consumed = 0;
            BackendInfo backend = new BackendInfo();
            for (String line : sample) {
                //System.out.println("            " + line);


                if (consumed == length) {
                    //System.out.println("Getting new Header...");
                    if (line.equalsIgnoreCase("[HEADER]")) {
                        current = "[HEADER]";
                        length = 1;
                    } else if (line.equalsIgnoreCase("[FOOTER]")) {
                        current = "[FOOTER]";
                        length = 1;
                    } else if (line.equalsIgnoreCase("[EXTRA]")) {
                        current = "[EXTRA]";
                        length = 1;
                    } else if (line.equalsIgnoreCase("[INFO]")) {
                        current = "[INFO]";
                        length = Integer.MAX_VALUE;
                    } else if (line.equalsIgnoreCase("[PLAYERS]")) {
                        current = "[PLAYERS]";
                        length = Integer.MAX_VALUE;
                    }
                    //System.out.println("Current: " + current);
                    consumed = 0;
                } else if (consumed == 0 && length == Integer.MAX_VALUE) {
                    //System.out.println("Getting length value for " + current + "...");
                    try {
                        length = Integer.parseInt(line) + 1;
                        consumed++;
                    } catch (NumberFormatException ignored) {
                        current = "";
                        consumed = length;
                    }
                } else if (consumed < length) {
                    consumed ++;
                    if (current.equalsIgnoreCase("[HEADER]")) {
                        backend.setHeader(line);
                    } else if (current.equalsIgnoreCase("[FOOTER]")) {
                        backend.setFooter(line);
                    } else if (current.equalsIgnoreCase("[EXTRA]")) {
                        backend.setExtra(line);
                    } else if (current.equalsIgnoreCase("[INFO]")) {
                        backend.addInfo(line);
                    } else if (current.equalsIgnoreCase("[PLAYERS]")) {
                        backend.addPlayer(line);
                    }
                }

            }
            return backend;
        }
    }
}

