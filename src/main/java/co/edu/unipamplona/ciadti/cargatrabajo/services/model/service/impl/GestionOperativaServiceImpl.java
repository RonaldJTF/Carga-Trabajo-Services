package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.GestionOperativaDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.GestionOperativaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.GestionOperativaService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GestionOperativaServiceImpl implements GestionOperativaService{
    private final GestionOperativaDAO gestionOperativaDAO;

    @Override
    @Transactional(readOnly = true)
    public GestionOperativaEntity findById(Long id) throws CiadtiException {
        return gestionOperativaDAO.findById(id).orElseThrow(() -> new CiadtiException("GestionOperativa no encontrado para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GestionOperativaEntity> findAll() {
        return gestionOperativaDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public GestionOperativaEntity save(GestionOperativaEntity entity) {
        if (entity.getId() != null){
            entity.onUpdate();
            gestionOperativaDAO.update(
                entity.getIdPadre(),
                entity.getIdTipologia(),
                entity.getNombre(),
                entity.getDescripcion(),
                entity.getOrden(),
                entity.getFechaCambio(), 
                entity.getRegistradoPor(), 
                entity.getId());
            return entity;
        }
        return gestionOperativaDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<GestionOperativaEntity> save(Collection<GestionOperativaEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = gestionOperativaDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<GestionOperativaEntity> findAllFilteredBy(GestionOperativaEntity filter) {
        SpecificationCiadti<GestionOperativaEntity> specification = new SpecificationCiadti<GestionOperativaEntity>(filter);
        return gestionOperativaDAO.findAll(specification);
    }

    @Override
    @Transactional(readOnly = true)
    public Long findLastOrderByIdPadre(Long idPadre) {
        return gestionOperativaDAO.findLastOrderByIdPadre(idPadre);
    }

    @Override
    @Transactional(rollbackFor = {RuntimeException.class, Exception.class})
    public int updateOrdenByIdPadreAndOrdenMajorOrEqualAndNotId(Long idPadre, Long orden, Long id, int increment) {
        return gestionOperativaDAO.updateOrdenByIdPadreAndOrdenMajorOrEqualAndNotId(idPadre, orden, id, increment);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByIdPadreAndOrdenAndNotId(Long idPadre, Long orden, Long id) {
        return gestionOperativaDAO.existsByIdPadreAndOrdenAndNotId(idPadre, orden, id);
    }

    @Override
    @Transactional(rollbackFor = {RuntimeException.class, Exception.class})
    public int updateOrdenByIdPadreAndOrdenBeetwenAndNotId(Long idPadre, Long inferiorOrder, Long superiorOrder, Long id, int increment) {
        return gestionOperativaDAO.updateOrdenByIdPadreAndOrdenBeetwenAndNotId(idPadre, inferiorOrder, superiorOrder, id, increment);
    }
}
