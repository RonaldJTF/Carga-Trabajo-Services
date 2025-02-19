package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.report;

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

import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ActividadGestionEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.GestionOperativaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.NivelEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.TipologiaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.GestionOperativaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.NivelService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.StaticResourceMediator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.constant.Corporate;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.command.ImageCommand;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.command.MergeCommand;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.command.TreeCommand;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.functions.TreeFunction;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AssignedOperationalManagementReportExcelJXLS {
    private final GestionOperativaService gestionOperativaService;
    private final NivelService nivelService;
    private final StaticResourceMediator staticResourceMediator;

    private Map<String, Object> registry;
    private Double HOURS_PER_MONTH;

    public byte[] generate(Long organizationChartId) throws Exception {
        registry = new HashMap<>();
        HOURS_PER_MONTH = Corporate.MONTHLY_WORKING_TIME.getValue();
        generateDataset(organizationChartId);
        String filePath = "reports/operationalsManagements/AssignedOperationalsManagements.xlsx";
        Map<String, Object> contextMap = new HashMap<String, Object>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String reportDate = dateFormat.format(new Date());

        TreeFunction t = new TreeFunction();
        contextMap.put("T", t);
        contextMap.put("levels", registry.get("levels"));
        contextMap.put("logo", this.staticResourceMediator.getResourceBytes("reports/images/logo.png"));
        contextMap.put("reportDate", reportDate);
        contextMap.put("HOURS_PER_MONTH", HOURS_PER_MONTH);
        contextMap.put("dataset", registry.get("operationalsManagements"));
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

    private void generateDataset(Long organizationChartId) throws Exception {
        List<NivelEntity> levels = nivelService.findAllInSomeActivity();
        List<GestionOperativaEntity> operationalsManagements = gestionOperativaService.findAssignedOperationalsManagementsByOrganizationChartId(organizationChartId);
        Map<Long, Integer> levelIndexes = IntStream.range(0, levels.size()).boxed().collect(Collectors.toMap(i -> levels.get(i).getId(), i -> i));

        registry.put("operationalsManagements", operationalsManagements);
        registry.put("levels", levels);
        registry.put("levelIndexes", levelIndexes);
        registry.put("treeDeep", getTreeDeep(operationalsManagements));
        assignTimePerLevel(operationalsManagements);
    }

    private int getTreeDeep(List<GestionOperativaEntity> operationalsManagements) {
        int max = 0;
        if (operationalsManagements != null) {
            for (GestionOperativaEntity e : operationalsManagements) {
                max = Math.max(max, 1 + getTreeDeep(e.getSubGestionesOperativas()));
            }
        }
        return max;
    }

    private void assignTimePerLevel(List<GestionOperativaEntity> operationalsManagements) throws CloneNotSupportedException {
        if (operationalsManagements != null) {
            for (GestionOperativaEntity e : operationalsManagements) {
                e.setTipologia(TipologiaEntity.builder().nombre(e.getTipologia().getNombre()).build());
                
                @SuppressWarnings("unchecked")
                Map<Long, Integer> levelIndexes = (Map<Long, Integer>) registry.get("levelIndexes");
                if (e.getActividad() != null) {
                    e.setActividad((ActividadGestionEntity )e.getActividad().clone());

                    List<Double> timePerLevel = new ArrayList<>(Collections.nCopies(levelIndexes.size(), (Double) null));
                    e.getActividad().setTimePerLevel(timePerLevel);
                    double standarTime = (double) 1.07 * (e.getActividad().getTiempoMinimoEnHoras() + 4 * e.getActividad().getTiempoPromedioEnHoras() + e.getActividad().getTiempoMaximoEnHoras()) / 6;
                    e.getActividad().setTiempoEstandar(standarTime);
                    e.getActividad().getTimePerLevel().set(levelIndexes.get(e.getActividad().getIdNivel()), e.getActividad().getFrecuencia() * standarTime);
                } else {
                    e.setActividad(ActividadGestionEntity.builder().timePerLevel(new ArrayList<>(Collections.nCopies(levelIndexes.size(), (Double) null))).build());
                }
                assignTimePerLevel(e.getSubGestionesOperativas());
            }
        }
    }
}
