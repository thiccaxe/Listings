package net.thiccaxe.listings;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Text {


    private String name = "";
    private int length = 0;
    private String padding = "";

    public Text(String name) {
        this.name = name;
        name(name);

    }

    public static Text assemble(List<Text> row) {
        Text text = new Text("");
        for (Text t : row) {
            text.append(t);
        }
        return text;
    }

    public void padding(int total) {
        if (total-length > 0) {
            this.padding = DefaultFont.getPadding(total - length);
            this.length = DefaultFont.getStringLength(name + padding);
        }
    }


    public int name(String name) {
        this.name = name;
        length = DefaultFont.getStringLength(this.name);
        return length;
    }
    public String name() {
        return this.name;
    }
    public int length() {
        return this.length;
    }
    public int append(String text) {
        //System.out.println("Appending \"" + text + "\" to : " + name);
        return this.name(name + text);
    }
    public int append(Text text) {

        return append(text.name);
    }
    public int prepend(String text) {
        return this.name(text + name);
    }
    public int prepend(Text text) {
        return prepend(text.name);
    }
    public Text copy() {
        return new Text(name);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + name + ")";
    }




}
