package co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.poi;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Methods;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Trace;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CellPOI {
    private Object value;
    private Object subValue;
    private List<CellPOI> children;
    private Style style;
    private int columnIndex;
    private int rowIndex;
    private int usedRows;
    private int usedColumns;
    private int aditionalCellsToUse;
    private int usedCells_;
    private boolean noExtend_;
    private boolean showInColumn_;

    public void completeStyles(Style parentStyle){
        if (style == null){
            style = parentStyle;
        }else{
            Field[] fields = parentStyle.getClass().getDeclaredFields();
            for (Field field : fields) {
                try {
                    field.setAccessible(true); 
                    if (field.get(style) == null 
                        || (field.getType().isPrimitive() && isNumericType(field.getType()) && field.getInt(style) < 0)) {
                        String fieldName = field.getName(); 
                        Field parentField = parentStyle.getClass().getDeclaredField(fieldName);
                        parentField.setAccessible(true); 
                        Object parentValue = parentField.get(parentStyle);
                        field.set(style, parentValue);
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    Trace.logError(this.getClass().getName(), Methods.getCurrentMethodName(this.getClass()), e);
                }
            }
        }
    }

    public void addSiblings(List<CellPOI> siblings){
        CellPOI parent = this;
        if (siblings != null && !siblings.isEmpty()){
            for (CellPOI cell : siblings) {
                if(parent.getChildren() == null){
                    parent.setChildren(new ArrayList<>());
                }
                parent.getChildren().add(cell);
                parent = cell;
            }
        }
    }

    public static List<CellPOI> createSiblings(List<CellPOI> siblings){
        CellPOI parent = null;
        CellPOI cell;
        if (siblings != null && !siblings.isEmpty()){
            parent = siblings.get(0);
            for (int i = 1; i<siblings.size(); i++){
                cell = siblings.get(i);
                if(parent.getChildren() == null){
                    parent.setChildren(new ArrayList<>());
                }
                parent.getChildren().add(cell);
                parent = cell;
            }
            return List.of(siblings.get(0));
        }
        return null;
    }

    private static final Set<Class<?>> numericTypes = new HashSet<>(Arrays.asList(
            Byte.class, Short.class, Integer.class, Long.class,
            Float.class, Double.class, int.class, double.class, float.class, byte.class
    ));

    private static boolean isNumericType(Class<?> type) {
        return Number.class.isAssignableFrom(type) || numericTypes.contains(type);
    }
}
