package co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.command;

import org.apache.poi.ss.usermodel.IndexedColors;
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
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.common.Size;
import org.jxls.transform.poi.PoiTransformer;

public class HeaderCommand extends AbstractCommand {
    public static final String COMMAND_NAME = "header";
    private String tittle;
    private String subtittle;

    private Area area;
    private XSSFFont _subFont;

    public void setTittle(String tittle) {
        this.tittle = tittle;
    }

    public void setSubtittle(String subtittle) {
        this.subtittle = subtittle;
    }

    @Override
    public Size applyAt(CellRef cellRef, Context context) {
        if (area == null) {
            throw new IllegalArgumentException("No area is defined for header command");
        }

        Size resultSize = area.applyAt(cellRef, context);
        if (resultSize.equals(Size.ZERO_SIZE)) {
            return resultSize;
        }

        PoiTransformer transformer = (PoiTransformer) area.getTransformer();
        XSSFWorkbook workbook = (XSSFWorkbook) transformer.getWorkbook();
        XSSFSheet sheet = workbook.getSheet(cellRef.getSheetName()); 
        XSSFRow row =  sheet.getRow(cellRef.getRow()) != null ? sheet.getRow(cellRef.getRow()) : sheet.createRow(cellRef.getRow());
        XSSFCell cell = row.getCell(cellRef.getCol());

        if (this._subFont == null){
            this._subFont = workbook.createFont();
            this._subFont.setColor(IndexedColors.GREY_40_PERCENT.getIndex()); 
            this._subFont.setBold(false);
            this._subFont.setItalic(true);
            this._subFont.setFontName(row.getRowStyle().getFont() != null ? row.getRowStyle().getFont().getFontName(): null);
        }

        String tittle = this.getVal(this.tittle, context);
        String subtittle = this.getVal(this.subtittle, context);
        CellRangeAddress region = getRegion(sheet, cell);
        buildContent(sheet, cellRef, region, tittle, subtittle);
        return area.getSize();
    }

    @Override
    public Command addArea(Area area) {
        if (areaList.size() >= 1) {
            throw new IllegalArgumentException("You can only add 1 area to 'header' command!");
        }
        this.area = area;
        return super.addArea(area);
    }

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    private CellRangeAddress getRegion(XSSFSheet sheet, XSSFCell cell) {
        int cellRowIndex = cell.getRowIndex();
        int cellColIndex = cell.getColumnIndex();
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
            if (mergedRegion.isInRange(cellRowIndex, cellColIndex)) {
                return mergedRegion;
            }
        }
        return null;
    }

    private String getVal(String expression, Context context) {
        if (expression != null && expression.trim().length() > 0) {
            Object obj = context.evaluate(expression);
            if (obj != null) return obj.toString();
        }
        return "";
    }

    private void buildContent(XSSFSheet sheet, CellRef cellRef, CellRangeAddress region, String text1 , String text2) {
        XSSFRow row = sheet.getRow(cellRef.getRow());
        XSSFCell cell = row.getCell(cellRef.getCol());

        text1 = text1 != null ? text1 : "";
        text2 = text2 != null && !text2.isBlank() ? text2 : "";

        XSSFRichTextString richText = new XSSFRichTextString(text1 + (text2.length() > 0 ? "\n" + text2 : " "));
        richText.applyFont(text1.length(), text1.length() + text2.length() + 1, _subFont);
        cell.setCellValue(richText);
    }
}
