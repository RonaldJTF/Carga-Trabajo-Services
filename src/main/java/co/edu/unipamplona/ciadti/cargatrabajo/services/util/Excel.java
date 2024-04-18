package co.edu.unipamplona.ciadti.cargatrabajo.services.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class Excel {
    private final ArrayList columnList;

    public Excel(ArrayList columnList) {
        this.columnList = columnList;
    }

    public ArrayList convertData(InputStream inputStream, Class<?> classVO) throws CiadtiException {
        ArrayList dataList;
        ArrayList results = null;
        boolean isValid;
        Object instanceOfClass;
        String value;
        String[] columns;

        try {
            dataList = this.read(inputStream);
            if (dataList != null && dataList.size() > 0) {
                results = new ArrayList();
                for (Object data : dataList) {
                    columns = (String[]) data;
                    if (columns != null) {
                        instanceOfClass = classVO.newInstance();
                        isValid = false;
                        for (int j = 0; j < columns.length; j++) {
                            value = columns[j];
                            if (null != value) {
                                this.buildObject(classVO, instanceOfClass, (String) columnList.get(j), value);
                                isValid = true;
                            } else
                                isValid = false;
                        }
                        if (isValid)
                            results.add(instanceOfClass);
                    }
                }
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new CiadtiException(e.getMessage(), 500);
        }
        return results;
    }

    public ArrayList<String[]> read(InputStream inputStream) throws CiadtiException {
        ArrayList<String[]> data = new ArrayList();
        int counter;
        int totalColumns;
        String[] row;
        try {
            if (columnList != null) {
                totalColumns = columnList.size();
                int rowCount = 0;
                XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
                XSSFSheet sheet = workbook.getSheetAt(0);
                for (Row r : sheet) {
                    if (rowCount != 0) {
                        counter = 0;
                        row = new String[totalColumns];
                        for (Cell cell : r) {
                            switch (cell.getCellType()) {
                                case NUMERIC:
                                    row[counter] = String.valueOf((long) cell.getNumericCellValue());
                                    break;
                                case STRING:
                                    row[counter] = cell.getStringCellValue();
                                    break;
                            }
                            if ((counter + 1) % totalColumns == 0) {
                                data.add(row);
                            }
                            counter++;
                        }
                    }
                    rowCount++;
                }
                workbook.close();
                if (data.size() == 0)
                    throw new CiadtiException("Excel no importado correctamente.", 400);
            }
        } catch (Exception e) {
            throw new CiadtiException(e.getMessage(), 500);
        }
        return data;
    }

    private void buildObject(Class<?> classVO, Object instanceOfClass, String fieldName, String fieldValue) throws CiadtiException {
        Field field;
        try {
            field = getDeclaredFieldIngoreCase(classVO, fieldName);
            field.setAccessible(true);
            field.set(instanceOfClass, fieldValue);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new CiadtiException(e.getMessage(), 500);
        }
    }

    public Field getDeclaredFieldIngoreCase(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getName().equalsIgnoreCase(fieldName)) {
                return field;
            }
        }
        throw new NoSuchFieldException(fieldName);
    }
}
