package co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.poi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;

public class ReportPOI {
    XSSFWorkbook workbook;
    XSSFSheet sheet;
    private List<CellPOI> items = new ArrayList<>();
    private BlockPOI titleBlock;
    private boolean showInColumn;
    private Map<String, List<CellPOI>> styleRegistry_ = new HashMap<>();
    private Map<Style, XSSFCellStyle> cellStyles_ = new HashMap<>();
    private XSSFFont _subFont;

    public ReportPOI (){
        workbook = new XSSFWorkbook();
        createSheet("Default page");
    }

    public ReportPOI (String sheetName){
        workbook = new XSSFWorkbook();
        createSheet(sheetName);
    }

    public void createSheet(String sheetName){
        reset();
        sheet = workbook.createSheet(sheetName);
    }

    public Map<String, Position> addTitle(BlockPOI block){
        titleBlock = block;
        insertBlockStyle(block);
        block.extendProperty();
        return addCells(titleBlock, false);
    }

    public Map<String, Position> addBlock(BlockPOI block){
        insertBlockStyle(block);
        block.extendProperty();
        return addCells(block, true);
    }

    /** 
     * Genera el contenido en un arreglo de bytes del libro.
     * @return byte[]
     * @throws CiadtiException
     */
    public void builtSheetContent() throws CiadtiException{
        ajustTitle();
        assignValues();
    }

    /** 
     * Genera el contenido en un arreglo de bytes del libro.
     * @return byte[]
     * @throws CiadtiException
     */
    public byte[] generateBytes() throws CiadtiException{
        byte[] workbookBytes = null;
        ByteArrayOutputStream outputStream;
        try {
            outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            outputStream.close();
            workbookBytes = outputStream.toByteArray();
            workbook.close();
        } catch (IOException e) {
            throw new CiadtiException("Ha ocurrido un error al generar archivo", 500);
        }
        return workbookBytes;
    }

    private void reset(){
        items = new ArrayList<>();
        styleRegistry_ = new HashMap<>();
        System.out.println(cellStyles_.size());
        this.cellStyles_ = new HashMap<>();
    }

    /**
     *  Agrega / completa la infomación del objeto CellPOI, por ejemplo, donde se va a ubicar, cuantas celdas va a ocupar (por si hace merge con otras celdas),
     *  heredar estilos de las celdas padres, que a su vez ya han heredado los estilos del bloque.
     * @param block: Bloque con información de las celdas.
     * @param assignItemsToList: Establece si los elementos cellPOI deben ser insertados en el parametro items. Nota: esto se hace porque 
     *                           el bloque título debe ajustar su ancho o alto(de acuerdo a la orientación) en funcuón del resto de contenido.
     * @return Map<String, Position> con la relación de posición en la que puede ubicarse otro bloque ('right', 'bottom') 
     */
    private Map<String, Position> addCells(BlockPOI block, boolean assignItemsToList){
        showInColumn = block.isShowInColumn();
        List<CellPOI> cells = block.getItems();
        Position position = block.getPosition();
        assignUsedCells(cells);
        assignUsedRowsAndColumns(cells, 0, getDeep(cells));
        assignCoord(cells, position.getY(), position.getX());
        inheritStyleValues(cells, null);
        if(assignItemsToList){
            assignBuiltCells(cells);
        }
        return block.getNextPositions();
    }

    /**
     * Ajusta las celdas del bloque título (titleBlock) en función del ancho o alto ocupado por los bloques que conforman el cuerpo
     */
    private void ajustTitle(){
        int[] positionXiXfYiYf =  {Integer.MAX_VALUE, 0, Integer.MAX_VALUE, 0};
        for(CellPOI cell : items){
            if (cell.getColumnIndex() < positionXiXfYiYf[0]){
                positionXiXfYiYf[0] = cell.getColumnIndex();
            }
            if (cell.getColumnIndex() + cell.getUsedColumns() - 1 > positionXiXfYiYf[1]){
                positionXiXfYiYf[1] = cell.getColumnIndex() + cell.getUsedColumns() - 1;
            }
        }
        ajustCell(titleBlock.getItems(), (positionXiXfYiYf[1] - positionXiXfYiYf[0] + 1) - titleBlock.getItems().stream().map(CellPOI::getUsedColumns).reduce(0, Integer::sum));
        addCells(titleBlock, true);
    }

