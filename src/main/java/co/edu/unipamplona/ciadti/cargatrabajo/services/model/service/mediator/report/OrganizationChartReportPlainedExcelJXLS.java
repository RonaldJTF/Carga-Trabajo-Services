package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.report;

import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.dto.ReportOperationalManagementDTO;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.NivelEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.OrganigramaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.GestionOperativaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.NivelService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.OrganigramaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator.StaticResourceMediator;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.constant.Corporate;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.command.ImageCommand;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.command.MergeCommand;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.report.jxls.functions.TreeFunction;
import lombok.RequiredArgsConstructor;
import org.jxls.common.NeedsPublicContext;
import org.jxls.transform.poi.JxlsPoiTemplateFillerBuilder;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@Service
public class OrganizationChartReportPlainedExcelJXLS {
    private static final String REPORT_TEMPLATE_PATH = "reports/organizationChart/organizationChart.xlsx";
    private static final String LOGO_PATH = "reports/images/logo.png";
    private static final String DATE_FORMAT = "dd/MM/yyyy HH:mm";

    private final StaticResourceMediator staticResourceMediator;
    private final GestionOperativaService gestionOperativaService;
    private final NivelService nivelService;
    private final OrganigramaService organigramaService;

    private Map<String, Object> registry;
    private Double hoursPerMonth;

    /**
     * Genera un informe en formato Excel basado en los IDs de organigrama proporcionados.
     * Este método realiza las siguientes acciones:
     * 1. Inicializa el registro (`registry`) con los niveles y sus índices correspondientes.
     * 2. Establece las horas mensuales de trabajo utilizando el valor de {@link Corporate#MONTHLY_WORKING_TIME}.
     * 3. Prepara un mapa de contexto con los datos necesarios para generar el informe, incluyendo:
     *    - Los niveles y sus índices.
     *    - El logo de la organización.
     *    - Las horas mensuales de trabajo.
     *    - La fecha del informe.
     *    - Los datos operativos del informe.
     *    - Los datos agrupados por proceso y sumados por nivel.
     * 4. Obtiene el recurso de la plantilla del informe desde el {@link StaticResourceMediator}.
     * 5. Utiliza {@link JxlsPoiTemplateFillerBuilder} para llenar la plantilla con los datos del contexto
     *    y generar el informe en formato Excel.
     * 6. Retorna el contenido del informe como un arreglo de bytes.
     *
     * @param organizationChartIds Lista de IDs de organigrama para los cuales se generará el informe.
     * @return Un arreglo de bytes que representa el contenido del informe en formato Excel.
     * @throws Exception Si ocurre un error durante la generación del informe, como problemas al obtener
     *                   los recursos, procesar los datos o llenar la plantilla.
     */
    public byte[] generate(Long organizationChartIds) throws Exception {
        initializeRegistry();
        hoursPerMonth = Corporate.MONTHLY_WORKING_TIME.getValue();

        Map<String, Object> contextMap = prepareContextMap(organizationChartIds);
        Resource resource = staticResourceMediator.getResource(REPORT_TEMPLATE_PATH);

        return JxlsPoiTemplateFillerBuilder
                .newInstance()
                .withTemplate(resource.getInputStream())
                .withCommand("merge", MergeCommand.class)
                .withCommand("image", ImageCommand.class)
                .needsPublicContext((NeedsPublicContext) contextMap.get("T"))
                .buildAndFill(contextMap);
    }

    /**
     * Inicializa el registro (`registry`) con los niveles y sus índices correspondientes.
     * Este método realiza las siguientes acciones:
     * 1. Crea un nuevo `LinkedHashMap` para el registro, que mantiene el orden de inserción.
     * 2. Obtiene todos los niveles disponibles utilizando el servicio `nivelService`.
     * 3. Almacena la lista de niveles en el registro bajo la clave "levels".
     * 4. Crea un mapa de índices de niveles utilizando el método {@link #createLevelIndexes(List)}
     *    y lo almacena en el registro bajo la clave "levelIndexes".
     */
    private void initializeRegistry() {
        registry = new LinkedHashMap<>();
        List<NivelEntity> levels = nivelService.findAll();
        registry.put("levels", levels);
        registry.put("levelIndexes", createLevelIndexes(levels));
    }

