package co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.command;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jxls.area.Area;
import org.jxls.command.AbstractCommand;
import org.jxls.command.Command;
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.common.Size;
import org.jxls.transform.poi.PoiTransformer;

public class GroupRowCommand extends AbstractCommand {
    private Area area;
    private String collapseIf;

    @Override
    public String getName() {
        return "groupRow";
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
        int startRow = cellRef.getRow();
        int endRow = cellRef.getRow() + resultSize.getHeight() - 1;
        sheet.groupRow(startRow, endRow);
        if (collapseIf != null && collapseIf.trim().length() > 0) {
            sheet.setRowGroupCollapsed(startRow, context.isConditionTrue(collapseIf));
        }
        return resultSize;
    }

    @Override
    public Command addArea(Area area) {
        super.addArea(area);
        this.area = area;
        return this;
    }

    public void setCollapseIf(String collapseIf) {
        this.collapseIf = collapseIf;
    }
}