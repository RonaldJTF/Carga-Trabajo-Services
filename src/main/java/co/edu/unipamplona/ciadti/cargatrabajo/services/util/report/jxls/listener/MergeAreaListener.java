package co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressBase;
import org.jxls.common.AreaListener;
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.transform.Transformer;
import org.jxls.transform.poi.PoiTransformer;

@Slf4j
public class MergeAreaListener implements AreaListener {

    private final CellRef commandCell;

    private final Sheet sheet;

    private CellRef lastRowCellRef;

    public MergeAreaListener(Transformer transformer, CellRef cellRef) {
        this.commandCell = cellRef;
        this.sheet = ((PoiTransformer) transformer).getXSSFWorkbook().getSheet(cellRef.getSheetName());
    }

    /*public MergeAreaListener(Transformer transformer, CellRef cellRef, int colsToMerge, int rowsToMerge) {
        this.commandCell = cellRef;
        this.sheet = ((PoiTransformer) transformer).getXSSFWorkbook().getSheet(cellRef.getSheetName());
    }*/

    @Override
    public void afterApplyAtCell(CellRef cellRef, Context context) {
        // child cell
        if (commandCell.getCol() != cellRef.getCol()) {
            this.setLastRowCellRef(cellRef);
        } else {
            if (existMerged(cellRef)) {
                return;
            }
            merge(cellRef);
        }
    }

    private void merge(CellRef cellRef) {
        if(this.lastRowCellRef == null) return;

        int from = cellRef.getRow();

        int lastRow = sheet.getMergedRegions().stream()
                .filter(address -> address.isInRange(this.lastRowCellRef.getRow(), this.lastRowCellRef.getCol()))
                .mapToInt(CellRangeAddressBase::getLastRow).findFirst().orElse(this.lastRowCellRef.getRow());
        if(lastRow - from > 0){
            CellRangeAddress region = new CellRangeAddress(from, lastRow, cellRef.getCol(), cellRef.getCol());
            sheet.addMergedRegion(region);
            applyStyle(sheet.getRow(cellRef.getRow()).getCell(cellRef.getCol()), region);
        }
    }

    private void setLastRowCellRef(CellRef cellRef) {
        if (this.lastRowCellRef == null || this.lastRowCellRef.getRow() < cellRef.getRow()) {
            this.lastRowCellRef = cellRef;
        }
    }

    private void applyStyle(Cell cell, CellRangeAddress region) {
        CellStyle cellStyle = cell.getCellStyle();
        for (int i = region.getFirstRow(); i <= region.getLastRow(); i++) {
            Row r = this.sheet.getRow(i);
            if (r == null) {
                r = this.sheet.createRow(i);
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

    @Override
    public void beforeApplyAtCell(CellRef cellRef, Context context) {
    }

    @Override
    public void beforeTransformCell(CellRef srcCell, CellRef targetCell, Context context) {
    }

    @Override
    public void afterTransformCell(CellRef srcCell, CellRef targetCell, Context context) {
    }

    private boolean existMerged(CellRef cell) {
        return sheet.getMergedRegions().stream()
                .anyMatch(address -> address.isInRange(cell.getRow(), cell.getCol()));
    }

}