    /**
     * Crea un mapa que asocia el ID de cada nivel con su índice en la lista.
     *
     * @param levels, lista con información a mapear
     * @return Map<Long, Integer>, mapa de id de los niveles
     */
    private Map<Long, Integer> createLevelIndexes(List<NivelEntity> levels) {
        return IntStream.range(0, levels.size())
                .boxed()
                .collect(Collectors.toMap(i -> levels.get(i).getId(), i -> i));
    }

    /**
     * Prepara un mapa de contexto que contiene los datos necesarios para generar el informe.
     * Este método realiza las siguientes acciones:
     * 1. Obtiene y procesa los datos del informe a partir de los IDs de organigrama proporcionados.
     * 2. Agrupa los datos por proceso y suma los tiempos por nivel.
     * 3. Crea un mapa de contexto con los datos procesados, funciones auxiliares y recursos estáticos.
     *
     * @param organizationChartIds Lista de IDs de organigrama para los cuales se generará el informe.
     * @return Un mapa de contexto que contiene:
     *         - Una función de árbol (`TreeFunction`) para procesar estructuras jerárquicas.
     *         - Los niveles y sus índices.
     *         - El logo de la organización en formato de bytes.
     *         - Las horas mensuales de trabajo.
     *         - La fecha del informe en el formato especificado.
     *         - Los datos operativos del informe.
     *         - Los datos agrupados por proceso y sumados por nivel.
     * @throws Exception Si ocurre un error al obtener o procesar los datos.
     */
    private Map<String, Object> prepareContextMap(Long organizationChartIds) throws CiadtiException {
        Map<String, Object> contextMap = new LinkedHashMap<>();

        List<ReportOperationalManagementDTO> reportData = fetchAndProcessReportData(organizationChartIds);
        List<ReportOperationalManagementDTO> resultProcess = groupByProcesoAndSumTiemposPorNivel(reportData);

        OrganigramaEntity organigrama = organigramaService.findById(organizationChartIds);

        TreeFunction t = new TreeFunction();
        contextMap.put("T", t);
        contextMap.put("levels", registry.get("levels"));
        contextMap.put("levelIndexes", registry.get("levelIndexes"));
        contextMap.put("logo", staticResourceMediator.getResourceBytes(LOGO_PATH));
        contextMap.put("HOURS_PER_MONTH", hoursPerMonth);
        contextMap.put("reportDate", new SimpleDateFormat(DATE_FORMAT).format(new Date()));
        contextMap.put("organizationChart", organigrama);
        contextMap.put("dataOperationalManagement", reportData);
        contextMap.put("plainProcess", resultProcess);

        return contextMap;
    }

    /**
     * Obtiene y procesa los datos del informe de gestión operativa para los IDs de organigrama proporcionados.
     * Este método realiza las siguientes acciones:
     * 1. Obtiene los datos sin procesar (rawData) desde el servicio `gestionOperativaService`.
     * 2. Convierte los datos en una lista de DTOs utilizando el método {@link #mapRawDataToDTOs(List)}.
     * 3. Calcula los niveles de profundidad de cada ítem en la lista de DTOs utilizando el método {@link #calculateDepthLevels(List)}.
     * 4. Construye la estructura de datos necesaria para el informe utilizando el método {@link #buildStructureData(List)}.
     * 5. Almacena los datos procesados en el registro (`registry`) bajo la clave "dataOperationalManagement".
     * 6. Retorna la lista de DTOs procesados.
     *
     * @param organizationChartIds Lista de IDs de organigrama para los cuales se generará el informe.
     * @return Una lista de objetos {@link ReportOperationalManagementDTO} que representan los datos procesados del informe.
     */
    private List<ReportOperationalManagementDTO> fetchAndProcessReportData(Long organizationChartIds) {
        List<Object[]> rawData = gestionOperativaService.findOperationalManagementByOrganizationChart(organizationChartIds);
        List<ReportOperationalManagementDTO> resultDTO = mapToDTO(rawData);
        List<ReportOperationalManagementDTO> reportData = processGroupedByDependencies(resultDTO);
        calculateDepthLevels(reportData);
        buildStructureData(reportData);
        registry.put("dataOperationalManagement", reportData);
        return reportData;
    }

