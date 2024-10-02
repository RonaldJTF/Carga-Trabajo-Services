package co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.summarizer;


import org.jxls.functions.Summarizer;
import org.jxls.functions.SummarizerBuilder;

public class IntegerSummarizerBuilder implements SummarizerBuilder<Integer>{

    @Override
    public Summarizer<Integer> build() {
        return new Summarizer<Integer>() {
            private int sum = 0;
            
            @Override
            public void add(Object value) {
                if (value instanceof Integer) {
                    sum += (Integer) value;
                } else if (value instanceof Number) {
                    sum += ((Number) value).intValue();
                }
            }

            @Override
            public Integer getSum() {
                return sum;
            }
        };
    }

}
