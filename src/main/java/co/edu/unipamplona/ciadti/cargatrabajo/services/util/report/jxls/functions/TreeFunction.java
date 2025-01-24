package co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.functions;

import org.jxls.common.NeedsPublicContext;
import org.jxls.common.PublicContext;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TreeFunction implements NeedsPublicContext {
    private PublicContext context;
    private String propertyName;
    private String childName;

    public List<Number> sum(String propertyName, Object children, String childName) {
        if (this.context == null) {
            throw new IllegalStateException("PublicContext no ha sido establecido en TreeFunction.");
        }
        this.propertyName = propertyName;
        this.childName = childName;
        if (children instanceof String){
            children = this.context.getVar((String)children);
        }
        List<Number> summarized = new ArrayList<>();
        if(children != null){
            calculate((List<?>)children, summarized);
        }
        return summarized;
    }

    private void calculate(List<?> children, List<Number> summarized){
        for (Object child : children){
            List<?> obj = (List<?>) getField(child, this.childName);
            if (obj == null || obj.isEmpty()){
                addSum(summarized, child);
            }else{
                calculate(obj, summarized);
            }
        }   
    }

    @SuppressWarnings("unchecked")
    private void addSum(Object summarized, Object child){
        Object prop = getField(child, propertyName);
        if (prop == null){
            return;
        }
        if (prop instanceof Collection){
            if (summarized == null){
                summarized = new ArrayList<Number>();
            }
            for(int i=0; i< ((List<?>)prop).size(); i++){
                Number o = ((List<Number>)prop).get(i);
                if (o == null){o=0.0;}
                if (i >= ((List<Number>)summarized).size() ){
                    ((List<Number>)summarized).add(o);
                }else{
                    ((List<Object>)summarized).set(i, (double) ((List<Number>)summarized).get(i) + (double)o);
                }
            }
        }
    }

    private Object getField(Object obj, String declaredField) {
        if (obj == null || declaredField == null || declaredField.isEmpty()) {
            return null; 
        }
        try {
            int dotIndex = declaredField.indexOf(".");
            if (dotIndex != -1) {
                String fieldName = declaredField.substring(0, dotIndex);
                String remainingFields = declaredField.substring(dotIndex + 1);
                Field field = obj.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                Object nestedObject = field.get(obj);
                return getField(nestedObject, remainingFields);
            } else {
                Field field = obj.getClass().getDeclaredField(declaredField);
                field.setAccessible(true);
                return field.get(obj);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void setPublicContext(PublicContext context) {
        this.context = context;
    }

}