    /**
     * Convierte una lista de filas de datos (representadas como arreglos de objetos) en una lista de
     * objetos de tipo {@link ReportOperationalManagementDTO}. Este método utiliza el método
     * {@link #mapRowToDTO(Object[])} para mapear cada fila individual a un DTO.
     *
     * @param rawData Una lista de arreglos de objetos, donde cada arreglo representa una fila de datos
     *                obtenida de una consulta. Cada arreglo debe contener los valores en el orden
     *                esperado por el método {@link #mapRowToDTO(Object[])}.
     * @return Una lista de objetos {@link ReportOperationalManagementDTO}, donde cada DTO representa
     *         una fila de datos mapeada.
     */
//    private List<ReportOperationalManagementDTO> mapRawDataToDTOs(List<Object[]> rawData) {
//        return rawData.stream()
//                .map(this::mapRowToDTO)
//                .collect(Collectors.toList());
//    }

    /**
     * Convierte una fila de datos (representada como un arreglo de objetos) en un objeto de tipo
     * {@link ReportOperationalManagementDTO}. Este método se utiliza para mapear los resultados de una
     * consulta de base de datos a un DTO que representa la gestión operativa.
     *
     * @param row Un arreglo de objetos que contiene los valores de una fila de la consulta.
     * @return Un objeto {@link ReportOperationalManagementDTO} con los valores mapeados desde la fila.
     *         Si algún valor es nulo, se maneja adecuadamente para evitar excepciones.
     */
//    private ReportOperationalManagementDTO mapRowToDTO(Object[] row) {
//        return new ReportOperationalManagementDTO(
//                ((BigDecimal) row[0]).longValue(), // idGestionOperativa
//                (String) row[1], // proceso
//                (String) row[2], // procesoDescripcion
//                (String) row[3], // procedimiento
//                (String) row[4], // procedimientoDescripcion
//                (String) row[5], // actividad
//                (String) row[6], // actividadDescripcion
//                (String) row[7], // dependencia
//                (String) row[8], // dependenciaDescripcion
//                Optional.ofNullable((BigDecimal) row[9]).map(BigDecimal::longValue).orElse(null), // idActividad
//                Optional.ofNullable((BigDecimal) row[10]).map(BigDecimal::doubleValue).orElse(null), // frecuencia
//                Optional.ofNullable((BigDecimal) row[11]).map(BigDecimal::doubleValue).orElse(null), // tiempoMinimo
//                Optional.ofNullable((BigDecimal) row[12]).map(BigDecimal::doubleValue).orElse(null), // tiempoMaximo
//                Optional.ofNullable((BigDecimal) row[13]).map(BigDecimal::doubleValue).orElse(null), // tiempoPromedio
//                Optional.ofNullable((BigDecimal) row[14]).map(BigDecimal::longValue).orElse(null), // idNivel
//                Optional.ofNullable((String) row[15]).orElse(""), // nivel
//                Optional.ofNullable((BigDecimal) row[16]).map(BigDecimal::longValue).orElse(null) // idActividadPadre
//        );
//    }

