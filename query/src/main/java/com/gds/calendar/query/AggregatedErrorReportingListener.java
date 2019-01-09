package com.gds.calendar.query;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.function.Consumer;

import static org.springframework.util.Assert.notNull;

public class AggregatedErrorReportingListener implements ANTLRErrorListener {

    private final List<Consumer<String>> consumers = new ArrayList<>();

    public AggregatedErrorReportingListener registerErrorConsumer(final Consumer<String> errorConsumer) {
        notNull(errorConsumer, "Mandatory argument 'errorConsumer' is missing.");
        consumers.add(errorConsumer);
        return this;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        dispatch(offendingSymbol+":"+line+":"+msg+":"+e.getMessage());
    }

    @Override
    public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {
        dispatch("reportAmbiguity...");
    }

    @Override
    public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) {
        dispatch("reportAttemptingFullContext...");
    }

    @Override
    public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {
        dispatch("reportContextSensitivity...");
    }

    private void dispatch(final String message) {
        notNull(message, "Mandatory argument 'message' is missing.");
        consumers.forEach(c -> c.accept(message));
    }
}
