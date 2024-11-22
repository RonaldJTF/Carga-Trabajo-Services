package co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.command;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jxls.area.Area;
import org.jxls.command.AbstractCommand;
import org.jxls.command.Command;
import org.jxls.command.RunVar;
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.common.JxlsException;
import org.jxls.common.Size;
import org.jxls.transform.poi.PoiTransformer;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TreeCommand extends AbstractCommand {
    public static final String COMMAND_NAME = "tree";
    private String tree; 
    private String children; 
    private String nodeName;
    private String nodeDescription;
    private String adjustBy;
    private static final String TITTLE_VAR = "tittle";   
    private static final String NODE_VAR = "node";   

    private Area treeArea;
    private Area treeTittleArea;
    private Area dataArea;
    private Area dataTittleArea;

    private Area summaryTittleArea;
    private Area summaryDataArea;

    private Map<Object, Integer> _deeps = new LinkedHashMap<>();
    private int _finalColumnSectionOfTree = 0;
    private float _defaultHeightInPointToRowOfTree = 0;
    private XSSFSheet _sheet;
    private XSSFFont _subFont;

    @Override
    public String getName() {
        return TreeCommand.COMMAND_NAME; 
    }

    public void setTree(String tree) {
        this.tree = tree;
    }

    public void setChildren(String children) {
        this.children = children;
    }

    public void setAdjustBy(String adjustBy) {
        this.adjustBy = adjustBy;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public void setNodeDescription(String nodeDescription) {
        this.nodeDescription = nodeDescription;
    }


    @Override
    public Command addArea(Area area) {
        if (areaList.size() >= 6) {
            throw new JxlsException("No puede agregar más áreas al comando TreeCommand.");
        }
        if (areaList.isEmpty()) {
            this.treeTittleArea = area; 
        }else if (areaList.size() == 1) {
            this.dataTittleArea = area;
        }else if (areaList.size() == 2) {
            this.treeArea = area;
        }else if (areaList.size() == 3) {
            this.dataArea = area;
        }else if (areaList.size() == 4) {
            this.summaryTittleArea = area; 
        }else if (areaList.size() == 5) {
            this.summaryDataArea = area; 
        }
        return super.addArea(area);
    }

    @Override
    public Size applyAt(CellRef cellRef, Context context) {
        PoiTransformer transformer = (PoiTransformer) getTransformer();
        XSSFWorkbook workbook = (XSSFWorkbook) transformer.getWorkbook();
        XSSFSheet sheet = workbook.getSheet(cellRef.getSheetName());
        XSSFRow rowOfTree = sheet.getRow(cellRef.getRow() +  this.treeTittleArea.getSize().getHeight());

        this._sheet = sheet;
        this._subFont = workbook.createFont();
        this._subFont.setColor(IndexedColors.GREY_40_PERCENT.getIndex()); 
        this._subFont.setBold(false);
        this._subFont.setItalic(true);
        this._subFont.setFontName(rowOfTree.getRowStyle().getFont().getFontName());
        this._defaultHeightInPointToRowOfTree = rowOfTree.getHeightInPoints();
        
        getMaxDeeps((List<?>) context.getVar(this.tree), getField(((List<?>) context.getVar(this.tree)).get(0), this.adjustBy), 0, this._deeps);
        int treeWidth =  this._deeps.values().stream().mapToInt(Integer::intValue).sum() ;
        this._finalColumnSectionOfTree = treeWidth + cellRef.getCol();
        Size tittleSize = processTittle(cellRef, context);

        Size treeSize;
        try (RunVar runVar = new RunVar(TreeCommand.NODE_VAR, context)){
            treeSize = processNodes((List<?>) context.getVar(this.tree), new CellRef(cellRef.getSheetName(), cellRef.getRow() + this.treeTittleArea.getSize().getHeight(), cellRef.getCol()), context, runVar, 1, 0, true);
            treeSize.setWidth(treeWidth);
        }
        int width = Math.max(tittleSize.getWidth(), treeSize.getWidth() + dataArea.getSize().getWidth());
        int height = tittleSize.getHeight() + treeSize.getHeight();
        return new Size(width, height);
    }

    private Size processTittle(CellRef cellRef, Context context){
        int col = 0;
        CellRef cell = cellRef;
        Size size = this.treeTittleArea.getSize();
        try (RunVar runVar = new RunVar(TreeCommand.TITTLE_VAR, context)){    
            for (Map.Entry<Object, Integer> entry : this._deeps.entrySet()) {
                runVar.put(entry.getKey());
                cell = new CellRef(cell.getSheetName(), cell.getRow(), cell.getCol() + col);
                size = this.treeTittleArea.applyAt(cell, context);
                mergeRegion(cell, new Size(entry.getValue(), this.treeTittleArea.getSize().getHeight()));
                col = entry.getValue();
            } 
        }
        this.dataTittleArea.applyAt(new CellRef(cell.getSheetName(), cell.getRow(), this._finalColumnSectionOfTree), context);
        return size;
    }

    private Size processNodes(List<?> children, CellRef cellRef, Context context, RunVar runVar, int deep, int colOffset, boolean isRoot){
        this.treeArea.applyAt(cellRef, context);
        int rowOffset = 0;
        for (Object child : children){
            runVar.put(child);
            CellRef childCellRef = new CellRef(cellRef.getSheetName(), cellRef.getRow() + rowOffset, cellRef.getCol() + colOffset);
            Size childSize;
            List<?> childChildren = (List<?>) getField(child, this.children);
            CellRangeAddress region;
            if(childChildren != null && !childChildren.isEmpty()){
                boolean onlyHasChildrenOfSameType = !((List<?>)getField(child, this.children)).stream().anyMatch(item -> !compare(getField(item, this.adjustBy), getField(child, this.adjustBy)));
                if (onlyHasChildrenOfSameType){
                    childSize = processNodes(childChildren, childCellRef, context, runVar, deep + 1, 1, false);
                    region = mergeRegion(childCellRef, childSize);
                }else{
                    childSize = processNodes(childChildren, childCellRef, context, runVar, 1, this._deeps.get(getField(child, this.adjustBy)) - deep + 1, false);
                    region = mergeRegion(childCellRef, childSize);
                }
            }else{
                childSize = new Size(this._deeps.get(getField(child, this.adjustBy)) - deep + 1, 1);
                this.treeArea.applyAt(childCellRef, context);
                this.dataArea.applyAt( new CellRef(childCellRef.getSheetName(), childCellRef.getRow(), this._finalColumnSectionOfTree), context);
                region = mergeRegion(childCellRef, childSize);
                completeCells(childCellRef, context, runVar, childCellRef.getCol() + childSize.getWidth());
            }

            if(this.nodeName != null || this.nodeDescription != null){
                applyAndAdjustContentToCell(childCellRef, region, (String)getField(child, this.nodeName),  (String)getField(child, this.nodeDescription));
            }
            rowOffset += childSize.getHeight();  
            
            if(isRoot){
                try (RunVar runVarRoot = new RunVar("root", context)){    
                    runVarRoot.put(child);
                    this.summaryTittleArea.applyAt(new CellRef(cellRef.getSheetName(), cellRef.getRow() + rowOffset, cellRef.getCol()), context); 
                    this.summaryDataArea.applyAt(new CellRef(cellRef.getSheetName(), cellRef.getRow() + rowOffset, this._finalColumnSectionOfTree), context); 
                    for (int i=0; i< this.summaryTittleArea.getSize().getHeight(); i++){
                        mergeRegion(new CellRef(cellRef.getSheetName(), cellRef.getRow() + rowOffset + i, cellRef.getCol()), new Size(this._finalColumnSectionOfTree - cellRef.getCol(), 1));
                    }
                    rowOffset += this.summaryTittleArea.getSize().getHeight();
                }
            }
        }
        return new Size(colOffset, rowOffset);
    }
    
    private Size completeCells(CellRef cellRef, Context context, RunVar runVar, int startCol){
        if (this._finalColumnSectionOfTree <= startCol){
            return Size.ZERO_SIZE;
        }
        for (int col = startCol; col<this._finalColumnSectionOfTree; col++){
            runVar.put(null);
            CellRef cell = new CellRef(cellRef.getSheetName(), cellRef.getRow(), col);
            this.treeArea.applyAt(cell, context);
        }
        return new Size(this._finalColumnSectionOfTree - startCol, 1);
    }

    private CellRangeAddress mergeRegion(CellRef cellRef, Size size){
        CellRangeAddress region  = null;
        if (size.getHeight() > 1 || size.getWidth() > 1){
            XSSFRow row = this._sheet.getRow(cellRef.getRow());
            XSSFCell cell = row.getCell(cellRef.getCol());
            region = new CellRangeAddress(cellRef.getRow(), cellRef.getRow() + size.getHeight() -1 , cellRef.getCol(), cellRef.getCol() + size.getWidth() - 1);
            this._sheet.addMergedRegion(region);
            CellStyle cellStyle = cell.getCellStyle();
            for (int i = region.getFirstRow(); i <= region.getLastRow(); i++) {
                Row r = this._sheet.getRow(i);
                if (r == null) {
                    r = this._sheet.createRow(i);
                }
                for (int j = region.getFirstColumn(); j <= region.getLastColumn(); j++) {
                    Cell c = r.getCell(j);
                    if (c == null) {
                        c = r.createCell(j);
                    }
                    c.setCellStyle(cellStyle);
                }
            }
        }
        return region;
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

    private int getMaxDeeps(List<?> nodes, Object value, int deep, Map<Object, Integer> maxDeeps) {
        for (Object e : nodes) {
            int currentDeep = deep;
            if (compare(getField(e, this.adjustBy), value)) {
                currentDeep += 1;
                maxDeeps.put(value, Math.max(getValueInt(maxDeeps.get(value)), currentDeep));
            }else{
                int nextValue = getValueInt(maxDeeps.get(getField(e, this.adjustBy)));
                Object key = getField(e, this.adjustBy);
                maxDeeps.put(key, Math.max(1, nextValue));
                maxDeeps.put(key, getMaxDeeps((List<?>)getField(e, this.children), getField(e, this.adjustBy), 1, maxDeeps));
            }
            if (getField(e, this.children) != null) {
                maxDeeps.put(value, Math.max(getMaxDeeps((List<?>)getField(e, this.children), value, currentDeep, maxDeeps),  getValueInt(maxDeeps.get(value))));
            }
        }
        return getValueInt(maxDeeps.get(value));
    }

    private int getValueInt(Integer value){
        return value != null ? value : 0;
    }

    private boolean compare(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) {
            return true;
        }
        if (obj1 == null || obj2 == null) {
            return false;
        }
        return obj1.equals(obj2);
    }

    private void applyAndAdjustContentToCell(CellRef cellRef, CellRangeAddress region, String text1, String text2) {
        XSSFRow row = this._sheet.getRow(cellRef.getRow());
        XSSFCell cell = row.getCell(cellRef.getCol());

        text1 = text1 != null ? text1 : "";
        text2 = text2 != null && !text2.isBlank() ? text2 : "";

        XSSFRichTextString richText = new XSSFRichTextString(text1 + (text2.length() > 0 ? "\n" + text2 : " "));
        richText.applyFont(text1.length(), text1.length() + text2.length() + 1, this._subFont);
        cell.setCellValue(richText);
    }
}
