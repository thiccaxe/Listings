package net.thiccaxe.listings;



import java.util.*;

public class Formatter {
    public static final String SECTION = String.valueOf((char)0x00a7);
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


    private static List<List<Text>> createPlayerColumns(List<Text> players, int columnCapacity) {

        // Does NOT mutate provided List

        final List<List<Text>> rows = new LinkedList<>();
        final int columnCount = players.size() / columnCapacity + (players.size() % columnCapacity == 0 ? 0 : 1);

        for (int r = 0; r < columnCapacity; r ++) {
            final List<Text> column = new LinkedList<>();
            for (int c = 0; c < columnCount; c ++) {
                column.add(c * columnCapacity + r < players.size() ? players.get(c * columnCapacity + r).copy() : new Text("\0"));
            }
            rows.add(column);
        }
        return rows;

        /* Code for building columns (90deg rotated)
        final List<List<Text>> columns = new LinkedList<>();
        final int columnCount = players.size() / columnCapacity + (players.size() % columnCapacity == 0 ? 0 : 1);

        for (int c = 0; c < columnCount; c++) {
            final List<Text> row = new LinkedList<>();
            final int columnOffset = c*columnCount;
            for (int r = 0; r < columnCapacity; r++) {
                row.add(columnOffset + r < players.size() ? players.get(columnOffset + r) : new Text("\0"));
            }
            columns.add(row);
        }
        return columns;
         */
    }

    private static List<List<Text>> applyPlayerColumnPadding(List<List<Text>> rows, JustifyType justifyType) {

        // Mutates provided List

        final HashMap<Integer, Integer> columnWidths = new HashMap<>();
        for (int row = 0; row < rows.size(); row ++) {
            final List<Text> rowList = rows.get(row);
            final int columnSize = rowList.size();
            for (int col = 0; col < columnSize; col ++) {
                columnWidths.put(col, Math.max(columnWidths.getOrDefault(col, 0), rowList.get(col).length()));
            }

        }
        for (List<Text> row : rows) {
            for (int column = 0; column < row.size(); column ++) {
                final int columnWidth = columnWidths.get(column);
                final Text col = row.get(column);
                switch (justifyType) {
                    default:
                    case LEFT:
                        col.prepend(RESET);
                        col.append(BLACK + DefaultFont.getPadding(columnWidth - col.length()) + RESET);
                        break;
                    case RIGHT:
                        col.prepend(RESET + BLACK + DefaultFont.getPadding(columnWidth - col.length()) + RESET);
                        col.append(RESET);
                        break;
                    case CENTER:
                        String padding = DefaultFont.getPadding((columnWidth - col.length())/2);
                        col.prepend(RESET + BLACK + padding + RESET);
                        col.append(RESET + BLACK + padding + RESET);
                        break;
                }
            }
        }


        /* Code for columns (90deg rotation)
        for (List<Text> column : columns) {
            int columnWidth = 0;
            for (Text row : column) {
                columnWidth = Math.max(columnWidth, row.length());
            }
            for (Text row : column) {
                switch (justifyType) {
                    default:
                    case LEFT:
                        row.prepend(RESET);
                        row.append(BLACK + DefaultFont.getPadding(columnWidth - row.length()) + RESET);
                        break;
                    case RIGHT:
                        row.prepend(RESET + BLACK + DefaultFont.getPadding(columnWidth - row.length()) + RESET);
                        row.append(RESET);
                        break;
                    case CENTER:
                        String padding = DefaultFont.getPadding((columnWidth - row.length())/2);
                        row.prepend(RESET + BLACK + padding + RESET);
                        row.append(RESET + BLACK + padding + RESET);
                        break;
                }
            }

        }

         */
        return rows;
    }

    public static List<Text> createColumnGroup(List<Text> players, List<Text> info, Text header, Text footer, int columnCapacity, int rowCapacity, JustifyType justifyType) {
        columnCapacity = Math.max(info.size(), columnCapacity);
        List<List<Text>> rows = applyPlayerColumnPadding(createPlayerColumns(players, columnCapacity), justifyType);
        List<Text> sample = new LinkedList<>();


        final List<Text> paddedInfo = new LinkedList<>();
        applyPlayerColumnPadding(createPlayerColumns(info, columnCapacity), JustifyType.CENTER).forEach(row -> paddedInfo.add(Text.assemble(row)));
        System.out.println(paddedInfo);
        final HashMap<Integer, Integer> columnWidths = new HashMap<>();
        for (final List<Text> rowList : rows) {
            final int columnSize = rowList.size();
            for (int col = 0; col < columnSize; col++) {
                columnWidths.put(col, Math.max(columnWidths.getOrDefault(col, 0), rowList.get(col).length()));
            }

        }
        int totalLength = 0;
        for (int r = 0; r < rows.size(); r ++) {
            List<Text> row = rows.get(r);
            if (r < paddedInfo.size()) {
                row.add(paddedInfo.get(r));
            }
            Text text = Text.assemble(row);
            totalLength = Math.max(totalLength, text.length());
            sample.add(text);
        }
        System.out.println(totalLength);
        totalLength = Math.max(totalLength, header.length());
        String padding = DefaultFont.getPadding((totalLength-header.length()));
        header.prepend(RESET + BLACK + padding + RESET);
        header.append(RESET + BLACK + padding + RESET);
        sample.add(0, header);
        return sample;

    }