    /**
     * Ajusta la celda del bloque de título en función del ancho que debe ocupar el título en función del contenido en el cuerpo del reporte.
     * @param cells: Celdas contenidas.
     * @param remainingTotal: Define cuantas celdas debe ocupar una cellPOI para ajustarse en ancho o alto al contenido del bloque. Por ejemplo, 
     * en la representación gráfica se aprecia que la celda Item 1 debería hacer merge con otras dos celdas a su derecha para nivelarse con todo el bloque, 
     * así que para este caso, el atributo aditionalCellsToUse se debería establecer en 2.
     *  |-----------|-----------|-----------| 
        |  Item  1  |           |           |
        |-----------|-----------|-----------|
        |           | Item 2.1  |Item 2.1.1 |
        |  Item  2  |-----------|-----------|
        |           | Item 2.2  |Item 2.2.1 |
        |-----------|-----------|-----------|
     */
    private void ajustCell(List<CellPOI> cells, int remainingTotal){
        int newItemsPerCell = (int) Math.ceil(remainingTotal *1.0 / cells.stream().filter(e -> !(e.getValue() instanceof byte[])).toList().size());
        for (CellPOI cell : cells){
            if(!(cell.getValue() instanceof byte[])){
                if (remainingTotal - newItemsPerCell >= 0){
                    remainingTotal -= newItemsPerCell;
                    cell.setAditionalCellsToUse(newItemsPerCell);
                }else{
                    cell.setAditionalCellsToUse(remainingTotal);
                }
            }
            if (cell.getChildren() != null && cell.getChildren().size() > 0){
                ajustCell(cell.getChildren(), cell.getAditionalCellsToUse());
            }
        }
    }

    /**
     * Agrega al atributo 'items' las celdas a intertar en la hoja. Aquí las relaciones de padre e hijos han de ser puestas en una lista plana.
     * @param cells: Celdas asociadas.
     */
    private void assignBuiltCells(List<CellPOI> cells){
        for(CellPOI cell : cells){
            items.add(cell);
            if(cell.getChildren() != null && cell.getChildren().size() > 0){
                assignBuiltCells(cell.getChildren());
            }
        }
    }

    /**
     * Obtiene la profundida de un bloque de objetos CellPOI, esto con el propósito de saber cuantas filas o columnas 
     * (de acuerdo a la orientación del bloque) ocupa el bloque.
     * @param cells
     * @return int: número de celdas o culumnas ocupadas en la hoja de excel.
     */
    private int getDeep(List<CellPOI> cells){
        int rowNumber = 0;
        int max = 0;
        for(CellPOI cell : cells){
            rowNumber = goCountDeep(cell, 0);
            if (rowNumber > max){
                max = rowNumber;
            }
        }
        return max + 1;
    }

    private int goCountDeep(CellPOI cell, int counter){
        if (cell.getChildren() != null && cell.getChildren().size() > 0){
            counter += getDeep(cell.getChildren());
        }
        return counter;
    }

    /**
     * Define cuantas celdas en la hoja de excel debe ocupar el objeto CellPOI, esto en función de las celdas adicionales que deben agregarse
     * para que nivele con el bloque.
     * @param cells
     * @return int: número que debe usar, considerando que al menos se ocupa una celda.
     */
    private int assignUsedCells(List<CellPOI> cells){
        int n = 0;
        int aux = 0;
        for(CellPOI cell : cells){
            aux = goCountCells(cell);
            cell.setUsedCells_(aux + (cell.getAditionalCellsToUse() > 0 ? cell.getAditionalCellsToUse() : 0));
            n += aux;
        }
        return n;
    }

