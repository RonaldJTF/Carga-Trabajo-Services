package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.report;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jxls.functions.GroupSum;
import org.jxls.transform.poi.JxlsPoiTemplateFillerBuilder;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.GroupAttribute;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.NodeDTO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CargoEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.ConfigurationMediator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.StaticResourceMediator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.command.EachMergeCommand;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.command.HeaderCommand;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.command.ImageCommand;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.command.MergeCommand;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.command.StyleCommand;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.summarizer.IntegerSummarizerBuilder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AppointmentReportExcelJXLS {
    private final ConfigurationMediator configurationMediator;
    private final StaticResourceMediator staticResourceMediator;
    private Map<String, Object> registry;

    public byte[] generate(Map<String, Long[]> filters) throws Exception{
        registry = new HashMap<>();

        List<CargoEntity> appointments = configurationMediator.findAppointments(filters);  

        generateDataset(appointments);

        String filePath = "reports/appointment/Appointments.xlsx";
        Map<String, Object> contextMap = new HashMap<String, Object>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String reportDate = dateFormat.format(new Date());

        GroupSum<Integer> g = new GroupSum<Integer>(new IntegerSummarizerBuilder());
        contextMap.put("G", g);
        contextMap.put("logo", this.staticResourceMediator.getResourceBytes("reports/images/logo.png"));
        contextMap.put("reportDate", reportDate);
        contextMap.put("workplan", registry.get("workplan"));
        contextMap.put("dates", registry.get("dates"));
        contextMap.put("stages", registry.get("stages"));
        contextMap.put("totalDays", registry.get("totalDays"));
        contextMap.put("workplanAdvance", registry.get("workplanAdvance"));
        Resource resource = this.staticResourceMediator.getResource(filePath);

        byte[] bytes = JxlsPoiTemplateFillerBuilder
            .newInstance()
            .withTemplate(resource.getInputStream())
            .withCommand("merge", MergeCommand.class)
            .withCommand("image", ImageCommand.class)
            .withCommand("style", StyleCommand.class)
            .withCommand("eachMerge", EachMergeCommand.class)
            .withCommand("header", HeaderCommand.class)
            .needsPublicContext(g)
            .buildAndFill(contextMap);
        return bytes;
    }

    private void generateDataset(List<CargoEntity> appointments) throws CiadtiException{
        
    } 

    public List<NodeDTO> groupByAttributes(List<Map<String, Object>> list, List<GroupAttribute> groupAttributes) {
        List<String> groupKeys = groupAttributes.stream().map(GroupAttribute::getGroupKey).collect(Collectors.toList());
        List<String> groupValues = groupAttributes.stream().map(GroupAttribute::getGroupValue).collect(Collectors.toList());
        List<String> types = groupAttributes.stream().map(GroupAttribute::getType).collect(Collectors.toList());

        Map<String, List<Map<String, Object>>> grouped = groupRecursively(list, groupKeys, 0);
        return buildTree(grouped, groupKeys, groupValues, types, 0);
    }

    private Map<String, List<Map<String, Object>>> groupRecursively(List<Map<String, Object>> items, List<String> groupKeys, int level) {
        if (level >= groupKeys.size()) {
            return items.stream().collect(Collectors.groupingBy(item -> item.get(groupKeys.get(level - 1)).toString()));
        }
        String attr = groupKeys.get(level);
        return items.stream().collect(Collectors.groupingBy(item -> item.get(attr).toString()));
    }

    private List<NodeDTO> buildTree(Map<String, List<Map<String, Object>>> grouped, List<String> groupKeys, List<String> groupValues, List<String> types, int level) {
        if (level >= groupKeys.size()) return Collections.emptyList();

        List<NodeDTO> result = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : grouped.entrySet()) {
            String key = entry.getKey();
            List<Map<String, Object>> items = entry.getValue();

            Map<String, Object> data = new HashMap<>();
            data.put(groupKeys.get(level), key);
            data.put("type", types.get(level));
            data.put("items", items);
            data.put("isLastGroup", level == groupKeys.size() - 1);
            data.put("total", items.size());

            List<NodeDTO> children = buildTree(groupRecursively(items, groupKeys, level + 1), groupKeys, groupValues, types, level + 1);

            result.add(new NodeDTO(data, children));
        }
        return result;
    }
}