    private static List<Text> createColumnGroup(List<Text> players, Text header, Text footer, int columnCapacity, int rowCapacity, boolean justifyLeft) {
        int totalCapacity = columnCapacity * rowCapacity;
        boolean addFooter = false;
        int extraPlayers = Math.abs(players.size() - totalCapacity);
        if (players.size() > totalCapacity) {
            players.subList(totalCapacity, players.size()).clear();
            addFooter = true;
        }

        players.addAll(Collections.nCopies(columnCapacity - (players.size() % columnCapacity), new Text("\0")));

        final int columnCount = players.size() / columnCapacity;



        // Determine the widths of each column. Store them in a map for later use.

        final HashMap<Integer, Integer> columnWidths = new HashMap<>();

        for (int col = 0; col < columnCount; col++) {
            int columnWidth = 0;
            for (int row = 0; row < columnCapacity; row++) {
                columnWidth = Math.max(columnWidth, players.get(col * columnCapacity + row).length());
            }
            columnWidths.put(col, columnWidth);
        }

        // Assemble each entry into the rows.
        final List<List<Text>> rows = new LinkedList<>();

        int totalWidth = 0;
        HashMap<Integer, Integer> finalColumnWidths = new HashMap<>();


        for (int row = 0; row < columnCapacity; row++) {
            final List<Text> rowContent = new LinkedList<>();
            for (int col = 0; col < columnCount; col++) {

                final Text text = players.get(col * columnCapacity + row);
                text.name(text.name().replace("\0", ""));
                final String name = text.name();
                final int len = text.length();
                if (justifyLeft) {
                    text.name(RESET + name + BLACK + DefaultFont.getPadding(
                            columnWidths.get(col) - len
                    ) + RESET);
                } else {
                    text.name(RESET + BLACK + DefaultFont.getPadding(
                            columnWidths.get(col) - len
                    ) + RESET + name + RESET);
                }

                finalColumnWidths.put(col, Math.max(finalColumnWidths.getOrDefault(col, 0), text.length()));

                rowContent.add(text);
            }
            //System.out.println(rowContent + " <");
            rows.add(rowContent);
        }
        for (int width : finalColumnWidths.values()) {
            totalWidth += width;
        }

        final List<Text> assembledRows = new LinkedList<>();
        if (header.length() > 0) {
            header.name(DefaultFont.center(header.name(), header.length(), totalWidth));
            assembledRows.add(header);
        }
        rows.forEach(row -> {
            assembledRows.add(Text.assemble(row));
        });
        if (addFooter && footer.length() > 0) {
            footer.name(footer.name().replace("{X}", String.valueOf(extraPlayers)));
            footer.name(DefaultFont.center(footer.name(), footer.length(), totalWidth));
            assembledRows.add(footer);
        }



        return assembledRows;
    }

