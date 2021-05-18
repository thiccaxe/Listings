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

package net.thiccaxe.listings.text;


import net.thiccaxe.listings.Formatter;

public enum DefaultFont {
    A('A', 5),
    a('a', 5),
    B('B', 5),
    b('b', 5),
    C('C', 5),
    c('c', 5),
    D('D', 5),
    d('d', 5),
    E('E', 5),
    e('e', 5),
    F('F', 5),
    f('f', 4),
    G('G', 5),
    g('g', 5),
    H('H', 5),
    h('h', 5),
    I('I', 3),
    i('i', 1),
    J('J', 5),
    j('j', 5),
    K('K', 5),
    k('k', 4),
    L('L', 5),
    l('l', 2),
    M('M', 5),
    m('m', 5),
    N('N', 5),
    n('n', 5),
    O('O', 5),
    o('o', 5),
    P('P', 5),
    p('p', 5),
    Q('Q', 5),
    q('q', 5),
    R('R', 5),
    r('r', 5),
    S('S', 5),
    s('s', 5),
    T('T', 5),
    t('t', 3),
    U('U', 5),
    u('u', 5),
    V('V', 5),
    v('v', 5),
    W('W', 5),
    w('w', 5),
    X('X', 5),
    x('x', 5),
    Y('Y', 5),
    y('y', 5),
    Z('Z', 5),
    z('z', 5),
    NUM_1('1', 5),
    NUM_2('2', 5),
    NUM_3('3', 5),
    NUM_4('4', 5),
    NUM_5('5', 5),
    NUM_6('6', 5),
    NUM_7('7', 5),
    NUM_8('8', 5),
    NUM_9('9', 5),
    NUM_0('0', 5),
    EXCLAMATION_POINT('!', 1),
    AT_SYMBOL('@', 6),
    NUM_SIGN('#', 5),
    DOLLAR_SIGN('$', 5),
    PERCENT('%', 5),
    UP_ARROW('^', 5),
    AMPERSAND('&', 5),
    ASTERISK('*', 5),
    LEFT_PARENTHESIS('(', 4),
    RIGHT_PARENTHESIS(')', 4),
    MINUS('-', 5),
    UNDERSCORE('_', 5),
    PLUS_SIGN('+', 5),
    EQUALS_SIGN('=', 5),
    LEFT_CURL_BRACE('{', 4),
    RIGHT_CURL_BRACE('}', 4),
    LEFT_BRACKET('[', 3),
    RIGHT_BRACKET(']', 3),
    COLON(':', 1),
    SEMI_COLON(';', 1),
    DOUBLE_QUOTE('"', 3),
    SINGLE_QUOTE('\'', 1),
    LEFT_ARROW('<', 4),
    RIGHT_ARROW('>', 4),
    QUESTION_MARK('?', 5),
    SLASH('/', 5),
    BACK_SLASH('\\', 5),
    LINE('|', 1),
    TILDE('~', 5),
    TICK('`', 2),
    PERIOD('.', 1),
    COMMA(',', 1),
    SPACE(' ', 3),
    NULL('\0', 0),
    DEFAULT('a', 5);

    private final char character;
    private final int length;

    DefaultFont(char character, int length) {
        this.character = character;
        this.length = length;
    }

    public char getCharacter(){
        return this.character;
    }

    public int getLength(){
        return this.length+1;
    }

    public static int getStringLength(String name, boolean bold) {
        name = name.replaceAll( Formatter.SECTION + "[a-f0-9klmnor]", "");
        int len = 0;
        for(int i = 0, n = name.length() ; i < n ; i++) {
            DefaultFont defaultFont = getDefaultFontInfo(name.charAt(i));
            len += bold ? defaultFont.getBoldLength() : defaultFont.getLength();
        }
        return len;
    }

    public int getBoldLength(){
        if(this == DefaultFont.SPACE) return this.getLength();
        return this.length + 2;
    }

    public static DefaultFont getDefaultFontInfo(char c){
        for(DefaultFont dFI : DefaultFont.values()){
            if(dFI.getCharacter() == c) return dFI;
        }
        return DefaultFont.DEFAULT;
    }
    public static String getPadding(int paddingLength) {
        switch (paddingLength==0 ? 0 : paddingLength % 4) {
            default:
            case 0:
                return new String(new char[paddingLength/4 + 1]).replace("\0", " ");//    " ".repeat(paddingLength/4+1);
            case 1:
                return new String(new char[paddingLength/4]).replace("\0", " ") + ".`";
            case 2:
                return new String(new char[paddingLength/4 + 1]).replace("\0", " ") + ".";
            case 3:
                return new String(new char[paddingLength/4 + 1]).replace("\0", " ") + "`";
        }
    }
    public static String center(String text, int textLength, int totalLength) {
        totalLength = Math.max(textLength, totalLength);
        String padding = Formatter.RESET + Formatter.BLACK + getPadding((totalLength-textLength)/2) + Formatter.RESET;
        return  padding + text + padding;
    }
}