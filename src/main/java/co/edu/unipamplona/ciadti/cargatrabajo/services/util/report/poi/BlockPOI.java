package co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.poi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BlockPOI {
    private List<CellPOI> items;
    private Position position;
    private boolean showInColumn;
    @Builder.Default
    private Map<String, Position> nextPositions = new HashMap<>();
    private Style style;
    private boolean noExtend;
    
    private void getPositionsToNextInsert(List<CellPOI> cells, int[] positionXiXfYiYf){
        for(CellPOI cell : cells){
            if (cell.getColumnIndex() < positionXiXfYiYf[0]){
                positionXiXfYiYf[0] = cell.getColumnIndex();
            }
            if (cell.getColumnIndex() + cell.getUsedColumns() - 1 > positionXiXfYiYf[1]){
                positionXiXfYiYf[1] = cell.getColumnIndex() + cell.getUsedColumns() - 1;
            }
            if (cell.getRowIndex() < positionXiXfYiYf[2]){
                positionXiXfYiYf[2] = cell.getRowIndex();
            }
            if (cell.getRowIndex() + cell.getUsedRows() - 1 > positionXiXfYiYf[3]){
                positionXiXfYiYf[3] = cell.getRowIndex() + cell.getUsedRows() - 1;
            }
            if(cell.getChildren() != null){
                getPositionsToNextInsert(cell.getChildren(), positionXiXfYiYf);
            }
        }
    }

    public Map<String, Position> getNextPositions(){
        int [] positionXiXfYiYf = {Integer.MAX_VALUE, 0, Integer.MAX_VALUE, 0};
        getPositionsToNextInsert(items, positionXiXfYiYf);
        nextPositions.put("right", Position.builder().x(positionXiXfYiYf[1] + 1).y(positionXiXfYiYf[2]).build());
        nextPositions.put("bottom", Position.builder().x(positionXiXfYiYf[0]).y(positionXiXfYiYf[3] + 1).build());
        nextPositions.put("top-right", Position.builder().x(positionXiXfYiYf[1]).y(positionXiXfYiYf[2] - 1).build());
        nextPositions.put("bottom-right", Position.builder().x(positionXiXfYiYf[1]).y(positionXiXfYiYf[3] + 1).build());
        return nextPositions;
    }

    public void extendProperty(){
        extendProperty(items);
    }

    private void extendProperty(List<CellPOI> items){
        for (CellPOI cell : items){
            cell.setNoExtend_(noExtend);
            cell.setShowInColumn_(showInColumn);
            if (cell.getChildren() != null){
                extendProperty(cell.getChildren());
            }
        }
    }

}
