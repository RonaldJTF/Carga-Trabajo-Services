package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.report;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.ReportStructureDTO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ActividadEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.EstructuraEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.NivelEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.TipologiaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.EstructuraService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.NivelService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.TipologiaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.StaticResourceMediator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.constant.Corporate;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.command.ImageCommand;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.command.MergeCommand;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.functions.TreeFunction;
import lombok.RequiredArgsConstructor;
import org.jxls.transform.poi.JxlsPoiTemplateFillerBuilder;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@Service
public class StructureReportPlainedExcelJXLS {
    private final EstructuraService estructuraService;
    private final TipologiaService tipologiaService;
    private final NivelService nivelService;
    private final StaticResourceMediator staticResourceMediator;

    private Map<String, Object> registry;
    private Double HOURS_PER_MONTH;

    public byte[] generateExcel(List<Long> structureIds) throws Exception {
        registry = new HashMap<>();
        HOURS_PER_MONTH = Corporate.MONTHLY_WORKING_TIME.getValue();
        String filePath = "reports/structures/PlainedStructures.xlsx";
        Map<String, Object> contextMap = new HashMap<String, Object>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String reportDate = dateFormat.format(new Date());


        List<NivelEntity> levels = nivelService.findAllInSomeActivity();
        registry.put("levels", levels);

        Map<Long, Integer> levelIndexes = IntStream.range(0, levels.size()).boxed().collect(Collectors.toMap(i -> levels.get(i).getId(), i -> i));
        registry.put("levelIndexes", levelIndexes);

        TipologiaEntity tipologia = tipologiaService.findFirstTipology();
        List<EstructuraEntity> structures = structureIds != null && !structureIds.isEmpty() ? estructuraService.findAllFilteredByIds(structureIds) : estructuraService.findAllFilteredBy(EstructuraEntity.builder().nombre("").build());

        List<EstructuraEntity> plainedStructures = new ArrayList<EstructuraEntity>();
        filterAndPlainByIdTypology(structures, plainedStructures, tipologia.getId());
        filterDistinctOfIdTypology(plainedStructures, tipologia.getId());

        List<ReportStructureDTO> structureData = new ArrayList<>();
        buildStructureData(plainedStructures, new String[]{"", "", "", ""}, 0, structureData, 0);
        registry.put("plainedStructures", structureData);

        TreeFunction t = new TreeFunction();
        contextMap.put("T", t);
        contextMap.put("levels", registry.get("levels"));
        contextMap.put("levelIndexes", levelIndexes);
        contextMap.put("logo", this.staticResourceMediator.getResourceBytes("reports/images/logo.png"));
        contextMap.put("reportDate", reportDate);
        contextMap.put("HOURS_PER_MONTH", HOURS_PER_MONTH);
        contextMap.put("plainedStructures", plainedStructures);
        contextMap.put("dataStructures", structureData);
        Resource resource = this.staticResourceMediator.getResource(filePath);

        assignTimePerLevel(plainedStructures);

        return JxlsPoiTemplateFillerBuilder
                .newInstance()
                .withTemplate(resource.getInputStream())
                .withCommand("merge", MergeCommand.class)
                .withCommand("image", ImageCommand.class)
                .needsPublicContext(t)
                .buildAndFill(contextMap);
    }

    private void assignTimePerLevel(List<EstructuraEntity> structures) {
        if (structures != null) {
            for (EstructuraEntity e : structures) {
                Map<Long, Integer> levelIndexes = (Map<Long, Integer>) registry.get("levelIndexes");
                if (e.getActividad() != null) {
                    List<Double> timePerLevel = new ArrayList<>(Collections.nCopies(levelIndexes.size(), null));
                    e.getActividad().setTimePerLevel(timePerLevel);
                    double standarTime = 1.07 * (e.getActividad().getTiempoMinimoEnHoras() + 4 * e.getActividad().getTiempoPromedioEnHoras() + e.getActividad().getTiempoMaximoEnHoras()) / 6;
                    e.getActividad().setTiempoEstandar(standarTime);
                    e.getActividad().getTimePerLevel().set(levelIndexes.get(e.getActividad().getIdNivel()), e.getActividad().getFrecuencia() * standarTime);
                } else {
                    e.setActividad(ActividadEntity.builder().timePerLevel(new ArrayList<>(Collections.nCopies(levelIndexes.size(), null))).build());
                }
                assignTimePerLevel(e.getSubEstructuras());
            }
        }
    }


