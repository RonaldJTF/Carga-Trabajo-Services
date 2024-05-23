package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.OrderBy;
import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.FtpDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.FtpEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.FtpService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FtpServiceImpl implements FtpService{
    private final FtpDAO ftpDAO;

    @Override
    @Transactional(readOnly = true)
    public FtpEntity findById(Long id) throws CiadtiException {
        return ftpDAO.findById(id).orElseThrow(() -> new CiadtiException("Ftp no encontrado para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FtpEntity> findAll() {
        return ftpDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public FtpEntity save(FtpEntity entity) {
        if (entity.getId() != null){
            entity.onUpdate();
            ftpDAO.update(
                    entity.getNombre(),
                    entity.getDescripcion(),
                    entity.getCodigo(),
                    entity.getActivo(),
                    entity.getFechaCambio(),
                    entity.getRegistradoPor(),
                    entity.getId());
            return  entity;
        }
        return ftpDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<FtpEntity> save(Collection<FtpEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = ftpDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<FtpEntity> findAllFilteredBy(FtpEntity filter) {
        OrderBy orderBy = new OrderBy("nombre", true);
        Specification<FtpEntity> specification = new SpecificationCiadti<>(filter, orderBy);
        return ftpDAO.findAll(specification);
    }

    @Override
    @Transactional(readOnly = true)
    public FtpEntity findActive() throws CiadtiException {
        return ftpDAO.findActive().orElseThrow(() -> new CiadtiException("No se ha encontrado ningún ftp activo", 500));
    }
}
