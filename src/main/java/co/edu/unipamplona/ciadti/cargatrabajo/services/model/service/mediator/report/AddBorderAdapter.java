package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.report;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jxls.area.XlsArea;
import org.jxls.command.CellDataUpdater;
import org.jxls.common.AreaListener;
import org.jxls.common.CellData;
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.transform.poi.PoiTransformer;
import org.springframework.beans.factory.annotation.Autowired;


public class AddBorderAdapter implements AreaListener {

    XlsArea area;
    PoiTransformer transformer;
    
    public AddBorderAdapter(XlsArea area) {
        this.area = area;
        transformer = (PoiTransformer) area.getTransformer();
    }

    @Override
    public void afterApplyAtCell(CellRef arg0, Context arg1) {
    }

    @Override
    public void afterTransformCell(CellRef srcCell, CellRef targetCell, Context context) {
        highlightBonus(targetCell);
    }

    @Override
    public void beforeApplyAtCell(CellRef arg0, Context arg1) {
    }

    @Override
    public void beforeTransformCell(CellRef arg0, CellRef arg1, Context arg2) {
    }


    private void highlightBonus(CellRef cellRef) {
        Workbook workbook = transformer.getWorkbook();
        Sheet sheet = workbook.getSheet(cellRef.getSheetName());
        Cell cell = sheet.getRow(cellRef.getRow()).getCell(cellRef.getCol());
        CellStyle cellStyle = cell.getCellStyle();
        CellStyle newCellStyle = workbook.createCellStyle();
        newCellStyle.setDataFormat( cellStyle.getDataFormat() );
        newCellStyle.setFont( workbook.getFontAt( cellStyle.getFontIndex() ));
        newCellStyle.setFillBackgroundColor( cellStyle.getFillBackgroundColor());
        newCellStyle.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
        newCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cell.setCellStyle(newCellStyle);
    }
    
}
