/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service;

import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;

public class MarkdownService {
    private static final Logger log = getLogger(MarkdownService.class);
    private final String botId;
    private final Map<String, String> htmlCache = new ConcurrentHashMap<>();
    private final Path assetsDir = Path.of("assets");
    private final Parser parser;
    private final HtmlRenderer renderer;

    public MarkdownService(String botId) {
        this.botId = botId;
        var extensions = List.of(TablesExtension.create(), HeadingAnchorExtension.create(), AutolinkExtension.create());
        this.parser = Parser.builder().extensions(extensions).build();
        this.renderer = HtmlRenderer.builder().extensions(extensions).build();
        loadAssets();
    }

    private void loadAssets() {
        if (!Files.exists(assetsDir)) {
            log.warn("Assets directory does not exist: {}", assetsDir.toAbsolutePath());
            return;
        }

        try (Stream<Path> stream = Files.walk(assetsDir)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".md"))
                    .forEach(this::parseAndCache);
        } catch (IOException e) {
            log.error("Failed to load assets from {}", assetsDir, e);
        }
    }

    private void parseAndCache(Path path) {
        try {
            String content = Files.readString(path).replace("{{ bot_id }}", botId);
            Node document = parser.parse(content);
            String html = renderer.render(document);
            String relativePath = assetsDir.relativize(path).toString().replace("\\", "/");
            // Remove .md extension
            if (relativePath.endsWith(".md")) {
                relativePath = relativePath.substring(0, relativePath.length() - 3);
            }
            htmlCache.put(relativePath, html);
            log.info("Parsed and cached markdown: {}", relativePath);
        } catch (IOException e) {
            log.error("Failed to parse markdown file: {}", path, e);
        }
    }

    public String getHtml(String path) {
        return htmlCache.get(path);
    }

    public Map<String, String> getDirectoryHtml(String dirPath) {
        String prefix = dirPath.isEmpty() ? "" : dirPath + "/";
        Map<String, String> result = new HashMap<>();
        for (var entry : htmlCache.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                String subPath = entry.getKey().substring(prefix.length());
                if (!subPath.contains("/")) {
                    result.put(subPath, entry.getValue());
                }
            }
        }
        return result.isEmpty() ? Collections.emptyMap() : result;
    }
}
