package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.CargoDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.CompensacionLabNivelVigValorDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.AlcanceEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CargoEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CompensacionLabNivelVigValorEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CompensacionLabNivelVigenciaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CompensacionLaboralEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.EscalaSalarialEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.EstructuraEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.NivelEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.NormatividadEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.VigenciaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.CargoService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CargoServiceImpl implements CargoService{
    @PersistenceContext
    private EntityManager entityManager;

    private final CargoDAO cargoDAO;

    @Override
    @Transactional(readOnly = true)
    public CargoEntity findById(Long id) throws CiadtiException {
        return cargoDAO.findById(id).orElseThrow(() -> new CiadtiException("Actividad no encontrada para el id :: " + id, 404));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CargoEntity> findAll() {
        return cargoDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public CargoEntity save(CargoEntity entity) {
        if (entity.getId() != null) {
            entity.onUpdate();
            cargoDAO.update(
                entity.getAsignacionBasica(), 
                entity.getTotalCargos(), 
                entity.getIdEstructura(), 
                entity.getIdNivel(), 
                entity.getIdNormatividad(), 
                entity.getIdEscalaSalarial(), 
                entity.getIdAlcance(), 
                entity.getIdVigencia(), 
                entity.getFechaCambio(), 
                entity.getRegistradoPor(), 
                entity.getId());
            return entity;
        }
        return cargoDAO.save(entity);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public List<CargoEntity> save(Collection<CargoEntity> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteByProcedure(Long id, String register) {
        Integer rows = cargoDAO.deleteByProcedure(id, register);
        if (1 != rows) {
            throw new RuntimeException( "Se han afectado " + rows + " filas." );
        }
    }

    @Override
    public List<CargoEntity> findAllFilteredBy(CargoEntity filter) {
        SpecificationCiadti<CargoEntity> specification = new SpecificationCiadti<CargoEntity>(filter);
        return cargoDAO.findAll(specification);
    }

    @SuppressWarnings("null")
    @Override
    @Transactional(readOnly = true)
    public List<CargoEntity> findAllBy(Map<String, Long[]> filter) {
        Long[] structureIds = filter.get("dependencies");
        Long[] validitiesIds = filter.get("validities");

        String jpql= " select c.id, c.asignacionBasica, c.totalCargos, c.idEstructura, c.idVigencia, c.idAlcance, c.idNormatividad, c.idNivel, c.idEscalaSalarial, " + 
                " e.nombre, e.icono, e.mimetype, e.idPadre, " +
                " v.nombre, v.anio, v.estado, " + 
                " a.nombre, " +
                " n.nombre, " +
                " ni.descripcion, " +
                " es.nombre, " +
                " clnv.id, clnv.idVigencia, clnv.idNivel, clnv.idEscalaSalarial, clnv.idCompensacionLaboral, " +
                " cl.nombre, " +
                " cnvv.id, cnvv.idCompensacionLabNivelVigencia, cnvv.idRegla, cnvv.idVariable " + 
                " from CargoEntity c  " +
                " inner join EstructuraEntity e on (c.idEstructura = e.id)    " +
                " inner join VigenciaEntity v on (c.idVigencia = v.id)      " +
                " inner join AlcanceEntity a on (c.idAlcance = a.id)       " +
                " inner join NormatividadEntity n on (c.idNormatividad = n.id)  " +
                " inner join NivelEntity ni on (c.idNivel = ni.id)       " +
                " left outer join EscalaSalarialEntity es on (c.idEscalaSalarial = es.id) " +
                " left outer join CompensacionLabNivelVigenciaEntity clnv on (v.id = clnv.idVigencia and ni.id = clnv.idNivel and (es.id = clnv.idEscalaSalarial OR clnv.idEscalaSalarial IS NULL)) " +
                " left outer join CompensacionLabNivelVigValorEntity cnvv on (clnv.id = cnvv.idCompensacionLabNivelVigencia) " +
                " left outer join CompensacionLaboralEntity cl on (clnv.idCompensacionLaboral = cl.id )   " +
                " where 2 > 1  ";

        Map<String, Object> parameters = new HashMap<>();

        if (validitiesIds != null && validitiesIds.length > 0){
            jpql += "AND c.idVigencia IN :validitiesIds ";
            parameters.put("validitiesIds", Arrays.asList(validitiesIds));
        }
        if (structureIds != null && structureIds.length > 0){
            jpql += "AND c.idEstructura IN :structureIds ";
            parameters.put("structureIds", Arrays.asList(structureIds));
        }

        jpql += "order by c.idEstructura asc, c.idVigencia asc, c.idAlcance asc, c.idNivel asc, clnv.id asc ";

        Query query = entityManager.createQuery(jpql);

        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        } 

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        List<CargoEntity> appointments = new ArrayList<>();
        Long appointmentId = -1L;
        Long clnvId = -1L;
        CargoEntity appointment = null;
        CompensacionLabNivelVigenciaEntity compensacionLabNivelVigencia = null;

        for (Object[] row : results) {
            if (((Long) row[0]) != appointmentId){
                appointment = CargoEntity.builder()
                    .id((Long) row[0])
                    .asignacionBasica((Double) row[1])
                    .totalCargos((Integer) row[2])
                    .idEstructura((Long) row[3])
                    .idVigencia((Long) row[4])
                    .idAlcance((Long) row[5])
                    .idNormatividad((Long) row[6])
                    .idNivel((Long) row[7])
                    .idEscalaSalarial((Long) row[8])
                    .estructura(EstructuraEntity.builder()
                        .id((Long) row[3])
                        .nombre((String) row[9])
                        .icono((byte[]) row[10])
                        .mimetype((String)row[11])
                        .idPadre((Long) row[12])
                        .build())
                    .vigencia(VigenciaEntity.builder()
                        .id((Long) row[4])
                        .nombre((String) row[13])
                        .anio((String)row[14])
                        .estado((String)row[15])
                        .build())
                    .alcance(AlcanceEntity.builder()
                        .id((Long) row[5])
                        .nombre((String) row[16])
                        .build())
                    .normatividad(NormatividadEntity.builder()
                        .id((Long) row[6])
                        .nombre((String) row[17])
                        .build())
                    .nivel(NivelEntity.builder()
                        .id((Long) row[7])
                        .descripcion((String) row[18])
                        .build())
                    .escalaSalarial(EscalaSalarialEntity.builder()
                        .id((Long) row[8])
                        .nombre((String) row[19])
                        .build())
                    .compensacionesLaboralesAplicadas(new ArrayList<>())
                    .build();
                appointments.add(appointment);
                appointmentId = (Long) row[0];
            }

            if(row[20] != null){
                if((Long) row[20] != clnvId){
                    compensacionLabNivelVigencia = CompensacionLabNivelVigenciaEntity.builder()
                        .id((Long) row[20])
                        .idVigencia((Long) row[21])
                        .idNivel((Long) row[22])
                        .idEscalaSalarial((Long) row[23])
                        .idCompensacionLaboral((Long) row[24])
                        .compensacionLaboral(CompensacionLaboralEntity.builder()
                            .id((Long) row[24])
                            .nombre((String) row[25])
                            .build())
                        .valoresCompensacionLabNivelVigencia(new ArrayList<>())
                        .build();
                    appointment.getCompensacionesLaboralesAplicadas().add(compensacionLabNivelVigencia);
                    clnvId = (Long) row[20];
                }
                if(row[26] != null){
                    CompensacionLabNivelVigValorEntity cnvv = CompensacionLabNivelVigValorEntity.builder()
                        .id((Long) row[26])
                        .idCompensacionLabNivelVigencia((Long) row[27])
                        .idRegla((Long) row[28])
                        .idVariable((Long) row[29])
                        .build();
                    compensacionLabNivelVigencia.getValoresCompensacionLabNivelVigencia().add(cnvv);
                }
                
            }
           
        }
        return appointments;
    }

    @Override
    @Transactional(readOnly = true)
    public CargoEntity findByAppointmentId(Long id) {
        String jpql= " select c.id, c.asignacionBasica, c.totalCargos, c.idEstructura, c.idVigencia, c.idAlcance, c.idNormatividad, c.idNivel, c.idEscalaSalarial, " + 
                " e.nombre, e.icono, e.mimetype, e.idPadre, " +
                " v.nombre, v.anio, v.estado, " + 
                " a.nombre, " +
                " n, " +
                " ni.descripcion, " +
                " es.nombre " +
                " from CargoEntity c  " +
                " inner join EstructuraEntity e on (c.idEstructura = e.id)    " +
                " inner join VigenciaEntity v on (c.idVigencia = v.id)      " +
                " inner join AlcanceEntity a on (c.idAlcance = a.id)       " +
                " inner join NormatividadEntity n on (c.idNormatividad = n.id)  " +
                " inner join NivelEntity ni on (c.idNivel = ni.id)       " +
                " left outer join EscalaSalarialEntity es on (c.idEscalaSalarial = es.id) " +
                " where 2 > 1  ";

        Map<String, Object> parameters = new HashMap<>();

        if (id != null){
            jpql += "AND c.id = :id ";
            parameters.put("id", id);
        }

        jpql += "order by c.idEstructura asc, c.idVigencia asc, c.idAlcance asc, c.idNivel asc ";

        Query query = entityManager.createQuery(jpql);

        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        } 

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        CargoEntity appointment = null;

        for (Object[] row : results) {
            appointment = CargoEntity.builder()
                .id((Long) row[0])
                .asignacionBasica((Double) row[1])
                .totalCargos((Integer) row[2])
                .idEstructura((Long) row[3])
                .idVigencia((Long) row[4])
                .idAlcance((Long) row[5])
                .idNormatividad((Long) row[6])
                .idNivel((Long) row[7])
                .idEscalaSalarial((Long) row[8])
                .estructura(EstructuraEntity.builder()
                    .id((Long) row[3])
                    .nombre((String) row[9])
                    .icono((byte[]) row[10])
                    .mimetype((String)row[11])
                    .idPadre((Long) row[12])
                    .build())
                .vigencia(VigenciaEntity.builder()
                    .id((Long) row[4])
                    .nombre((String) row[13])
                    .anio((String)row[14])
                    .estado((String)row[15])
                    .build())
                .alcance(AlcanceEntity.builder()
                    .id((Long) row[5])
                    .nombre((String) row[16])
                    .build())
                .normatividad((NormatividadEntity) row[17])
                .nivel(NivelEntity.builder()
                    .id((Long) row[7])
                    .descripcion((String) row[18])
                    .build())
                .escalaSalarial(EscalaSalarialEntity.builder()
                    .id((Long) row[8])
                    .nombre((String) row[19])
                    .build())
                .build();
        }
        return appointment;
    }
    
}
