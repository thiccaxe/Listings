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


import net.thiccaxe.listings.text.DefaultFont;

import java.util.*;
import java.util.stream.Collectors;

public class Formatter {
    public static final String SECTION = String.valueOf((char) 0x00a7);
    public static final String OBSCURED = SECTION + "k";
    public static final String BOLD = SECTION + "l";
    public static final String STRIKETHROUGH = SECTION + "m";
    public static final String UNDERLINE = SECTION + "n";
    public static final String ITALIC = SECTION + "o";
    public static final String RESET = SECTION + "r";
    public static final String BLACK = SECTION + "0";
    public static final String DARK_GRAY = SECTION + "8";
    public static final String GRAY = SECTION + "7";
    public static final String WHITE = SECTION + "f";
    public static final String YELLOW = SECTION + "e";
    public static final String GOLD = SECTION + "6";
    public static final String RED = SECTION + "c";
    public static final String DARK_RED = SECTION + "4";
    public static final String GREEN = SECTION + "a";
    public static final String DARK_GREEN = SECTION + "2";
    public static final String AQUA = SECTION + "b";
    public static final String DARK_AQUA = SECTION + "3";
    public static final String BLUE = SECTION + "9";
    public static final String DARK_BLUE = SECTION + "1";
    public static final String PINK = SECTION + "d";
    public static final String PURPLE = SECTION + "5";

    public static List<String> format(List<String> players, List<String> info, String header, String footer, String extra, int maxColumns, int maxRows, JustifyType justifyType) {
        int columnCapacity = Math.max(info.size(), maxRows);
        boolean overflow = false;
        if (players.size() > maxColumns * columnCapacity) {
            overflow = true;
            players.subList(maxColumns*columnCapacity, players.size()-1).clear();
        }
        int originalPlayerLength = players.size();
        players.addAll(Collections.nCopies(columnCapacity - players.size() % columnCapacity, "\0"));
        int playerColumnCount = players.size() / columnCapacity;
        List<List<String>> rows = new LinkedList<>();
        HashMap<Integer, Integer> columnWidths = new HashMap<>();
        for (int col = 0; col < playerColumnCount; col++) {
            int columnWidth = 0;
            for (int i = 0; i < columnCapacity; i++)
                columnWidth = Math.max(columnWidth, DefaultFont.getStringLength(players.get(col * columnCapacity + i), false)) +
                        (justifyType == JustifyType.CENTER ? DefaultFont.getStringLength( "", false) : 0);
            columnWidths.put(col, columnWidth);
        }
        int infoWidth = 0;
        for (String infoRow : info) {
            infoWidth = Math.max(infoWidth, DefaultFont.getStringLength(infoRow, false));
        }
        infoWidth += 4;
        for (int row = 0; row < columnCapacity; row++) {
            List<String> rowContent = new LinkedList<>();
            for (int i = 0; i < playerColumnCount; i++) {
                String name = players.get(i * columnCapacity + row);
                if (!name.equals("\0") || row < info.size()) {
                    name = name.replace("\0", "");
                    switch (justifyType) {
                        default:
                        case LEFT:
                            rowContent.add(RESET + name + BLACK +
                                    getPadding(columnWidths.get(i) - DefaultFont.getStringLength(name, false)) + RESET);
                            break;
                        case RIGHT:
                            rowContent.add(RESET + BLACK +
                                    getPadding(columnWidths.get(i) - DefaultFont.getStringLength(name, false)) +
                                    RESET + name + RESET);
                            break;
                        case CENTER:
                            rowContent.add(RESET + BLACK +
                                    getPadding((columnWidths.get(i) - DefaultFont.getStringLength(name, false)/2)) +
                                    RESET + name + RESET + BLACK +
                                    getPadding((columnWidths.get(i) - DefaultFont.getStringLength(name, false)/2)) + RESET);
                            break;
                    }
                }
            }
            if (row < info.size()) {
                String infoRow = info.get(row);
                rowContent.add(BLACK +

                        getPadding((infoWidth - DefaultFont.getStringLength(infoRow, false)) / 2) + RESET + infoRow);
            }
            if (!rowContent.isEmpty())
                rows.add(rowContent);
        }
        List<String> finalizedRows = new LinkedList<>();
        int totalWidth = 0;
        for (List<String> list : rows) {
            int rowWidth = 0;
            StringBuilder rowContent = new StringBuilder();
            for (String str : list) {
                rowWidth += DefaultFont.getStringLength(str, false);
                rowContent.append(str);
            }
            String content = rowContent.toString();
            if (!content.isEmpty())
                finalizedRows.add(content);
            totalWidth = Math.max(totalWidth, rowWidth);
        }
        LinkedList<String> sample = new LinkedList<>();
        if (header != null) {
            sample.add(RESET + header);
        }
        sample.addAll(finalizedRows);
        if (overflow && extra != null) {
            sample.add(RESET + extra);
        }
        if (footer != null) {
            sample.add(RESET + footer);
        }

        return sample;
    }


    public static List<String> joinColumns(List<List<String>> cols) {
        // Copy
        List<List<String>> columns = cols.stream().map(
                ArrayList::new
        ).collect(Collectors.toList());

        int maxRows = 0;
        for (List<String> column : columns) {
            maxRows = Math.max(maxRows, column.size());
        }

        for (List<String> column : columns) {
            if (column.size() < maxRows) {
                int columnWidth = 0;
                for (String row : column) {
                    columnWidth = Math.max(columnWidth, DefaultFont.getStringLength(row, false));
                }
                column.addAll(Collections.nCopies(maxRows - column.size(), DefaultFont.getPadding(columnWidth - 4)));
            }
        }

        List<String> joined = new ArrayList<>();

        for (int row = 0; row < maxRows; row ++) {
            int finalRow = row;
            joined.add(columns.stream().map(col -> col.get(finalRow)).collect(Collectors.joining("")));
        }

        return Collections.unmodifiableList(joined);
    }

    private static String getPadding(int paddingLength) {
        switch ((paddingLength == 0) ? 0 : (paddingLength % 4)) {
            default:
            case 0:
                return (new String(new char[paddingLength / 4 + 1])).replace("\0", " ");
            case 1:
                return (new String(new char[paddingLength / 4])).replace("\0", " ") + ".`";
            case 2:
                return (new String(new char[paddingLength / 4 + 1])).replace("\0", " ") + ".";
            case 3:
                break;
        }
        return (new String(new char[paddingLength / 4 + 1])).replace("\0", " ") + "`";
    }
}



