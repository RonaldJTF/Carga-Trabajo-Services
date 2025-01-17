package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service;

import java.util.List;
import java.util.Map;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.projections.InventarioTipologiaDTO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.TipologiaEntity;

public interface TipologiaService extends CommonService<TipologiaEntity>{
    TipologiaEntity findFirstTipology();
    List<InventarioTipologiaDTO> findInventarioTipologia();
    List<TipologiaEntity> findAllManagement();
    TipologiaEntity findDependencyTipology();
    List<TipologiaEntity> findAllWithNextTipologyFilteredBy(TipologiaEntity filter);
    TipologiaEntity findWithNextTipologyHierarchicallyById(Long id);
    Map<Long, Integer> findOrderOfTypologies();
}