    /**
     * Calcula los niveles de profundidad de cada ítem en la lista de datos de gestión operativa
     * y ajusta el nombre de la actividad para reflejar su nivel en la jerarquía.
     * Este método realiza las siguientes acciones:
     * 1. Crea un mapa que asocia el ID de gestión operativa con su respectivo DTO.
     * 2. Para cada ítem en la lista, calcula su nivel de profundidad utilizando el método
     *    {@link #calculateDepth(ReportOperationalManagementDTO, Map)}.
     * 3. Ajusta el nombre de la actividad para reflejar su nivel en la jerarquía, agregando
     *    espacios en blanco según su profundidad.
     *
     * @param reportData Lista de objetos {@link ReportOperationalManagementDTO} que representan
     *                   los datos de gestión operativa.
     */
    private void calculateDepthLevels(List<ReportOperationalManagementDTO> reportData) {
        Map<Long, ReportOperationalManagementDTO> activityMap = reportData.stream()
                .collect(Collectors.toMap(
                        ReportOperationalManagementDTO::getIdGestionOperativa, // clave
                        Function.identity(), // valor
                        (existing, duplicate) -> existing // en caso de duplicado, conservar el existente
                ));

        for (ReportOperationalManagementDTO dto : reportData) {
            dto.setElementDepth(calculateDepth(dto, activityMap));
            dto.setActividad("      ".repeat(dto.getElementDepth()) + dto.getActividad());
        }
    }

    /**
     * Calcula el nivel de profundidad de un ítem en la jerarquía de gestión operativa.
     * Este método utiliza recursión para determinar cuántos niveles por encima del ítem actual
     * existen en la jerarquía, basándose en su relación padre-hijo.
     *
     * @param dto         El ítem de gestión operativa para el cual se calculará la profundidad.
     * @param activityMap Un mapa que asocia el ID de gestión operativa con su respectivo DTO.
     *                    Este mapa permite un acceso rápido a los padres de cada ítem.
     * @return El nivel de profundidad del ítem en la jerarquía. Si el ítem no tiene padre,
     *         se retorna 0 (indicando que es el nivel raíz). Si el padre no se encuentra en el mapa,
     *         también se retorna 0.
     */
    private int calculateDepth(ReportOperationalManagementDTO dto, Map<Long, ReportOperationalManagementDTO> activityMap) {
        if (dto.getIdGestionOperativaPadre() == null || !dto.getTipologia().equalsIgnoreCase("actividad")) {
            return 0;
        } else {
            ReportOperationalManagementDTO parent = activityMap.get(dto.getIdGestionOperativaPadre().longValue());
            if (parent == null || !Objects.equals(parent.getIdTipologia(), dto.getIdTipologia())) {
                return 0;
            }
            return calculateDepth(parent, activityMap) + 1;
        }
    }

    /**
     * Construye la estructura de datos necesaria para el informe procesando cada ítem
     * en la lista de resultados. Este método utiliza el método {@link #processReportItem(ReportOperationalManagementDTO)}
     * para procesar cada ítem individualmente.
     *
     * @param results Lista de objetos {@link ReportOperationalManagementDTO} que representan
     *                los datos de gestión operativa a procesar.
     */
    private void buildStructureData(List<ReportOperationalManagementDTO> results) {
        if (results == null || results.isEmpty()) {
            results = new ArrayList<>();
            results.add(new ReportOperationalManagementDTO());
        }
        results.forEach(this::processReportItem);
    }

    /**
     * Procesa un ítem de gestión operativa para asignarle la nomenclatura correspondiente
     * y calcular los tiempos por nivel. Este método realiza las siguientes acciones:
     * 1. Calcula los tiempos por nivel para el ítem si tiene una actividad asociada (`idActividad` no es nulo).
     *    Si no tiene una actividad asociada, inicializa una lista de tiempos por nivel con valores nulos,
     *    esto se hace para que al momento de construir el reporte se puedan pintar las celdas vacías.
     * 2. Asigna la nomenclatura correspondiente al nivel del ítem si tiene una actividad asociada.
     * 3. Asigna los tiempos por nivel calculados o inicializados al ítem.
     *
     * @param item El ítem de gestión operativa a procesar.
     */
    private void processReportItem(ReportOperationalManagementDTO item) {
        List<NivelEntity> levels = (List<NivelEntity>) registry.get("levels");
        List<Double> tiemposPorNivel = item.getIdActividad() != null ? calculateTiemposPorNivel(item, levels) : initializedTiemposPorNivel(levels);

        item.setNomenclatura(item.getIdActividad() != null ? getLevelNomenclature(item.getNivel()) : null);
        item.setTiemposPorNivel(tiemposPorNivel);
    }