    private int goCountCells(CellPOI cell){
        if (cell.getChildren() != null && cell.getChildren().size() > 0){
            return assignUsedCells(cell.getChildren());
        }
        return 1;
    }

    /**
     * Hereda a los celdas hijas nos estilos que contiene el padre pero que no tiene definido la celda hija.
     * @param cells
     * @param parent
     */
    private void inheritStyleValues(List<CellPOI> cells, CellPOI parent){
        for (CellPOI cell : cells){
            if(parent != null){
                cell.completeStyles(parent.getStyle());
            }
            if (cell.getChildren() != null && cell.getChildren().size() > 0){
                inheritStyleValues(cell.getChildren(), cell);
            }
        }
    }

    /**
     * Asigna los valores de cuantas filas o columnas debe ocupar un objeto CellPOI en la hoja de excel. 
     * @param cells: Celdas asociadas
     * @param level: Hace referencia al nivel o profundidas en las celdas en las que estamos iterando, esto con respecto a las celdas padres iniciales.
     * @param blockDeep: Profundidad máxima o profundidad del bloque de objetos CellPOI. 
     */
    private void assignUsedRowsAndColumns(List<CellPOI> cells, int level, int blockDeep ){
        for(CellPOI cell : cells){
            if (cell.getChildren() != null && cell.getChildren().size() > 0){
                if(this.showInColumn){
                    cell.setUsedColumns(1);
                    cell.setUsedRows(cell.getUsedCells_());
                }else{
                    cell.setUsedRows(1);
                    cell.setUsedColumns(cell.getUsedCells_());
                }
                assignUsedRowsAndColumns(cell.getChildren(), level +1, blockDeep);
            }else{
                if(this.showInColumn){
                    cell.setUsedColumns(blockDeep - level);
                    cell.setUsedRows(cell.getUsedCells_());
                }else{
                    cell.setUsedRows(blockDeep - level);
                    cell.setUsedColumns(cell.getUsedCells_());
                }
            }
        }
    }

    /**
     * Calcula y asigna la posición en la que debe encontrarse una celda.
     * @param cells
     * @param rowIndex
     * @param colunmIndex
     */
    private void assignCoord(List<CellPOI> cells, int rowIndex, int colunmIndex){
        CellPOI cellPrevius = CellPOI.builder().rowIndex(0).columnIndex(0).usedRows(0).usedColumns(0).build();
        for(int i = 0; i < cells.size(); i++){
            CellPOI cell = cells.get(i);
            if (i > 0){
                if(this.showInColumn){rowIndex=0;}else{colunmIndex = 0;}
                cellPrevius = cells.get(i - 1);
            }
            if(this.showInColumn){
                cell.setColumnIndex(colunmIndex);
                cell.setRowIndex(rowIndex + cellPrevius.getRowIndex() + cellPrevius.getUsedRows());
            }else{
                cell.setColumnIndex(colunmIndex + cellPrevius.getColumnIndex() + cellPrevius.getUsedColumns());
                cell.setRowIndex(rowIndex);
            }
            if(cell.getChildren() != null && cell.getChildren().size() > 0){
                if (this.showInColumn){
                    assignCoord(cell.getChildren(),  cell.getRowIndex(), colunmIndex + 1);
                }else{
                    assignCoord(cell.getChildren(), rowIndex + 1, cell.getColumnIndex());
                }
            }
        }
    }
   
