package clojuredev;

import java.io.Reader;
import java.util.Iterator;

import clojure.lang.LineNumberingPushbackReader;
import clojure.lang.LispReader;
import clojure.lang.RT;
import clojure.lang.Var;

public class ParseIterator implements Iterator {

    private LineNumberingPushbackReader pushbackReader;
    private Object EOF = new Object();
    private Object current = null;

    public ParseIterator(Reader rdr) {
        LineNumberingPushbackReader pushbackReader = (rdr instanceof LineNumberingPushbackReader) ? (LineNumberingPushbackReader) rdr
                : new LineNumberingPushbackReader(rdr);
        Var.pushThreadBindings(RT.map(clojure.lang.Compiler.LOADER, RT
                .makeClassLoader(), RT.CURRENT_NS, RT.CURRENT_NS.get(),
                clojure.lang.Compiler.LINE_BEFORE, pushbackReader
                        .getLineNumber(), clojure.lang.Compiler.LINE_AFTER,
                pushbackReader.getLineNumber()));
    }

    @Override
    public boolean hasNext() {
        try {
            current = LispReader.read(pushbackReader, false, EOF, false);
            return current != EOF;
        }
        catch (Exception e) {
            return false;
        }
    }

    @Override
    public Object next() {
        return current;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
