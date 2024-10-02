package co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.command;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jxls.area.Area;
import org.jxls.command.AbstractCommand;
import org.jxls.command.Command;
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.common.Size;
import org.jxls.transform.poi.PoiTransformer;

public class StyleCommand extends AbstractCommand{
    private Area area;

    private String width;
    private String height;
    private String backgroundColorRGB;
    private Boolean[] borders;
    private BorderStyle borderStyle;
    private int[] borderColorRGB;

    @Override
    public String getName() {
        return "style";
    }

    @Override
    public Command addArea(Area area) {
        super.addArea(area);
        this.area = area;
        return this;
    }

    @Override
    public Size applyAt(CellRef cellRef, Context context) {
        Size resultSize = area.applyAt(cellRef, context);
        if (resultSize.equals(Size.ZERO_SIZE)) {
            return resultSize;
        }
        PoiTransformer transformer = (PoiTransformer) area.getTransformer();
        XSSFWorkbook workbook = (XSSFWorkbook) transformer.getWorkbook();
        XSSFSheet sheet = workbook.getSheet(cellRef.getSheetName());
        XSSFRow row = sheet.getRow(cellRef.getRow());
        int w = 0;
        for (int i = cellRef.getCol(); i < cellRef.getCol() + resultSize.getWidth(); i=i+1+w){
            XSSFCell cell = row.getCell(i);
            XSSFCellStyle style = getStyle(workbook, cell, context);
            CellRangeAddress region = applyStyle(sheet, cell, style);
            w = region != null ? region.getLastColumn() - region.getFirstColumn() : 0;
        }
        return resultSize;
    }

    private XSSFCellStyle getStyle(XSSFWorkbook workbook, XSSFCell cell, Context context){
        XSSFCellStyle currentStyle = cell.getCellStyle();
        XSSFCellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(currentStyle);

        XSSFFont font_ = (XSSFFont) style.getFont();
        
        if(backgroundColorRGB != null){
            int[] bg = this.formatToRGB(this.getVal(backgroundColorRGB, context));
            XSSFColor customColor = new XSSFColor(new byte[]{(byte)bg[0], (byte)bg[1], (byte)bg[2]}, null);
            if(style.getFillPattern().equals(FillPatternType.NO_FILL) || style.getFillPattern().equals(FillPatternType.SOLID_FOREGROUND)){
                style.setFillForegroundColor(customColor);
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            }else{
                style.setFillBackgroundColor(customColor);
            }
        }
        if(borderStyle != null){
            style.setBorderTop(borderStyle);
            style.setBorderRight(borderStyle);
            style.setBorderBottom(borderStyle);
            style.setBorderLeft(borderStyle);
        }
        if(borders != null && borderStyle != null){
            style.setBorderTop(borders[0] ? borderStyle : BorderStyle.NONE);
            style.setBorderRight(borders[1] ? borderStyle : BorderStyle.NONE);
            style.setBorderBottom(borders[2] ? borderStyle : BorderStyle.NONE);
            style.setBorderLeft(borders[3] ? borderStyle : BorderStyle.NONE);
        }
        if(borderColorRGB != null){
            XSSFColor customColor = new XSSFColor(new byte[]{(byte)borderColorRGB[0], (byte)borderColorRGB[1], (byte)borderColorRGB[2]}, null);
            style.setTopBorderColor(customColor);
            style.setRightBorderColor(customColor);
            style.setBottomBorderColor(customColor);
            style.setLeftBorderColor(customColor);
        }

        style.setFont(font_);
        return style;
    }

    private CellRangeAddress applyStyle(XSSFSheet sheet, XSSFCell cell, XSSFCellStyle style) {
        cell.setCellStyle(style);
        int cellRowIndex = cell.getRowIndex();
        int cellColIndex = cell.getColumnIndex();
        CellRangeAddress foundRegion = null;
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
            if (mergedRegion.isInRange(cellRowIndex, cellColIndex)) {
                applyStyleToMergedRegion(sheet, mergedRegion, style);
                foundRegion = mergedRegion;
                break;
            }
        }
        if(width != null){
            setWidthSize(sheet, cellColIndex, Integer.parseInt(width));
        }
        if(height != null){
            setHeightSize(sheet, cellRowIndex, Short.parseShort(height));
        }
        return foundRegion;
    }

    private void applyStyleToMergedRegion(XSSFSheet sheet, CellRangeAddress region, XSSFCellStyle style) {
        int firstRow = region.getFirstRow();
        int lastRow = region.getLastRow();
        int firstCol = region.getFirstColumn();
        int lastCol = region.getLastColumn();

        for (int rowNum = firstRow; rowNum <= lastRow; rowNum++) {
            XSSFRow row = sheet.getRow(rowNum);
            if (row == null) continue;
            for (int colNum = firstCol; colNum <= lastCol; colNum++) {
                XSSFCell cell = row.getCell(colNum);
                if (cell == null) {
                    cell = row.createCell(colNum);
                }
                cell.setCellStyle(style);
            }
        }
    }

    private void setWidthSize(XSSFSheet sheet, int colIdx, int width) {
        sheet.setColumnWidth(colIdx, width * 256);
    }

    private void setHeightSize(XSSFSheet sheet, int rowIdx, short height) {
        Row row = sheet.getRow(rowIdx);
        if (row == null)
            row = sheet.createRow(rowIdx);
        row.setHeightInPoints(height);
    }

    public void setBorderStyle(String borderStyle){
        this.borderStyle = BorderStyle.valueOf(borderStyle);
    }

    public void setBorderColorRGB(String borderColorRGBString){
        borderColorRGBString = borderColorRGBString.replace("{", "").replace("}", "");
        String[] stringArray = borderColorRGBString.split(",\\s*");
        this.borderColorRGB = new int[3];
        for (int i = 0; i < stringArray.length; i++) {
            this.borderColorRGB[i] = Integer.parseInt(stringArray[i]);
        }
    }

    public void setBorders(String borders){
        borders = borders.replace("{", "").replace("}", "");
        String[] stringArray = borders.split(",\\s*");
        this.borders = new Boolean[4];
        for (int i = 0; i < stringArray.length; i++) {
            this.borders[i] = Boolean.parseBoolean(stringArray[i]);
        }
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public void setBackgroundColorRGB(String backgroundColorRGB){
        this.backgroundColorRGB = backgroundColorRGB;
    }

    private String getVal(String expression, Context context) {
        if (expression != null && expression.trim().length() > 0) {
            Object obj = context.evaluate(expression);
            if(obj != null && obj.toString().matches("^[\\{\\[]?\\d+,\\d+,\\d+[\\}\\]]?$")){
                return obj.toString();
            }else{
                return expression;
            }
        }
        return "";
    }


    private int[] formatToRGB(String val){
        String str = val.replace("{", "").replace("}", "");
        String[] stringArray = str.split(",\\s*");
        int[] bg = new int[3];
        for (int i = 0; i < stringArray.length; i++) {
            bg[i] = Integer.parseInt(stringArray[i]);
        }
        return bg;
    }
}