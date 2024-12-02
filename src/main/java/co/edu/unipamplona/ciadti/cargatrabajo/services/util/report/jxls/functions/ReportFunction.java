package co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.functions;

import java.lang.reflect.Field;
import java.util.List;

import org.jxls.common.NeedsPublicContext;
import org.jxls.common.PublicContext;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.ReportAppointmentDTO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.ReportAppointmentDTO.ComparativeAttribute;

public class ReportFunction implements NeedsPublicContext {
    private PublicContext context;

    public Double sumComparativesByScope(List<ReportAppointmentDTO> items, String key, String propertyName) {
        Double summarized = 0.0;
        
        if(items != null && key != null){  
            for (ReportAppointmentDTO e : items){
                ComparativeAttribute comparativeAttribute = getComparativeAttribute(e.getComparativesByScope(), key);
                if(comparativeAttribute != null){
                    Object obj = getField(comparativeAttribute, propertyName);
                    if (obj instanceof Number){
                        summarized += ((Number)obj).doubleValue();
                    }
                }
            }
        }
        return summarized;
    }

    private ComparativeAttribute getComparativeAttribute(List<ComparativeAttribute> comparativesByScope, String key){
        for(ComparativeAttribute ca : comparativesByScope){
            if(ca.getKey().equals(key)){
                return ca;
            }
        }
        return null;
    }

    @Override
    public void setPublicContext(PublicContext context) {
        this.context = context;
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

}