    /**
     * Asigna el valor o contenido a cada celda definida en el libro.
     */
    private void assignValues(){
        Collections.sort(items, Comparator.comparingInt(CellPOI::getRowIndex));
        this._subFont = workbook.createFont();;
        this._subFont.setColor(IndexedColors.GREY_40_PERCENT.getIndex()); 
        this._subFont.setBold(false);
        this._subFont.setItalic(true);

        for (CellPOI cell : items){
            int col1 = cell.getColumnIndex();
            int col2 = cell.getColumnIndex() + cell.getUsedColumns() - 1;
            int row1 = cell.getRowIndex();
            int row2 = cell.getRowIndex() + cell.getUsedRows() - 1;

            /*Se define si queremos extender o hacer merge con las celdas vecinas, esto para ajustarla con la misma profundidad del bloque, 
              Nota: Se entiende por extender cuando la celda no tiene celdas hijas pero se quiere ajustar en filas o en colunma, por ejemplo, la celta Item 1.
              CELDA NO EXTENDIDA
                |-----------|-----------|-----------| 
                |  Item  1  |           |           |
                |-----------|-----------|-----------|
                |           | Item 2.1  |Item 2.1.1 |
                |  Item  2  |-----------|-----------|
                |           | Item 2.2  |Item 2.2.1 |
                |-----------|-----------|-----------|
              CELDA EXTENDIDA
                |-----------------------------------|
                |  Item  1                          |
                |-----------|-----------|-----------|
                |           | Item 2.1  |Item 2.1.1 |
                |  Item  2  |-----------|-----------|
                |           | Item 2.2  |Item 2.2.1 |
                |-----------|-----------|-----------|
            */
            CellRangeAddress region = null;
            if (!cell.isNoExtend_()){
                region = mergedCells( row1, row2, col1, col2);
            }else{
                if (cell.isShowInColumn_()){
                    region = mergedCells( row1, row2, col1, col1);
                }else{
                    region = mergedCells( row1, row1, col1, col2);
                }
            }

            /*Aplica los estilos de la celda actal a las celdas con las que hace merge, si alguna de esas celdas con las que se hace merge está en otra fila, entonces se guarda
             * la referencia de la fila en la que no se aplica el estilo en el atributo 'styleRegistry' de la clase, para ser aplicado cuando pasamos por la fila correspondiente.
             * Nota: Todas las inserciones en una fila deben hacerse de una vez para todas las colunmas, ya que no podemos escribir en una fila X y pasar a la fila Y y luego querer volver
             * a escribir en la fila X.
            */
            for (int row=row1; row<=row2; row++){
                for(int col=col1; col<=col2; col++){
                    if (row > row1){
                        if(styleRegistry_.get(String.valueOf(row)) == null){
                            styleRegistry_.put(String.valueOf(row), new ArrayList<>());
                        }    
                        styleRegistry_.get(String.valueOf(row)).add(cell);
                    }else{
                        insertStyle(row, col, cell.getStyle());
                    }
                }
            }   

            /*Insertar los estilos para las columnas de la fila que no se han insertado porque no era la fila actual*/
            List<CellPOI> list = styleRegistry_.get(String.valueOf(row1));
            if(list != null){
                for (CellPOI cell_ : list){
                    int col1_ = cell_.getColumnIndex();
                    int col2_ = cell_.getColumnIndex() + cell_.getUsedColumns() - 1;
                    for(int col=col1_; col<=col2_; col++){
                        insertStyle(row1, col, cell_.getStyle());
                    }
                }
            }

            if (cell.getValue() == null){
                insertValueToCell(cell.getRowIndex(), cell.getColumnIndex(), "");
            }else if (cell.getValue() instanceof String ){
                insertValueToCell(cell.getRowIndex(), cell.getColumnIndex(), (String)cell.getValue(), (String)cell.getSubValue(), region);
            }else if (cell.getValue()  instanceof byte[]) {
                int inputImage = workbook.addPicture((byte[])cell.getValue() , Workbook.PICTURE_TYPE_PNG);
                XSSFDrawing  drawing = (XSSFDrawing) sheet.createDrawingPatriarch();
                XSSFClientAnchor imageAnchor = new XSSFClientAnchor();

                CellRangeAddress mergedRegion = new CellRangeAddress(cell.getRowIndex(), 
                cell.getRowIndex() + cell.getUsedRows() - 1,
                cell.getColumnIndex(),
                cell.getColumnIndex() + cell.getUsedColumns() - 1);

                imageAnchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
                imageAnchor.setCol1(cell.getColumnIndex());
                imageAnchor.setCol2(cell.getColumnIndex() + 1);
                imageAnchor.setRow1(mergedRegion.getFirstRow());
                imageAnchor.setRow2(mergedRegion.getLastRow() + 1);
                drawing.createPicture(imageAnchor, inputImage);
            }else{
                if (cell.getSubValue() != null){
                    insertValueToCell(cell.getRowIndex(), cell.getColumnIndex(), cell.getValue().toString(), cell.getSubValue().toString(), region);
                }else{
                    insertValueToCell(cell.getRowIndex(), cell.getColumnIndex(), cell.getValue().toString());
                }
            }
        }
    }

