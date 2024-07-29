package co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.poi;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Style implements Cloneable{
    private FillPatternType patternType;
    private HorizontalAlignment horizontalAlignment;
    private VerticalAlignment verticalAlignment;
    private int width;
    private short height;
    private Boolean bold;
    private Boolean italic;
    private Boolean underline;
    private int fontSize;
    private String font;
    private IndexedColors color;
    private IndexedColors backgroundColor;
    private int[] colorRGB; 
    private int[] backgroundColorRGB;
    private BorderStyle borderTop;
    private BorderStyle borderRight;
    private BorderStyle borderBottom;
    private BorderStyle borderLeft;
    private Boolean[] borders;
    private BorderStyle borderStyle;
    private int[] borderColorRGB;
    private Boolean wrapText;
    private Short rotation;
    private int rowIdx_;
    private int colIdx_;

    public XSSFCellStyle catchCellStyle(SXSSFWorkbook workbook, SXSSFSheet sheet){
        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
        XSSFFont font_ = (XSSFFont) workbook.createFont();
        if (backgroundColor != null){
            style.setFillForegroundColor(backgroundColor.getIndex());
        }
        if(backgroundColorRGB != null){
            XSSFColor customColor = new XSSFColor(new byte[]{(byte)backgroundColorRGB[0], (byte)backgroundColorRGB[1], (byte)backgroundColorRGB[2]}, null);
            style.setFillForegroundColor(customColor);
        }
        if (patternType != null){
            style.setFillPattern(patternType);
        }
        if (horizontalAlignment != null){
            style.setAlignment(horizontalAlignment);
        }
        if (verticalAlignment != null){
            style.setVerticalAlignment(verticalAlignment);
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
        if(borderTop != null){
            style.setBorderTop(borderTop);
        }
        if(borderRight != null){
            style.setBorderRight(borderRight);
        }
        if(borderBottom != null){
            style.setBorderBottom(borderBottom);
        }
        if(borderLeft != null){
            style.setBorderLeft(borderLeft);
        }
        if(borderColorRGB != null){
            XSSFColor customColor = new XSSFColor(new byte[]{(byte)borderColorRGB[0], (byte)borderColorRGB[1], (byte)borderColorRGB[2]}, null);
            style.setTopBorderColor(customColor);
            style.setRightBorderColor(customColor);
            style.setBottomBorderColor(customColor);
            style.setLeftBorderColor(customColor);
        }
        if(rotation != null){
            style.setRotation(rotation);
        }
        if(wrapText != null){
            style.setWrapText(wrapText);
        }
        if(width > 0){
            setWidthSize(sheet, width);
        }
        if(height > 0){
            setHeightSize(sheet, height);
        }
        if (fontSize > 0){
            font_.setFontHeightInPoints((short) fontSize); 
        }
        if (font != null){
            font_.setFontName(font);
        }
        if(color != null){
            font_.setColor(color.getIndex());
        }
        if(colorRGB != null){
            XSSFColor customColor = new XSSFColor(new byte[]{(byte)colorRGB[0], (byte)colorRGB[1], (byte)colorRGB[2]}, null);
            font_.setColor(customColor);
        }
        if(bold != null){
            font_.setBold(bold);
        }
        if(italic != null){
            font_.setItalic(italic);
        }
        if(underline != null){
            font_.setItalic(underline);
        }

        style.setFont(font_);
        return style;
    }

    private void setWidthSize(SXSSFSheet sheet, int width) {
        sheet.setColumnWidth(colIdx_, width * 256);
    }

    private void setHeightSize(SXSSFSheet sheet, short height) {
        Row row = sheet.getRow(rowIdx_);
        if (row == null)
            row = sheet.createRow(rowIdx_);
        row.setHeightInPoints(height);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
