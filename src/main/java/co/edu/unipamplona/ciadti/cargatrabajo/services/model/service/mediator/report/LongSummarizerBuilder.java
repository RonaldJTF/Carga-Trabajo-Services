package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.report;

import java.util.List;

import org.jxls.functions.Summarizer;
import org.jxls.functions.SummarizerBuilder;

public class LongSummarizerBuilder implements SummarizerBuilder<List<?>>{

    @Override
    public Summarizer<List<?>> build() {
        return new Summarizer<List<?>>() {
            long sum = 0;

            @Override
            public void add(Object value) {
                if (value instanceof Long) {
                    sum += (Long) value;
                } else {
                    throw new IllegalArgumentException("Expected value of type Long, but got: " + value.getClass());
                }
            }

            @Override
            public List<?> getSum() {
                return List.of(1, 3, 5,7, 2, 44);
            }
        };
    }

}