    /**
     * Inicializa una lista de tiempos por nivel con valores nulos. La lista tendrá el mismo tamaño
     * que la lista de niveles proporcionada. Este método es utilizado para crear una lista de tiempos
     * por nivel cuando un ítem no tiene una actividad asociada.
     *
     * @param levels Lista de objetos {@link NivelEntity} que representan los niveles disponibles.
     * @return Una lista de {@link Double} con el mismo tamaño que la lista de niveles, donde cada
     *         elemento es nulo.
     */
    private List<Double> initializedTiemposPorNivel(List<NivelEntity> levels) {
        List<Double> tiemposPorNivel = new ArrayList<>();
        for (int i = 0; i < levels.size(); i++) {
            tiemposPorNivel.add(null);
        }
        return tiemposPorNivel;
    }
    /**
     * Calcula los tiempos por nivel para un ítem de gestión operativa basado en los tiempos mínimo,
     * máximo y promedio, así como en la frecuencia de la actividad. Este método realiza las siguientes acciones:
     * 1. Convierte los tiempos mínimo, máximo y promedio de minutos a horas.
     * 2. Calcula el tiempo estándar utilizando la fórmula:
     *    {@code 1.07 * (minTime + 4 * meanTime + maxTime) / 6}.
     * 3. Asigna los tiempos mínimo, máximo y promedio convertidos y el tiempo estándar calculado a los ítems.
     * 4. Calcula los tiempos por nivel para cada nivel en la lista de niveles, asignando un valor
     *    solo al nivel correspondiente al ítem y dejando los demás como nulos.
     *
     * @param item   El ítem de gestión operativa para el cual se calcularán los tiempos por nivel.
     * @param levels La lista de niveles disponibles.
     * @return Una lista de {@link Double} que representa los tiempos por nivel. Solo el nivel
     *         correspondiente al ítem tendrá un valor calculado; los demás serán nulos.
     */
    private List<Double> calculateTiemposPorNivel(ReportOperationalManagementDTO item, List<NivelEntity> levels) {
        double minTime = roundToTwoDecimals(item.getTiempoMinimo() / 60.0);
        double maxTime = roundToTwoDecimals(item.getTiempoMaximo() / 60.0);
        double meanTime = roundToTwoDecimals(item.getTiempoPromedio() / 60.0);
        double standardTime = roundToTwoDecimals(1.07 * (minTime + 4 * meanTime + maxTime) / 6);

        item.setTiempoMinimo(minTime);
        item.setTiempoMaximo(maxTime);
        item.setTiempoPromedio(meanTime);
        item.setTiempoEstandar(standardTime);

        return levels.stream()
                .map(level -> Objects.equals(level.getId(), item.getIdNivel()) ? roundToOneDecimal(item.getFrecuencia() * standardTime) : null)
                .collect(Collectors.toList());
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100) / 100.0;
    }

    private double roundToOneDecimal(double value) {
        return Math.round(value * 10) / 10.0;
    }

    /**
     * Genera la nomenclatura abreviada para un nombre de nivel. Este método toma el nombre del nivel
     * y lo convierte en una nomenclatura abreviada siguiendo las siguientes reglas:
     * 1. Si el nombre del nivel es nulo o está vacío, retorna una cadena vacía.
     * 2. Si el nombre del nivel contiene una sola palabra, retorna los primeros 3 caracteres de esa palabra en mayúsculas.
     * 3. Si el nombre del nivel contiene más de una palabra, retorna los primeros 3 caracteres de la primera palabra
     *    en mayúsculas, seguidos de un punto, un espacio, la primera letra de la segunda palabra en mayúsculas y otro punto.
     *
     * @param levelName El nombre del nivel para el cual se generará la nomenclatura.
     * @return La nomenclatura abreviada del nivel. Si el nombre del nivel es nulo o está vacío, retorna una cadena vacía.
     */
    private String getLevelNomenclature(String levelName) {
        if (levelName == null || levelName.isEmpty()) {
            return "";
        }
        String[] words = levelName.split(" ");
        if (words.length == 1) {
            return words[0].substring(0, Math.min(3, words[0].length())).toUpperCase();
        } else {
            String firstPart = words[0].substring(0, Math.min(3, words[0].length())).toUpperCase();
            String secondPart = words[1].substring(0, 1).toUpperCase();
            return firstPart + ". " + secondPart + ".";
        }
    }

    /**
     * Agrupa los datos de gestión operativa por proceso y suma los tiempos por nivel.
     * Este método realiza las siguientes acciones:
     * 1. Agrupa los ítems de gestión operativa por el campo "proceso".
     * 2. Para cada grupo, suma los tiempos por nivel utilizando el método {@link #sumaLists(List, List)}.
     * 3. Crea un nuevo DTO agrupado para cada proceso con los tiempos por nivel sumados.
     *
     * @param reportData Lista de objetos {@link ReportOperationalManagementDTO} que representan
     *                   los datos de gestión operativa a agrupar.
     * @return Una lista de objetos {@link ReportOperationalManagementDTO} agrupados por proceso,
     *         con los tiempos por nivel sumados.
     */
    private static List<ReportOperationalManagementDTO> groupByProcesoAndSumTiemposPorNivel(List<ReportOperationalManagementDTO> reportData) {
        Map<String, List<Double>> groupedData = reportData.stream()
                .collect(Collectors.groupingBy(
                        ReportOperationalManagementDTO::getProceso,
                        Collectors.reducing(
                                new ArrayList<Double>(),
                                ReportOperationalManagementDTO::getTiemposPorNivel,
                                (list1, list2) -> sumaLists(list1, list2)
                        )
                ));

        return groupedData.entrySet().stream()
                .map(entry -> createGroupedDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Suma dos listas de valores de tipo {@link Double} elemento por elemento.
     * Este método realiza las siguientes acciones:
     * 1. Si la primera lista está vacía, retorna la segunda lista.
     * 2. Si la segunda lista está vacía, retorna la primera lista.
     * 3. Si ambas listas tienen elementos, suma los elementos correspondientes de ambas listas.
     *    Si alguno de los elementos es nulo, se considera como 0.0 para la suma.
     * 4. Retorna una nueva lista con los resultados de la suma.
     *
     * @param list1 La primera lista de valores de tipo {@link Double}.
     * @param list2 La segunda lista de valores de tipo {@link Double}.
     * @return Una nueva lista de {@link Double} que contiene la suma de los elementos correspondientes
     *         de las dos listas de entrada.
     */
    private static List<Double> sumaLists(List<Double> list1, List<Double> list2) {
        if (list1.isEmpty()) return list2;
        if (list2.isEmpty()) return list1;
        return IntStream.range(0, Math.min(list1.size(), list2.size()))
                .mapToObj(i -> {
                    Double value1 = list1.get(i) != null ? list1.get(i) : 0.0;
                    Double value2 = list2.get(i) != null ? list2.get(i) : 0.0;
                    return value1 + value2;
                })
                .collect(Collectors.toList());
    }

    /**
     * Crea un nuevo objeto {@link ReportOperationalManagementDTO} con el proceso y los tiempos por nivel especificados.
     * Este método utiliza el patrón Builder para construir el DTO.
     *
     * @param proceso El nombre del proceso que se asignará al DTO.
     * @param tiemposPorNivel La lista de tiempos por nivel que se asignará al DTO.
     * @return Un nuevo objeto {@link ReportOperationalManagementDTO} con el proceso y los tiempos por nivel especificados.
     */
    private static ReportOperationalManagementDTO createGroupedDTO(String proceso, List<Double> tiemposPorNivel) {
        return ReportOperationalManagementDTO.builder()
                .proceso(proceso)
                .tiemposPorNivel(tiemposPorNivel)
                .build();
    }

    private List<ReportOperationalManagementDTO> mapToDTO(List<Object[]> resultList) {

        List<ReportOperationalManagementDTO> report = new ArrayList<>();
        for (Object[] result : resultList) {
            ReportOperationalManagementDTO dto = new ReportOperationalManagementDTO();

            // Mapeo común
            dto.setIdGestionOperativa(((Number) result[0]).longValue());
            String geopNombre = (String) result[1];
            String geopDescripcion = (String) result[2];
            dto.setIdGestionOperativaPadre(result[3] != null ? ((Number) result[3]).longValue() : null);
            Long idTipologia = result[4] != null ? ((Number) result[4]).longValue() : null;
            //dto.setNivel(result[5] != null ? ((Number) result[5]).intValue() : null);
            dto.setDependencia((String) result[6]);

            dto.setOrganigrama((String) result[7]);
            dto.setOrganigramaDescripcion((String) result[8]);

            dto.setIdActividad(result[9] != null ? ((Number) result[9]).longValue() : null);
            dto.setIdNivel(result[10] != null ? ((Number) result[10]).longValue() : null);
            dto.setFrecuencia(result[11] != null ? ((Number) result[11]).doubleValue() : null);
            dto.setTiempoMaximo(result[12] != null ? ((Number) result[12]).doubleValue() : null);
            dto.setTiempoMinimo(result[13] != null ? ((Number) result[13]).doubleValue() : null);
            dto.setTiempoPromedio(result[14] != null ? ((Number) result[14]).doubleValue() : null);
            dto.setNivel((String) result[15]);

            String tipologia = (String) result[16];

            dto.setIdTipologia(idTipologia);
            dto.setTipologia(tipologia);

            if (idTipologia != null) {
                switch (tipologia) {
                    case "Proceso":
                        dto.setProceso(geopNombre);
                        dto.setProcesoDescripcion(geopDescripcion);
                        break;
                    case "Procedimiento":
                        dto.setProcedimiento(geopNombre);
                        dto.setProcedimientoDescripcion(geopDescripcion);
                        break;
                    case "Actividad":
                        dto.setActividad(geopNombre);
                        dto.setActividadDescripcion(geopDescripcion);
                        break;
                    default:
                        break;
                }
            }
            report.add(dto);
        }

        return report;
    }

    private List<ReportOperationalManagementDTO> processGroupedByDependencies(List<ReportOperationalManagementDTO> flatList) {
        // Paso 1: Agrupar por dependencia
        Map<String, List<ReportOperationalManagementDTO>> groupedByDependencies = flatList.stream()
                .collect(Collectors.groupingBy(ReportOperationalManagementDTO::getDependencia));

        List<ReportOperationalManagementDTO> finalList = new ArrayList<>();

        // Paso 2: Procesar cada grupo de dependencia
        for (Map.Entry<String, List<ReportOperationalManagementDTO>> entry : groupedByDependencies.entrySet()) {
            String dependencia = entry.getKey();
            List<ReportOperationalManagementDTO> group = entry.getValue();

            group.sort(Comparator.comparing(ReportOperationalManagementDTO::getIdGestionOperativa));

            // Paso 3: Crear un mapa de acceso rápido para todos los elementos del grupo
            Map<Long, Set<ReportOperationalManagementDTO>> dtoMap = new HashMap<>();

            // Paso 4: Procesar jerarquía de cada elemento dentro del grupo
            for (ReportOperationalManagementDTO dto : group) {
                Long currentParentId = dto.getIdGestionOperativaPadre();

                ReportOperationalManagementDTO newDto = new ReportOperationalManagementDTO();

                // Copiar todos los campos del hijo
                newDto.setIdGestionOperativa(dto.getIdGestionOperativa());
                newDto.setIdActividad(dto.getIdActividad());
                newDto.setActividad(dto.getActividad() != null ? dto.getActividad() : "");
                newDto.setDependencia(dependencia);
                newDto.setNivel(dto.getNivel());
                newDto.setIdNivel(dto.getIdNivel());
                newDto.setIdGestionOperativaPadre(currentParentId);
                newDto.setIdTipologia(dto.getIdTipologia());
                newDto.setTipologia(dto.getTipologia());

                newDto.setProceso(dto.getProceso());
                newDto.setProcesoDescripcion(dto.getProcesoDescripcion());
                newDto.setProcedimiento(dto.getProcedimiento());
                newDto.setProcedimientoDescripcion(dto.getProcedimientoDescripcion());

                // Copiar los tiempos del hijo (si existen), sin reemplazar por los del padre
                if (dto.getTiempoMinimo() != null) {
                    newDto.setTiempoMinimo(dto.getTiempoMinimo());
                }
                if (dto.getTiempoMaximo() != null) {
                    newDto.setTiempoMaximo(dto.getTiempoMaximo());
                }
                if (dto.getTiempoPromedio() != null) {
                    newDto.setTiempoPromedio(dto.getTiempoPromedio());
                }
                if (dto.getTiempoEstandar() != null) {
                    newDto.setTiempoEstandar(dto.getTiempoEstandar());
                }
                if (dto.getFrecuencia() != null) {
                    newDto.setFrecuencia(dto.getFrecuencia());
                }

                // Buscar el proceso y procedimiento para cada hijo
                while (currentParentId != null) {
                    Long finalCurrentParentId = currentParentId;
                    ReportOperationalManagementDTO parent = group.stream()
                            .filter(d -> d.getIdGestionOperativa().equals(finalCurrentParentId))
                            .findFirst()
                            .orElse(null);

                    if (parent != null) {

                        Set<ReportOperationalManagementDTO> parentMap = dtoMap.get(parent.getIdGestionOperativa());

                        if (!parent.getIdTipologia().equals(newDto.getIdTipologia())) {
                            if (parent.getProceso() != null && newDto.getProceso() == null) {
                                // Solo reemplazar si el campo proceso del hijo está vacío
                                newDto.setProceso(parent.getProceso());
                                newDto.setProcesoDescripcion(parent.getProcesoDescripcion());

                                if (parentMap != null && !parentMap.isEmpty()) {
                                    ReportOperationalManagementDTO temp = parentMap.iterator().next();
                                    if (temp.getIdActividad() == null && newDto.getProceso().equals(temp.getProceso())){
                                        dtoMap.remove(newDto.getIdGestionOperativaPadre());
                                    }
                                }
                            } else if (parent.getProcedimiento() != null && newDto.getProcedimiento() == null) {
                                // Solo reemplazar si el campo procedimiento del hijo está vacío
                                newDto.setProcedimiento(parent.getProcedimiento());
                                newDto.setProcedimientoDescripcion(parent.getProcedimientoDescripcion());

                                if (parentMap != null && !parentMap.isEmpty()) {
                                    ReportOperationalManagementDTO temp = parentMap.iterator().next();
                                    if (temp.getIdActividad() == null && newDto.getProcedimiento().equals(temp.getProcedimiento())){
                                        dtoMap.remove(newDto.getIdGestionOperativaPadre());
                                    }
                                }
                            }
                        }

                        // Continuar con el padre del nivel superior
                        currentParentId = parent.getIdGestionOperativaPadre();
                    } else {
                        break; // Detener si no hay más padres
                    }
                }
                // Agregar el nuevo DTO al mapa para asegurar no agregar duplicados por idGestionOperativa
                dtoMap.computeIfAbsent(newDto.getIdGestionOperativa(), k -> new HashSet<>()).add(newDto);

            }

            // Paso 5: Aplanar el mapa (esto solo afecta el mapa, pero no duplica en la lista final)
            dtoMap.values().stream()
                    .flatMap(Set::stream)
                    .sorted(Comparator.comparing(ReportOperationalManagementDTO::getIdGestionOperativa)) // Ordenar antes de agregar
                    .forEach(finalList::add);
        }

        return finalList;
    }


}
