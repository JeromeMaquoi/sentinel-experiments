package be.unamur.snail.processors;

import spoon.reflect.declaration.CtElement;

public interface InstrumentProcessor<T extends CtElement> {
    void instrument(T element, InstrumentationUtils utils);
}
