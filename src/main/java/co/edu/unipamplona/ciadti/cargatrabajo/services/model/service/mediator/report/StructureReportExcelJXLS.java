package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.report;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jxls.transform.poi.JxlsPoiTemplateFillerBuilder;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ActividadEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.EstructuraEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.NivelEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.TipologiaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.EstructuraService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.NivelService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.TipologiaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.StaticResourceMediator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.command.ImageCommand;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.command.MergeCommand;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.command.TreeCommand;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.functions.TreeFunction;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class StructureReportExcelJXLS {
    private final EstructuraService estructuraService;
    private final TipologiaService tipologiaService;
    private final NivelService nivelService;
    private final StaticResourceMediator staticResourceMediator;

    private Map<String, Object> registry;
    private Double HOURS_PER_MONTH;

    public byte[] generate(List<Long> structureIds) throws Exception{
        registry = new HashMap<>();
        HOURS_PER_MONTH = 151.3;
        generateDataset(structureIds);
        String filePath = "reports/structures/reporte.xlsx";
        Map<String, Object> contextMap = new HashMap<String, Object>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String reportDate = dateFormat.format(new Date());

        TreeFunction t = new TreeFunction();
        contextMap.put("T", t);
        contextMap.put("levels", registry.get("levels"));
        contextMap.put("logo", this.staticResourceMediator.getResourceBytes("reports/images/logo.png"));
        contextMap.put("reportDate", reportDate);
        contextMap.put("HOURS_PER_MONTH", HOURS_PER_MONTH);
        contextMap.put("dataset", registry.get("plainedStructures"));
        contextMap.put("treeDeep", registry.get("treeDeep"));
        Resource resource = this.staticResourceMediator.getResource(filePath);

        byte[] bytes = JxlsPoiTemplateFillerBuilder
            .newInstance()
            .withTemplate(resource.getInputStream())
            .withCommand("merge", MergeCommand.class)
            .withCommand("image", ImageCommand.class)
            .withCommand("tree", TreeCommand.class)
            .needsPublicContext(t)
            .buildAndFill(contextMap);
        return bytes;
    }

    private void generateDataset(List<Long> structureIds) throws Exception{
        List<NivelEntity> levels = nivelService.findAll();
        TipologiaEntity tipologiaEntity = tipologiaService.findFirstTipology();
        List<EstructuraEntity> structures = structureIds != null && structureIds.size() > 0 
                                            ? estructuraService.findAllFilteredByIds(structureIds) 
                                            : estructuraService.findAllFilteredBy(EstructuraEntity.builder().nombre("").build());

        List<EstructuraEntity> plainedStructures = new ArrayList<>();
        filterAndPlainByIdTypology(structures, plainedStructures, tipologiaEntity.getId());
        filterDistinctOfIdTypology(plainedStructures, tipologiaEntity.getId());

        Map<Long, Integer> levelIndexes = IntStream.range(0, levels.size()).boxed().collect(Collectors.toMap( i -> levels.get(i).getId(), i -> i));

        registry.put("plainedStructures", plainedStructures);
        registry.put("levels", levels);
        registry.put("levelIndexes", levelIndexes);
        registry.put("treeDeep", getTreeDeep(plainedStructures));
        assignTimePerLevel(plainedStructures);
    }

    private int getTreeDeep(List<EstructuraEntity> structures){
        int max = 0;
        if (structures != null){
            for (EstructuraEntity e : structures){
                max = Math.max(max, 1 + getTreeDeep(e.getSubEstructuras()));
            }
        }
        return max;
    }

    private void assignTimePerLevel(List<EstructuraEntity> structures){
        if (structures != null){
            for (EstructuraEntity e : structures){
                Map<Long, Integer> levelIndexes  = (Map<Long, Integer>)registry.get("levelIndexes");
                if ( e.getActividad() != null){
                    List<Double> timePerLevel = new ArrayList<>(Collections.nCopies(levelIndexes.size(), (Double) null));
                    e.getActividad().setTimePerLevel(timePerLevel);
                    double standarTime = (double) 1.07*(e.getActividad().getTiempoMinimoEnHoras() + 4*e.getActividad().getTiempoPromedioEnHoras() + e.getActividad().getTiempoMaximoEnHoras())/6;
                    e.getActividad().setTiempoEstandar(standarTime);
                    e.getActividad().getTimePerLevel().set(levelIndexes.get(e.getActividad().getIdNivel()), e.getActividad().getFrecuencia() * standarTime);
                }else{
                    e.setActividad(ActividadEntity.builder().timePerLevel(new ArrayList<>(Collections.nCopies(levelIndexes.size(), (Double) null))).build());
                }
                assignTimePerLevel(e.getSubEstructuras());
            }
        }
    }

    /**
     * De la lista de todas las estructuras, toma a cada dependencia(definida por idTipology) y las pone en un mismo nivel en la lista plainedStructures,
     * es decir, de esta posible estructura nos queda algo así:
     * ESTRUCTURA INICIAL:
     *      Dependencia 1 
     *          Subdependencia 1.1
     *              Proceso 1
     *                  Procedimiento 1
     *                      ...
     *              Proceso 2
     *                  ....
     *          Subdependencia 1.2
     *              ...
     *      Dependencia 2
     *          ...
     * ESTRUCTURA FINAL:
     *      Dependencia 1 
     *          Subdependencia 1.1
     *              Proceso 1
     *                  Procedimiento 1
     *                      ...
     *              Proceso 2
     *                  ...
     *      Subdependencia 1.1
     *          ...
     *      Subdependencia 1.2
     *          ...
     *      Dependencia 2
     *          ....
     * @param structures
     * @param plainedStructures
     * @param idTypology
     */
    private void filterAndPlainByIdTypology(List<EstructuraEntity> structures, List<EstructuraEntity> plainedStructures, Long idTipology){
        for (EstructuraEntity e : structures) {
            if (e.getTipologia().getId() == idTipology && e.getSubEstructuras() != null ){
                if(e.getSubEstructuras().stream().anyMatch(o -> o.getTipologia().getId() != idTipology)){
                    plainedStructures.add(e);
                }
                filterAndPlainByIdTypology(e.getSubEstructuras(), plainedStructures, idTipology);
            }
        }
    }

    /**
     * Remueve de una estructura las subestructuras, ya que estas subestructuras han sido extraidas y puestas en la lista plainedStructures (por el método filterAndPlainByIdTypology)
     * @param plainedStructures
     * @param idTypology
     */
    private void filterDistinctOfIdTypology(List<EstructuraEntity> plainedStructures, Long idTypology){
        for (EstructuraEntity e : plainedStructures) {
            e.setSubEstructuras(
                e.getSubEstructuras().stream()
                    .filter(o -> o.getTipologia().getId() != idTypology)
                    .collect(Collectors.toList())
            );
        }
    }
}