    public static List<String> format(List<Text> players, List<Text> info, Text header, Text footer, boolean justify) {
        /*
        players.addAll(Arrays.asList(
                new Text("TestPlayer1"),
                new Text("TestPlayer2"),
                new Text("TestPlayer3"),
                new Text("TestPlayer4"),
                new Text("TestPlayer5"),
                new Text("TestPlayer6"),
                new Text("TestPlayer7"),
                new Text("TestPlayer8"),
                new Text("TestPlayer9"),
                new Text("TestPlayer10"),
                new Text("TestPlayer11"),
                new Text("TestPlayer12"),
                new Text("TestPlayer13"),
                new Text("TestPlayer14"),
                new Text("TestPlayer15"),
                new Text("TestPlayer16"),
                new Text("TestPlayer17"),
                new Text("TestPlayer18"),
                new Text("TestPlayer19"),
                new Text("TestPlayer20"),
                new Text("TestPlayer21"),
                new Text("TestPlayer22"),
                new Text("TestPlayer23"),
                new Text("TestPlayer24"),
                new Text("TestPlayer25"),
                new Text("TestPlayer26"),
                new Text("TestPlayer27"),
                new Text("TestPlayer28"),
                new Text("TestPlayer29"),
                new Text("TestPlayer30")
        ));

         */
        List<String> sample = new LinkedList<>();
        createColumnGroup(players, header, footer, 12, 2, justify).forEach(row -> sample.add(row.name()));
        return sample;

        /*
        final int columnCapacity = Math.max(info.size(),
                20
        );

        final int originalPlayerLength = players.size();

        players.addAll(Collections.nCopies(columnCapacity - (players.size() % columnCapacity), "\0"));

        final int playerColumnCount = players.size() / columnCapacity;


        final List<List<String>> rows = new LinkedList<>();

        final HashMap<Integer, Integer> columnWidths = new HashMap<>();


        for (int col = 0; col < playerColumnCount; col++) {
            int columnWidth = 0;
            for (int row = 0; row < columnCapacity; row++) {
                columnWidth = Math.max(columnWidth, DefaultFont.getStringLength(players.get(col * columnCapacity + row)));
            }
            columnWidths.put(col, columnWidth);
        }

        int infoWidth = 0;
        for (String infoRow : info) {
            infoWidth = Math.max(infoWidth, DefaultFont.getStringLength(infoRow));
        }
        infoWidth += 4;


        for (int row = 0; row < columnCapacity; row++) {
            List<String> rowContent = new LinkedList<>();
            for (int col = 0; col < playerColumnCount; col++) {
                String name = players.get(col * columnCapacity + row);
                if (!name.equals("\0") || row < info.size()) {
                    name = name.replace("\0", "");
                    if (justify) {
                        rowContent.add(
                                RESET +
                                        name +
                                        BLACK +
                                        DefaultFont.getPadding(
                                                columnWidths.get(col) - DefaultFont.getStringLength(name)
                                        ) +
                                        RESET
                        );
                    } else {
                        rowContent.add(
                                RESET +
                                        BLACK +
                                        DefaultFont.getPadding(
                                                columnWidths.get(col) - DefaultFont.getStringLength(name)
                                        ) +
                                        RESET +
                                        name
                        );
                    }
                }
            }
            if (row < info.size()) {
                String name = info.get(row);
                rowContent.add(
                        BLACK +
                                DefaultFont.getPadding((infoWidth - DefaultFont.getStringLength(name)) / 2) +
                                RESET +
                                name
                );
            }
            if (!rowContent.isEmpty()) {
                rows.add(rowContent);
            }
        }
        final List<String> finalizedRows = new LinkedList<>();
        int totalWidth = 0;
        for (List<String> row : rows) {
            int rowWidth = 0;
            StringBuilder rowContent = new StringBuilder();
            for (String col : row) {
                rowWidth += DefaultFont.getStringLength(col);
                rowContent.append(col);
            }
            final String content = rowContent.toString();
            //System.out.println(content + "|" + content.length());
            if (!content.isEmpty()) {
                finalizedRows.add(content);
            }
            totalWidth = Math.max(totalWidth, rowWidth);
        }

        LinkedList<String> sample = new LinkedList<>();

        //System.out.println(totalWidth);
        //System.out.println(DefaultFont.getStringLength(header));

        sample.add(
                BLACK +
                        DefaultFont.getPadding((Math.max(0,
                                totalWidth - DefaultFont.getStringLength(header)
                        )) / 2) +
                        RESET +
                        header
        );

        sample.addAll(finalizedRows);
         */
        //return new LinkedList<>();
    }
        /*
        final int maximumPlayersInColumn = Math.max(20, info.size());

        final int totalPlayerColumns = players.size() / maximumPlayersInColumn + (players.size() % maximumPlayersInColumn == 0 ? 0 : 1);


        final HashMap<Integer, Integer> columnWidths = new HashMap<>();
        final HashMap<Integer, Integer> rowsInColumn = new HashMap<>();

        int totalWidth = 0;
        for (int column = 0; column < totalPlayerColumns; column++) {
            int maximumColumnWidth = 0;
            int columnRowCount = 0;
            for (int row = 0; row < maximumPlayersInColumn; row++) {
                if (column * maximumPlayersInColumn + row < players.size()) {
                    maximumColumnWidth = Math.max(maximumColumnWidth, DefaultFont.getStringLength(players.get(column * maximumPlayersInColumn + row)));
                    columnRowCount ++;
                }
            }
            columnWidths.put(column, maximumColumnWidth);
            rowsInColumn.put(column, columnRowCount);
            totalWidth += maximumColumnWidth;
            //System.out.println(maximumColumnWidth);
        }

        final LinkedList<StringBuilder> rows = new LinkedList<>();

        for (int row = 0; row < maximumPlayersInColumn; row ++) {
            StringBuilder rowContent = new StringBuilder();
            for (int column = 0; column< totalPlayerColumns; column++) {
                String name = "";
                if (row < rowsInColumn.get(column)) {
                    name = players.get(column * maximumPlayersInColumn + row);
                    //System.out.println(names.get(column * maximumPlayersInColumn + row));
                }
                final int nameLength = DefaultFont.getStringLength(name);
                final int paddingLength = Math.max(0,  columnWidths.get(column) - nameLength);
                rowContent.append(name).append(BLACK).append(getPadding(paddingLength)).append(RESET);
            }
            if (row < info.size()) {
                rowContent.append(info.get(row));
            }
            totalWidth = Math.max(totalWidth, DefaultFont.getStringLength(rowContent.toString()));
            rows.add(rowContent);
        }
        final int headerLength = DefaultFont.getStringLength(header)+4;
        totalWidth = Math.max(totalWidth, headerLength);
        sample.add(getPadding(totalWidth-headerLength) + header);
        for (StringBuilder row : rows) {
            sample.add(row.toString());
        }
        //System.out.println(sample);

        return sample;
    }
    */




}
