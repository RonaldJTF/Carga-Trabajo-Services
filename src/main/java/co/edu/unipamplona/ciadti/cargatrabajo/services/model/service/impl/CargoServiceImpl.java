package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.impl;

import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unipamplona.ciadti.cargatrabajo.services.config.specification.SpecificationCiadti;
import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao.CargoDAO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.AlcanceEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CargoEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CompensacionLabNivelVigValorEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CompensacionLabNivelVigenciaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CompensacionLaboralEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.DenominacionEmpleoEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.DependenciaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.EscalaSalarialEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.JerarquiaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.NivelEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.NormatividadEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.OrganigramaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.PeriodicidadEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.VigenciaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CategoriaEntity;
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
        return cargoDAO.findById(id).orElseThrow(() -> new CiadtiException("Cargo no encontrada para el id :: " + id, 404));
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
                entity.getAsignacionBasicaMensual(), 
                entity.getTotalCargos(), 
                entity.getIdJerarquia(), 
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
        Long[] organizationChartIds = filter.get("organizationCharts");
        Long[] dependencyIds = filter.get("dependencies");
        Long[] validityIds = filter.get("validities");
        Long[] scopeIds = filter.get("scopes");
        Long[] levelIds = filter.get("levels");
        Long[] hierarchyIds = filter.get("hierarchies");

        String jpql= """
            select distinct c.id, c.asignacionBasicaMensual, c.totalCargos, c.idJerarquia, c.idVigencia, c.idAlcance, c.idNormatividad, c.idNivel, c.idEscalaSalarial,
                j.orden, 
                o.id, o.nombre, o.descripcion, 
                d.id, d.nombre, d.icono, d.mimetype,
                v.nombre, v.anio, v.estado, 
                a.nombre, 
                n.nombre,
                ni.descripcion, 
                es.nombre, es.codigo,
                clnv.id, clnv.idVigencia, clnv.idNivel, clnv.idEscalaSalarial, clnv.idCompensacionLaboral,
                cl.nombre, cl.idCategoria,
                p.id, p.nombre, p.frecuenciaAnual, 
                cat.nombre, 
                cnvv.id, cnvv.idCompensacionLabNivelVigencia, cnvv.idRegla, cnvv.idVariable,
                de.id, de.nombre, cde.id, cde.totalCargos
            from CargoEntity c 
                inner join JerarquiaEntity j on (c.idJerarquia = j.id) 
                inner join DependenciaEntity d on (j.idDependencia = d.id) 
                inner join OrganigramaEntity o on (j.idOrganigrama = o.id) 
                inner join VigenciaEntity v on (c.idVigencia = v.id) 
                inner join AlcanceEntity a on (c.idAlcance = a.id)  
                inner join NormatividadEntity n on (c.idNormatividad = n.id)
                inner join NivelEntity ni on (c.idNivel = ni.id)
                left outer join EscalaSalarialEntity es on (c.idEscalaSalarial = es.id)
                left outer join CompensacionLabNivelVigenciaEntity clnv on (v.id = clnv.idVigencia and ni.id = clnv.idNivel and (es.id = clnv.idEscalaSalarial OR clnv.idEscalaSalarial IS NULL)) 
                left outer join CompensacionLabNivelVigValorEntity cnvv on (clnv.id = cnvv.idCompensacionLabNivelVigencia) 
                left outer join CompensacionLaboralEntity cl on (clnv.idCompensacionLaboral = cl.id )
                left outer join PeriodicidadEntity p on (cl.idPeriodicidad = p.id ) 
                left outer join CategoriaEntity cat on (cl.idCategoria = cat.id ) 
                left outer join CargoDenominacionEmpleoEntity cde on (c.id = cde.idCargo) 
                left outer join DenominacionEmpleoEntity de on (cde.idDenominacionEmpleo = de.id)
            where 2 > 1
        """;    

        Map<String, Object> parameters = new HashMap<>();

        if (validityIds != null && validityIds.length > 0){
            jpql += "AND c.idVigencia IN :validityIds ";
            parameters.put("validityIds", Arrays.asList(validityIds));
        }
        if (organizationChartIds != null && organizationChartIds.length > 0){
            jpql += "AND j.idOrganigrama IN :organizationChartIds ";
            parameters.put("organizationChartIds", Arrays.asList(organizationChartIds));
        }
        if (dependencyIds != null && dependencyIds.length > 0){
            jpql += "AND j.idDependencia IN :dependencyIds ";
            parameters.put("dependencyIds", Arrays.asList(dependencyIds));
        }
        if (scopeIds != null && scopeIds.length > 0){
            jpql += "AND c.idAlcance IN :scopeIds ";
            parameters.put("scopeIds", Arrays.asList(scopeIds));
        }
        if (levelIds != null && levelIds.length > 0){
            jpql += "AND c.idNivel IN :levelIds ";
            parameters.put("levelIds", Arrays.asList(levelIds));
        }
        if (hierarchyIds != null && hierarchyIds.length > 0){
            jpql += "AND c.idJerarquia IN :hierarchyIds ";
            parameters.put("hierarchyIds", Arrays.asList(hierarchyIds));
        }

        jpql += " order by c.id asc, clnv.id asc, cnvv.id asc ";

        Query query = entityManager.createQuery(jpql);

        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        } 

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        List<CargoEntity> appointments = new ArrayList<>();
        Long appointmentId = -1L;
        Long clnvId = -1L;
        List<Long> jobTitleIds = null;
        CargoEntity appointment = null;
        CompensacionLabNivelVigenciaEntity compensacionLabNivelVigencia = null;
        DenominacionEmpleoEntity jobTitle = null;

        for (Object[] row : results) {
            if (!((Long) row[0]).equals(appointmentId)){
                appointment = CargoEntity.builder()
                    .id((Long) row[0])
                    .asignacionBasicaMensual((Double) row[1])
                    .totalCargos((Integer) row[2])
                    .idJerarquia((Long) row[3])
                    .idVigencia((Long) row[4])
                    .idAlcance((Long) row[5])
                    .idNormatividad((Long) row[6])
                    .idNivel((Long) row[7])
                    .idEscalaSalarial((Long) row[8])
                    .jerarquia(JerarquiaEntity.builder()
                        .id((Long) row[3])
                        .orden((Long) row[9])
                        .idOrganigrama((Long) row[10])
                        .idDependencia((Long) row[13])
                        .organigrama(OrganigramaEntity.builder()
                            .id((Long) row[10])
                            .nombre((String)row[11])
                            .descripcion((String)row[12]).build())
                        .dependencia(DependenciaEntity.builder()
                            .id((Long) row[13])
                            .nombre((String) row[14])
                            .icono((byte[]) row[15])
                            .mimetype((String)row[16])
                            .build())
                        .build())
                    .vigencia(VigenciaEntity.builder()
                        .id((Long) row[4])
                        .nombre((String) row[17])
                        .anio((String)row[18])
                        .estado((String)row[19])
                        .build())
                    .alcance(AlcanceEntity.builder()
                        .id((Long) row[5])
                        .nombre((String) row[20])
                        .build())
                    .normatividad(NormatividadEntity.builder()
                        .id((Long) row[6])
                        .nombre((String) row[21])
                        .build())
                    .nivel(NivelEntity.builder()
                        .id((Long) row[7])
                        .descripcion((String) row[22])
                        .build())
                    .escalaSalarial(EscalaSalarialEntity.builder()
                        .id((Long) row[8])
                        .nombre((String) row[23])
                        .codigo((String) row[24])
                        .build())
                    .compensacionesLaboralesAplicadas(new ArrayList<>())
                    .denominacionesEmpleos(new ArrayList<>())
                    .build();
                appointments.add(appointment);
                appointmentId = (Long) row[0];
                jobTitleIds = new ArrayList<>();
                clnvId = -1L;
            }

            if(row[25] != null){
                if(!((Long) row[25]).equals(clnvId)){
                    compensacionLabNivelVigencia = CompensacionLabNivelVigenciaEntity.builder()
                        .id((Long) row[25])
                        .idVigencia((Long) row[26])
                        .idNivel((Long) row[27])
                        .idEscalaSalarial((Long) row[28])
                        .idCompensacionLaboral((Long) row[29])
                        .compensacionLaboral(CompensacionLaboralEntity.builder()
                            .id((Long) row[29])
                            .nombre((String) row[30])
                            .idCategoria((Long) row[31])
                            .idPeriodicidad((Long) row[32])
                            .periodicidad(PeriodicidadEntity.builder().id((Long) row[32]).nombre((String) row[33]).frecuenciaAnual((Long) row[34]).build())
                            .categoria(CategoriaEntity.builder().nombre((String) row[35]).build())
                            .build())
                        .valoresCompensacionLabNivelVigencia(new ArrayList<>())
                        .build();
                    clnvId = (Long) row[25];

                    /*Si se ha parametrizado una misma compensanción laboral de manera general en el nivel ocupacional,
                      pero luego se ha especificado la misma para una escala salarial, entonces nos quedamos con la especificación.*/
                    Long clId = (Long) row[29];
                    Long salaryScaleId = (Long) row[28];
                    int index = this.findJobCompensationIndex(clId, appointment.getCompensacionesLaboralesAplicadas());
                    if(index == -1){
                        appointment.getCompensacionesLaboralesAplicadas().add(compensacionLabNivelVigencia);
                    }else if(salaryScaleId != null){
                        appointment.getCompensacionesLaboralesAplicadas().set(index, compensacionLabNivelVigencia);
                    }
                }

                if(row[36] != null){
                    CompensacionLabNivelVigValorEntity cnvv = CompensacionLabNivelVigValorEntity.builder()
                        .id((Long) row[36])
                        .idCompensacionLabNivelVigencia((Long) row[37])
                        .idRegla((Long) row[38])
                        .idVariable((Long) row[39])
                        .build();
                    compensacionLabNivelVigencia.getValoresCompensacionLabNivelVigencia().add(cnvv);
                }
            }

            if(row[42] != null){
                if(!jobTitleIds.stream().anyMatch(added -> added.equals((Long) row[42]))){
                    jobTitle = DenominacionEmpleoEntity
                        .builder()
                        .id((Long) row[40])
                        .nombre((String) row[41])
                        .totalCargos((Long) row[43])
                        .build();
                    appointment.getDenominacionesEmpleos().add(jobTitle);
                    jobTitleIds.add((Long) row[42]);
                }
            }
        }
        return appointments;
    }

    /**
     * Encuentra el índice del primer elemento de la lista que coincide con el identificador de la compensación laboral.
     * 
     * @param jobCompensationId Identificador de la compensación laboral.
     * @param list Lista de elementos de la clase CompensacionLabNivelVigenciaEntity.
     * @return Índice del elemento encontrado, o -1 si no se encuentra.
     */
    private int findJobCompensationIndex(Long jobCompensationId, List<CompensacionLabNivelVigenciaEntity> list) {
        if (list == null || jobCompensationId == null) return -1;

        for (int i = 0; i < list.size(); i++) {
            if (jobCompensationId.equals(list.get(i).getIdCompensacionLaboral())) {
                return i;
            }
        }
        return -1;
    }

    @SuppressWarnings("null")
    @Override
    @Transactional(readOnly = true)
    public CargoEntity findByAppointmentId(Long id) {
        String jpql= """
            select distinct c.id, c.asignacionBasicaMensual, c.totalCargos, c.idJerarquia, c.idVigencia, c.idAlcance, c.idNormatividad, c.idNivel, c.idEscalaSalarial,
                j.orden, 
                o.id, o.nombre, o.descripcion, 
                d.id, d.nombre, d.icono, d.mimetype,
                v.nombre, v.anio, v.estado, 
                a.nombre, 
                n.nombre,
                ni.descripcion, 
                es.nombre, es.codigo,
                clnv.id, clnv.idVigencia, clnv.idNivel, clnv.idEscalaSalarial, clnv.idCompensacionLaboral,
                cl.nombre, cl.idCategoria,
                p.id, p.nombre, p.frecuenciaAnual, 
                cat.nombre, 
                cnvv.id, cnvv.idCompensacionLabNivelVigencia, cnvv.idRegla, cnvv.idVariable,
                de.id, de.nombre, cde.id, cde.totalCargos
            from CargoEntity c 
                inner join JerarquiaEntity j on (c.idJerarquia = j.id) 
                inner join DependenciaEntity d on (j.idDependencia = d.id) 
                inner join OrganigramaEntity o on (j.idOrganigrama = o.id) 
                inner join VigenciaEntity v on (c.idVigencia = v.id) 
                inner join AlcanceEntity a on (c.idAlcance = a.id)  
                inner join NormatividadEntity n on (c.idNormatividad = n.id)
                inner join NivelEntity ni on (c.idNivel = ni.id)
                left outer join EscalaSalarialEntity es on (c.idEscalaSalarial = es.id)
                left outer join CompensacionLabNivelVigenciaEntity clnv on (v.id = clnv.idVigencia and ni.id = clnv.idNivel and (es.id = clnv.idEscalaSalarial OR clnv.idEscalaSalarial IS NULL)) 
                left outer join CompensacionLabNivelVigValorEntity cnvv on (clnv.id = cnvv.idCompensacionLabNivelVigencia) 
                left outer join CompensacionLaboralEntity cl on (clnv.idCompensacionLaboral = cl.id ) 
                left outer join PeriodicidadEntity p on (cl.idPeriodicidad = p.id ) 
                left outer join CategoriaEntity cat on (cl.idCategoria = cat.id ) 
                left outer join CargoDenominacionEmpleoEntity cde on (c.id = cde.idCargo) 
                left outer join DenominacionEmpleoEntity de on (cde.idDenominacionEmpleo = de.id) 
            where c.id = :id  
            order by c.id asc, clnv.id asc, cnvv.id asc 
        """;

        Query query = entityManager.createQuery(jpql);
        query.setParameter("id", id);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        
        Long appointmentId = -1L;
        Long clnvId = -1L;
        List<Long> jobTitleIds = null;
        CargoEntity appointment = null;
        CompensacionLabNivelVigenciaEntity compensacionLabNivelVigencia = null;
        DenominacionEmpleoEntity jobTitle = null;

        for (Object[] row : results) {
            if (!((Long) row[0]).equals(appointmentId)){
                appointment = CargoEntity.builder()
                    .id((Long) row[0])
                    .asignacionBasicaMensual((Double) row[1])
                    .totalCargos((Integer) row[2])
                    .idJerarquia((Long) row[3])
                    .idVigencia((Long) row[4])
                    .idAlcance((Long) row[5])
                    .idNormatividad((Long) row[6])
                    .idNivel((Long) row[7])
                    .idEscalaSalarial((Long) row[8])
                    .jerarquia(JerarquiaEntity.builder()
                        .id((Long) row[3])
                        .orden((Long) row[9])
                        .idOrganigrama((Long) row[10])
                        .idDependencia((Long) row[13])
                        .organigrama(OrganigramaEntity.builder()
                            .id((Long) row[10])
                            .nombre((String)row[11])
                            .descripcion((String)row[12]).build())
                        .dependencia(DependenciaEntity.builder()
                            .id((Long) row[13])
                            .nombre((String) row[14])
                            .icono((byte[]) row[15])
                            .mimetype((String)row[16])
                            .build())
                        .build())
                    .vigencia(VigenciaEntity.builder()
                        .id((Long) row[4])
                        .nombre((String) row[17])
                        .anio((String)row[18])
                        .estado((String)row[19])
                        .build())
                    .alcance(AlcanceEntity.builder()
                        .id((Long) row[5])
                        .nombre((String) row[20])
                        .build())
                    .normatividad(NormatividadEntity.builder()
                        .id((Long) row[6])
                        .nombre((String) row[21])
                        .build())
                    .nivel(NivelEntity.builder()
                        .id((Long) row[7])
                        .descripcion((String) row[22])
                        .build())
                    .escalaSalarial(EscalaSalarialEntity.builder()
                        .id((Long) row[8])
                        .nombre((String) row[23])
                        .codigo((String) row[24])
                        .build())
                    .compensacionesLaboralesAplicadas(new ArrayList<>())
                    .denominacionesEmpleos(new ArrayList<>())
                    .build();
                appointmentId = (Long) row[0];
                clnvId = -1L;
                jobTitleIds = new ArrayList<>();
            }

            if(row[25] != null){
                if(!((Long) row[25]).equals(clnvId)){
                    compensacionLabNivelVigencia = CompensacionLabNivelVigenciaEntity.builder()
                        .id((Long) row[25])
                        .idVigencia((Long) row[26])
                        .idNivel((Long) row[27])
                        .idEscalaSalarial((Long) row[28])
                        .idCompensacionLaboral((Long) row[29])
                        .compensacionLaboral(CompensacionLaboralEntity.builder()
                            .id((Long) row[29])
                            .nombre((String) row[30])
                            .idCategoria((Long) row[31])
                            .idPeriodicidad((Long) row[32])
                            .periodicidad(PeriodicidadEntity.builder().id((Long) row[32]).nombre((String) row[33]).frecuenciaAnual((Long) row[34]).build())
                            .categoria(CategoriaEntity.builder().nombre((String) row[35]).build())
                            .build())
                        .valoresCompensacionLabNivelVigencia(new ArrayList<>())
                        .build();
                    clnvId = (Long) row[25];

                    /*Si se ha parametrizado una misma compensanción laboral de manera general en el nivel ocupacional,
                    pero luego se ha especificado la misma para una escala salarial, entonces nos quedamos con la especificación.*/
                    Long clId = (Long) row[29];
                    Long salaryScaleId = (Long) row[28];
                    int index = this.findJobCompensationIndex(clId, appointment.getCompensacionesLaboralesAplicadas());
                    if(index == -1){
                        appointment.getCompensacionesLaboralesAplicadas().add(compensacionLabNivelVigencia);
                    }else if(salaryScaleId != null){
                        appointment.getCompensacionesLaboralesAplicadas().set(index, compensacionLabNivelVigencia);
                    }
                }
                if(row[36] != null){
                    CompensacionLabNivelVigValorEntity cnvv = CompensacionLabNivelVigValorEntity.builder()
                        .id((Long) row[36])
                        .idCompensacionLabNivelVigencia((Long) row[37])
                        .idRegla((Long) row[38])
                        .idVariable((Long) row[39])
                        .build();
                    compensacionLabNivelVigencia.getValoresCompensacionLabNivelVigencia().add(cnvv);
                }
            }

            if(row[42] != null){
                if(!jobTitleIds.stream().anyMatch(added -> added.equals((long) row[42]))){
                    jobTitle = DenominacionEmpleoEntity
                        .builder()
                        .id((Long) row[40])
                        .nombre((String) row[41])
                        .totalCargos((Long) row[43])
                        .build();
                    appointment.getDenominacionesEmpleos().add(jobTitle);
                    jobTitleIds.add((Long) row[42]);
                }
            }
        }
        return appointment;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DenominacionEmpleoEntity> findAllJobTitlesByAppointmentId(Long appointmentId) {
        return cargoDAO.findAllJobTitlesByAppointmentId(appointmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getBasicMonthlyAllowance(Long validityId, Long levelId, Long salaryScaleId) {
        List<Double> result = cargoDAO.getBasicMonthlyAllowances(validityId, levelId, salaryScaleId);
        Double value = null;
        if (result != null && !result.isEmpty()){
            value = result.get(0);
        }
        return value;
    }
}