    /**
     * De la lista de todas las estructuras, toma a cada dependencia(definida por idTipology) y las pone en un mismo nivel en la lista plainedStructures,
     * es decir, de esta posible estructura nos queda algo así:
     * ESTRUCTURA INICIAL:
     * Dependencia 1
     * Subdependencia 1.1
     * Proceso 1
     * Procedimiento 1
     * ...
     * Proceso 2
     * ....
     * Subdependencia 1.2
     * ...
     * Dependencia 2
     * ...
     * ESTRUCTURA FINAL:
     * Dependencia 1
     * Subdependencia 1.1
     * Proceso 1
     * Procedimiento 1
     * ...
     * Proceso 2
     * ...
     * Subdependencia 1.1
     * ...
     * Subdependencia 1.2
     * ...
     * Dependencia 2
     * ....
     *
     * @param structures
     * @param plainedStructures
     * @param idTypology
     */
    private void filterAndPlainByIdTypology(List<EstructuraEntity> structures, List<EstructuraEntity> plainedStructures, Long idTypology) {
        for (EstructuraEntity e : structures) {
            if (Objects.equals(e.getIdTipologia(), idTypology) && e.getSubEstructuras() != null) {
                if (e.getSubEstructuras().stream().anyMatch(o -> !Objects.equals(o.getIdTipologia(), idTypology))) {
                    plainedStructures.add(e);
                }
                filterAndPlainByIdTypology(e.getSubEstructuras(), plainedStructures, idTypology);
            }
        }
    }

    /**
     * Remueve de una estructura las subestructuras, ya que estas subestructuras han sido extraidas y puestas en la lista plainedStructures (por el método filterAndPlainByIdTypology)
     *
     * @param plainedStructures
     * @param idTypology
     */
    private void filterDistinctOfIdTypology(List<EstructuraEntity> plainedStructures, Long idTypology) {
        for (EstructuraEntity e : plainedStructures) {
            e.setSubEstructuras(e.getSubEstructuras().stream().filter(o -> o.getIdTipologia() != idTypology).collect(Collectors.toList()));
        }
    }

    private void buildStructureData(List<EstructuraEntity> structures, String[] tipologyStructures, int level, List<ReportStructureDTO> results, int currentDepth) {

        for (EstructuraEntity structure : structures) {
            tipologyStructures[level] = structure.getNombre();

            String levl = null;
            String levelNomenclature = null;
            Double frecuency = null;
            Double minTime = null;
            Double meanTime = null;
            Double maxTime = null;
            Double standarTime = null;
            List<Double> tiemposPorNivel = new ArrayList<>();

            boolean isActivity = "Actividad".equalsIgnoreCase(structure.getTipologia().getNombre());

            List<NivelEntity> levels = (List<NivelEntity>) registry.get("levels");

            if (isActivity && structure.getActividad() != null) {
                levelNomenclature = getLevelNomenclature(structure.getActividad().getNivel().getDescripcion());
                levl = String.format(structure.getActividad().getNivel().getDescripcion());
                minTime = (double) Math.round((structure.getActividad().getTiempoMinimo() / 60.0) * 100) / 100;
                frecuency = structure.getActividad().getFrecuencia();
                maxTime = (double) Math.round((structure.getActividad().getTiempoMaximo() / 60.0) * 100) / 100;
                meanTime = (double) Math.round((structure.getActividad().getTiempoPromedio() / 60.0) * 100) / 100;
                standarTime = (double) Math.round((1.07 * (minTime + 4 * meanTime + maxTime) / 6) * 100) / 100;
                for (NivelEntity e : levels) {
                    tiemposPorNivel.add(Objects.equals(e.getId(), structure.getActividad().getIdNivel()) ? Math.round((frecuency * standarTime) * 10) / 10.0 : null);
                }
            }

            if (tiemposPorNivel.size() < levels.size()) {
                for (int i = tiemposPorNivel.size(); i < levels.size(); i++) {
                    tiemposPorNivel.add(null);
                }
            }

            ReportStructureDTO reportDTO = ReportStructureDTO.builder()
                    .dependencia(tipologyStructures[0])
                    .proceso(level >= 1 ? tipologyStructures[1] : "")
                    .procedimiento(level >= 2 ? tipologyStructures[2] : "")
                    .actividad(level >= 3 ? ("      ".repeat(currentDepth) + tipologyStructures[3]) : "")
                    .nivel(levl).nomenclatura(levelNomenclature)
                    .frecuencia(frecuency).tiempoMinimo(minTime)
                    .tiempoPromedio(meanTime).tiempoMaximo(maxTime)
                    .tiempoEstandar(standarTime)
                    .tiemposPorNivel(tiemposPorNivel)
                    .elementDepth(currentDepth)
                    .build();

            if (isActivity || (structure.getSubEstructuras() == null || structure.getSubEstructuras().isEmpty())) {
                results.add(reportDTO);
            }

            if (structure.getSubEstructuras() != null && !structure.getSubEstructuras().isEmpty()) {
                boolean onlyHasChildrenOfSameType = structure.getSubEstructuras().stream().allMatch(e -> Objects.equals(e.getIdTipologia(), structure.getIdTipologia()));
                if (onlyHasChildrenOfSameType) {
                    buildStructureData(structure.getSubEstructuras(), tipologyStructures, level, results, currentDepth + 1);
                } else {
                    buildStructureData(structure.getSubEstructuras(), tipologyStructures, level + 1, results, 0);
                }
            }
        }
    }

    private String getLevelNomenclature(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        String[] words = str.split(" ");
        if (words.length == 1) {
            return words[0].substring(0, Math.min(3, words[0].length())).toUpperCase();
        } else {
            String firstPart = words[0].substring(0, Math.min(3, words[0].length())).toUpperCase();
            String secondPart = words[1].substring(0, 1).toUpperCase();
            return firstPart + ". " + secondPart + ".";
        }
    }

}
