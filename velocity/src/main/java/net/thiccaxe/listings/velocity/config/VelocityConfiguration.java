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

package net.thiccaxe.listings.velocity.config;

import net.thiccaxe.listings.ListingsPlugin;
import net.thiccaxe.listings.config.Configuration;
import net.thiccaxe.listings.config.ServerType;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class VelocityConfiguration extends Configuration {

    private Set<String> servers = Collections.emptySet();

    public VelocityConfiguration(File dataFolder, ListingsPlugin plugin, ServerType serverType) {
        super(dataFolder, plugin, serverType);
    }

    @Override
    protected void updateOptions() {
        super.updateOptions();
        try {
            if (root != null) {
                ConfigurationNode config = root.node("configuration");
                servers = new HashSet<>(config.node("backends").getList(String.class, Collections.emptyList()));
                //System.out.println("servers: " + String.join(", ", servers));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Set<String> getServers() {
        return servers;
    }
}