    /**
     * Inserta los estilos a una celda en la posición X y Y.
     * @param rowIdx
     * @param colIdx
     * @param style
     */
    private void insertStyle(int rowIdx, int colIdx, Style style) {
        if (style != null){
            XSSFRow row = getOrCreateRow(rowIdx);
		    XSSFCell cell = getOrCreateCell(row, colIdx);
           
            XSSFCellStyle xSSFCellStyle = (XSSFCellStyle)this.cellStyles_.get(style);
            if (xSSFCellStyle == null) {
                style.setColIdx_(colIdx);
                style.setRowIdx_(rowIdx);
                xSSFCellStyle = style.catchCellStyle(this.workbook, this.sheet);
                this.cellStyles_.put(style, xSSFCellStyle);
            }else{
                if(style.getWidth() > 0){
                    this.sheet.setColumnWidth(colIdx, style.getWidth() * 256);
                }
                if (style.getHeight() > 0){
                    row.setHeightInPoints(style.getHeight());
                }
            }
            cell.setCellStyle(xSSFCellStyle);
        }
    }

    
    /**
     * Hereda o inserta los estilos del bloque a los elementos CellPOI, y estos a los elementos hijos en cascada.
     * @param block
     */
    private void insertBlockStyle(BlockPOI block) {
        if (block != null){
           for (CellPOI e : block.getItems()) {
                e.completeStyles(block.getStyle());
           }
        }
    }

    private void insertValueToCell(int rowIdx, int colIdx, String value) {
		XSSFRow row = getOrCreateRow(rowIdx);
		XSSFCell cell = getOrCreateCell(row, colIdx);
		cell.setCellValue(value);
	}

    private void insertValueToCell(int rowIdx, int colIdx, String text1, String text2, CellRangeAddress region) {
		XSSFRow row = getOrCreateRow(rowIdx);
		XSSFCell cell = getOrCreateCell(row, colIdx);

        text1 = text1 != null ? text1 : "";
        text2 = text2 != null && !text2.isBlank() ? text2 : "";

       
        //cell.getCellStyle().setWrapText(true);

        XSSFRichTextString richText = new XSSFRichTextString(text1 + (text2.length() > 0 ? "\n" + text2 : " "));
        richText.applyFont(text1.length(), text1.length() + text2.length() + 1, this._subFont);
        cell.setCellValue(richText);

        /*int maxCharsPerLine = this.sheet.getColumnWidth(cell.getColumnIndex())/ 230; //256;
        int numLinesText1 = (int) Math.ceil((double) (text1).length() / maxCharsPerLine);
        int numLinesText2 = (int) Math.ceil((double) (text2).length() / maxCharsPerLine);
        int numLines = numLinesText1 + numLinesText2;*/
        row.setHeight((short)-1);
    }
	
	private CellRangeAddress mergedCells(int row1, int row2, int col1, int col2) {
        CellRangeAddress region = null;
        if (col2 - col1 > 0 || row2 - row1 > 0){
            region = new CellRangeAddress(row1, row2, col1, col2);
            this.sheet.addMergedRegion(region);
        }
        return region;
	}

	private XSSFCell getOrCreateCell(XSSFRow row, int colIdx) {
		XSSFCell cell = row.getCell(colIdx);
		if (cell == null) {
			cell = row.createCell(colIdx);
		}
		return cell;
	}

	private XSSFRow getOrCreateRow(int rowIdx) {
		XSSFRow row = this.sheet.getRow(rowIdx);
		if (row == null) {
			row = sheet.createRow(rowIdx);
		}
		return row;
	}
}
