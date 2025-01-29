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
     * 1. Obtiene los datos sin procesar (rawData) desde el servicio {@code gestionOperativaService} para los
     *    IDs de organigrama proporcionados.
     * 2. Convierte los datos sin procesar en una lista de objetos {@link ReportOperationalManagementDTO}
     *    utilizando el método {@link #mapRawDataToDTOs(List)}.
     * 3. Agrupa los datos procesados por dependencias y calcula la jerarquía interna utilizando el método
     *    {@link #processGroupedByDependencies(List)}.
     * 4. Calcula los niveles de profundidad de cada ítem en la lista de DTOs utilizando el método
     *    {@link #calculateDepthLevels(List)}.
     * 5. Construye la estructura de datos necesaria para el informe utilizando el método {@link #buildStructureData(List)}.
     * 6. Almacena los datos procesados en el registro (`registry`) bajo la clave {@code "dataOperationalManagement"}.
     * 7. Retorna la lista de DTOs procesados, que contiene los datos listos para ser utilizados en la construcción del informe.
     *
     * @param organizationChartIds Lista de IDs de organigrama para los cuales se generará el informe.
     * @return Una lista de objetos {@link ReportOperationalManagementDTO} que representan los datos procesados
     *         del informe, listos para ser utilizados en la generación del reporte.
     */
    private List<ReportOperationalManagementDTO> fetchAndProcessReportData(Long organizationChartIds) {
        List<Object[]> rawData = gestionOperativaService.findOperationalManagementByOrganizationChart(organizationChartIds);
        List<ReportOperationalManagementDTO> resultDTO = mapRawDataToDTOs(rawData);
        List<ReportOperationalManagementDTO> reportData = processGroupedByDependencies(resultDTO);
        calculateDepthLevels(reportData);
        buildStructureData(reportData);
        registry.put("dataOperationalManagement", reportData);
        return reportData;
    }

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
            ReportOperationalManagementDTO parent = activityMap.get(dto.getIdGestionOperativaPadre());
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

    /**
     * Convierte una lista de arreglos de objetos en una lista de objetos {@link ReportOperationalManagementDTO}.
     * Este método realiza las siguientes acciones:
     * 1. Crea una nueva lista vacía para almacenar los objetos DTO procesados.
     * 2. Itera sobre cada arreglo de objetos (`result`) en la lista de entrada (`resultList`).
     * 3. Para cada arreglo, llama al método {@link #createDTOFromResult(Object[])} para crear un objeto DTO.
     * 4. Agrega el objeto DTO generado a la lista.
     * 5. Retorna la lista completa de objetos DTO procesados.
     *
     * @param resultList Lista de arreglos de objetos que contienen los datos crudos a procesar.
     *                   Cada arreglo representa una fila de datos obtenida de una consulta.
     * @return Una lista de objetos {@link ReportOperationalManagementDTO} mapeados desde los datos crudos.
     */
    private List<ReportOperationalManagementDTO> mapRawDataToDTOs(List<Object[]> resultList) {
        List<ReportOperationalManagementDTO> report = new ArrayList<>();
        for (Object[] result : resultList) {
            ReportOperationalManagementDTO dto = createDTOFromResult(result);
            report.add(dto);
        }
        return report;
    }

    /**
     * Crea y retorna un objeto {@link ReportOperationalManagementDTO} a partir de un arreglo de datos.
     * Este método realiza las siguientes acciones:
     * 1. Crea una nueva instancia de {@link ReportOperationalManagementDTO}.
     * 2. Mapea los campos comunes al DTO utilizando el método {@link #mapCommonFields(ReportOperationalManagementDTO, Object[])}.
     * 3. Mapea los campos específicos de tipología al DTO utilizando el método {@link #mapTipologiaFields(ReportOperationalManagementDTO, Object[])}.
     * 4. Retorna el objeto {@link ReportOperationalManagementDTO} completamente mapeado.
     *
     * @param result Un arreglo de objetos que contiene los datos sin procesar necesarios para construir el DTO.
     *               Cada posición del arreglo representa un atributo del DTO.
     * @return Un objeto {@link ReportOperationalManagementDTO} que contiene los datos mapeados desde el arreglo.
     */
    private ReportOperationalManagementDTO createDTOFromResult(Object[] result) {
        ReportOperationalManagementDTO dto = new ReportOperationalManagementDTO();

        mapCommonFields(dto, result);
        mapTipologiaFields(dto, result);

        return dto;
    }

    /**
     * Mapea los campos comunes desde un arreglo de datos al objeto {@link ReportOperationalManagementDTO}.
     * Este método realiza las siguientes acciones:
     * 1. Extrae los valores necesarios del arreglo de datos `result` utilizando índices específicos.
     * 2. Convierte los valores sin procesar a los tipos correspondientes (Long, String, Double) usando métodos auxiliares
     *    como {@link #getLongValue(Object)} y {@link #getDoubleValue(Object)}.
     * 3. Asigna los valores extraídos a los atributos del objeto {@link ReportOperationalManagementDTO}.
     *
     * @param dto    El objeto {@link ReportOperationalManagementDTO} donde se asignarán los datos mapeados.
     * @param result Un arreglo de objetos que contiene los datos sin procesar necesarios para mapear los campos comunes del DTO.
     *               Cada posición del arreglo representa un atributo específico.
     */
    private void mapCommonFields(ReportOperationalManagementDTO dto, Object[] result) {
        dto.setIdGestionOperativa(getLongValue(result[0]));
        dto.setIdGestionOperativaPadre(getLongValue(result[3]));
        dto.setDependencia((String) result[6]);
        dto.setOrganigrama((String) result[7]);
        dto.setOrganigramaDescripcion((String) result[8]);
        dto.setIdActividad(getLongValue(result[9]));
        dto.setIdNivel(getLongValue(result[10]));
        dto.setFrecuencia(getDoubleValue(result[11]));
        dto.setTiempoMaximo(getDoubleValue(result[12]));
        dto.setTiempoMinimo(getDoubleValue(result[13]));
        dto.setTiempoPromedio(getDoubleValue(result[14]));
        dto.setNivel((String) result[15]);
        dto.setIdTipologia(getLongValue(result[4]));
        dto.setTipologia((String) result[16]);
    }

    /**
     * Mapea los campos relacionados con la tipología desde un arreglo de datos al objeto {@link ReportOperationalManagementDTO}.
     * Este método realiza las siguientes acciones:
     * 1. Extrae los valores de los campos `geopNombre`, `geopDescripcion`, `idTipologia` y `tipologia` desde el arreglo `result`.
     * 2. Verifica si `idTipologia` no es nulo antes de realizar el mapeo.
     * 3. Dependiendo del valor del campo `tipologia`, asigna los valores extraídos a los atributos correspondientes
     *    del objeto {@link ReportOperationalManagementDTO}.
     *
     * @param dto    El objeto {@link ReportOperationalManagementDTO} donde se asignarán los datos mapeados.
     * @param result Un arreglo de objetos que contiene los datos sin procesar necesarios para mapear los campos de tipología.
     *               Las posiciones relevantes en el arreglo son:
     *               - `result[1]`: Nombre gestión operativa (geopNombre).
     *               - `result[2]`: Descripción gestión operativa (geopDescripcion).
     *               - `result[4]`: ID de tipología (idTipologia).
     *               - `result[16]`: Nombre de la tipología (tipologia).
     */
    private void mapTipologiaFields(ReportOperationalManagementDTO dto, Object[] result) {
        String geopNombre = (String) result[1];
        String geopDescripcion = (String) result[2];
        Long idTipologia = getLongValue(result[4]);
        String tipologia = (String) result[16];

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
    }

    private Long getLongValue(Object value) {
        return value != null ? ((Number) value).longValue() : null;
    }

    private Double getDoubleValue(Object value) {
        return value != null ? ((Number) value).doubleValue() : null;
    }

    /**
     * Procesa una lista plana de objetos {@link ReportOperationalManagementDTO} agrupándolos por la dependencia a la que pertenecen.
     * El objetivo es completar la información de cada elemento de la lista llenando el resto de atributos (`Proceso`, `Procedimiento` o `Actividad`)
     * de los elementos de la lista siguiendo la estructura jerárquica de las gestiones operativas, cuya jerarquía sigue el orden:
     * **Proceso -> Procedimiento -> Actividad**. Esta estructura jerárquica es utilizada para la construcción del reporte.
     *
     * Cada elemento en la lista tiene una tipología específica (`Proceso`, `Procedimiento` o `Actividad`), lo que determina
     * qué atributos del elemento contienen información. Por ejemplo:
     * - Si un elemento tiene la tipología `Actividad`, los atributos `actividad` y `actividadDescripcion` contienen la información correspondiente.
     * - Si un elemento tiene la tipología `Procedimiento`, los atributos `procedimiento` y `procedimientoDescripcion` están completos.
     * - Si un elemento tiene la tipología `Proceso`, los atributos `proceso` y `procesoDescripcion` contienen la información.
     *
     * Este método realiza las siguientes acciones:
     * 1. Agrupa los objetos {@link ReportOperationalManagementDTO} por el campo `dependencia`, creando grupos basados en este atributo.
     * 2. Para cada grupo:
     *    - Ordena los elementos por el campo `idGestionOperativa` para mantener un orden lógico dentro del grupo.
     *    - Completa la información jerárquica del grupo utilizando el método {@link #processGroupHierarchy(List, String)}.
     * 3. Aplana la estructura jerárquica generada y la agrega a una lista final utilizando el método {@link #flattenDtoMap(Map, List)}.
     * 4. Retorna la lista procesada, que contiene los datos estructurados y listos para la construcción del reporte.
     *
     * @param flatList Una lista plana de objetos {@link ReportOperationalManagementDTO} que representan los datos iniciales sin procesar.
     * @return Una lista de objetos {@link ReportOperationalManagementDTO} lista para ser utilizada en la construcción del reporte.
     */
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

            Map<Long, Set<ReportOperationalManagementDTO>> dtoMap = processGroupHierarchy(group, dependencia);

            // Paso 4: Aplanar el mapa y agregar a la lista final
            flattenDtoMap(dtoMap, finalList);
        }
        return finalList;
    }

    /**
     * Procesa un grupo de objetos {@link ReportOperationalManagementDTO} y genera un mapa que organiza los elementos
     * con su información completa en función de sus relaciones padre-hijo utilizando en el atributo `idGestionOperativaPadre`.
     * Este método realiza las siguientes acciones:
     * 1. Itera sobre cada objeto del grupo y crea un nuevo objeto {@link ReportOperationalManagementDTO}
     *    con base en la información existente, utilizando el método {@link #createNewDto(ReportOperationalManagementDTO, String)}.
     * 2. Vincula cada elemento con su jerarquía padre-hijo utilizando el método {@link #processParentHierarchy(ReportOperationalManagementDTO, ReportOperationalManagementDTO, List, Map)}.
     * 3. Almacena cada elemento en el mapa jerárquico `dtoMap`, donde la clave es el ID del objeto (`idGestionOperativa`)
     *    y el valor es un conjunto de elementos relacionados con dicho ID.
     *
     * @param group Lista de objetos {@link ReportOperationalManagementDTO} pertenecientes a un grupo específico de dependencia.
     * @param dependencia La dependencia a la que pertenece el grupo procesado.
     * @return Un mapa jerárquico donde la clave es el ID del elemento de gestión operativa y el valor es un conjunto de elementos relacionados.
     */
    private Map<Long, Set<ReportOperationalManagementDTO>> processGroupHierarchy(List<ReportOperationalManagementDTO> group, String dependencia) {
        Map<Long, Set<ReportOperationalManagementDTO>> dtoMap = new HashMap<>();

        for (ReportOperationalManagementDTO dto : group) {
            ReportOperationalManagementDTO newDto = createNewDto(dto, dependencia);
            processParentHierarchy(dto, newDto, group, dtoMap);
            dtoMap.computeIfAbsent(newDto.getIdGestionOperativa(), k -> new HashSet<>()).add(newDto);
        }

        return dtoMap;
    }

    /**
     * Crea una nueva instancia de {@link ReportOperationalManagementDTO} copiando los datos del DTO original
     * y asignando la dependencia correspondiente. Este método se utiliza para generar una representación
     * individual de un elemento de gestión operativa.
     * Este método realiza las siguientes acciones:
     * 1. Crea una nueva instancia de {@link ReportOperationalManagementDTO}.
     * 2. Copia todos los campos del objeto original al nuevo objeto, incluyendo:
     *    - Información básica como ID, actividad, dependencia, nivel, tipología, proceso y procedimiento.
     *    - Tiempos asociados al elemento, utilizando el método auxiliar {@link #copyTimes(ReportOperationalManagementDTO, ReportOperationalManagementDTO)}.
     * 3. Asigna la dependencia proporcionada al nuevo objeto.
     *
     * @param dto El objeto original de tipo {@link ReportOperationalManagementDTO} del cual se copiarán los datos.
     * @param dependencia La dependencia a asignar al nuevo objeto.
     * @return Un nuevo objeto {@link ReportOperationalManagementDTO} con los datos copiados y la dependencia asignada.
     */
    private ReportOperationalManagementDTO createNewDto(ReportOperationalManagementDTO dto, String dependencia) {
        ReportOperationalManagementDTO newDto = new ReportOperationalManagementDTO();

        // Copiar todos los campos del hijo
        newDto.setIdGestionOperativa(dto.getIdGestionOperativa());
        newDto.setIdActividad(dto.getIdActividad());
        newDto.setActividad(dto.getActividad() != null ? dto.getActividad() : "");
        newDto.setDependencia(dependencia);
        newDto.setNivel(dto.getNivel());
        newDto.setIdNivel(dto.getIdNivel());
        newDto.setIdGestionOperativaPadre(dto.getIdGestionOperativaPadre());
        newDto.setIdTipologia(dto.getIdTipologia());
        newDto.setTipologia(dto.getTipologia());
        newDto.setProceso(dto.getProceso());
        newDto.setProcesoDescripcion(dto.getProcesoDescripcion());
        newDto.setProcedimiento(dto.getProcedimiento());
        newDto.setProcedimientoDescripcion(dto.getProcedimientoDescripcion());

        // Copiar los tiempos del hijo (si existen)
        if (dto.getIdActividad() != null)
            copyTimes(dto, newDto);

        return newDto;
    }

    /**
     * Copia los valores de los campos relacionados con el tiempo desde el objeto fuente
     * ({@link ReportOperationalManagementDTO}) al objeto destino. Este método asegura que los valores de tiempo
     * como el tiempo mínimo, máximo, promedio, estándar y frecuencia sean asignados si están disponibles
     * en el objeto fuente.
     * Este método realiza las siguientes acciones:
     * 1. Copia al objeto destino los campos `tiempoMinimo`, `tiempoMaximo`, `tiempoPromedio`, `tiempoEstandar` y `frecuencia`.
     *
     * @param source El objeto fuente de tipo {@link ReportOperationalManagementDTO} desde el cual se copiarán los valores de tiempo.
     * @param target El objeto destino de tipo {@link ReportOperationalManagementDTO} al cual se asignarán los valores de tiempo.
     */
    private void copyTimes(ReportOperationalManagementDTO source, ReportOperationalManagementDTO target) {
        target.setTiempoMinimo(source.getTiempoMinimo());
        target.setTiempoMaximo(source.getTiempoMaximo());
        target.setTiempoPromedio(source.getTiempoPromedio());
        target.setTiempoEstandar(source.getTiempoEstandar());
        target.setFrecuencia(source.getFrecuencia());
    }

    /**
     * Procesa la jerarquía de padres para un objeto {@link ReportOperationalManagementDTO} dado. Este método
     * recorre la jerarquía de padres utilizando el campo `idGestionOperativaPadre` del objeto actual y va buscando
     * en el grupo de objetos para actualizar el DTO hijo con la información de sus padres.
     * El proceso se detiene cuando no se encuentra más información de padres o cuando el campo `idGestionOperativaPadre`
     * es nulo.
     * Este método realiza las siguientes acciones:
     * 1. Obtiene el `idGestionOperativaPadre` del objeto actual (`dto`).
     * 2. En un bucle, busca al padre correspondiente utilizando el método {@link #findParentById(Long, List)}.
     * 3. Si se encuentra un padre, se actualiza el DTO hijo con la información del padre utilizando el método
     *    {@link #updateDtoFromParent(ReportOperationalManagementDTO, ReportOperationalManagementDTO, Map)}.
     * 4. El proceso continúa hasta que se alcanza un nivel donde no hay más padres o el `idGestionOperativaPadre` es nulo.
     *
     * @param dto El objeto hijo de tipo {@link ReportOperationalManagementDTO} que se está procesando.
     * @param newDto El objeto hijo modificado que se va actualizando con la información de los padres.
     * @param group La lista de objetos {@link ReportOperationalManagementDTO} del grupo, utilizada para encontrar los padres.
     * @param dtoMap El mapa que contiene los objetos de tipo {@link ReportOperationalManagementDTO} organizados por su ID
     */
    private void processParentHierarchy(ReportOperationalManagementDTO dto, ReportOperationalManagementDTO newDto, List<ReportOperationalManagementDTO> group, Map<Long, Set<ReportOperationalManagementDTO>> dtoMap) {
        Long currentParentId = dto.getIdGestionOperativaPadre();

        while (currentParentId != null) {
            ReportOperationalManagementDTO parent = findParentById(currentParentId, group);
            if (parent != null) {
                updateDtoFromParent(newDto, parent, dtoMap);
                currentParentId = parent.getIdGestionOperativaPadre();
            } else {
                break;
            }
        }
    }

    /**
     * Busca un padre en el grupo de objetos {@link ReportOperationalManagementDTO} utilizando el
     * `idGestionOperativa` correspondiente al `parentId`. Este método realiza una búsqueda en la lista de
     * objetos y retorna el primer padre encontrado cuyo `idGestionOperativa` coincida con el `parentId` proporcionado.
     * Si no se encuentra un padre con ese ID, se retorna `null`.
     * Este método realiza las siguientes acciones:
     * 1. Filtra la lista `group` de objetos {@link ReportOperationalManagementDTO} buscando un objeto cuyo
     *    `idGestionOperativa` coincida con el `parentId`.
     * 2. Si se encuentra un objeto que coincida, lo retorna.
     * 3. Si no se encuentra ningún objeto que coincida, retorna `null`.
     *
     * @param parentId El ID del padre que se busca en el grupo de objetos.
     * @param group La lista de objetos {@link ReportOperationalManagementDTO} en la que se realizará la búsqueda.
     * @return El objeto {@link ReportOperationalManagementDTO} que corresponde al padre encontrado, o `null` si no se encuentra.
     */
    private ReportOperationalManagementDTO findParentById(Long parentId, List<ReportOperationalManagementDTO> group) {
        return group.stream()
                .filter(d -> d.getIdGestionOperativa().equals(parentId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Actualiza un objeto {@link ReportOperationalManagementDTO} con información proveniente de su objeto padre,
     * si el padre tiene información adicional que falta en el objeto hijo (representado por `newDto`).
     * Si el padre tiene información relevante en los campos `proceso` o `procedimiento`, y el objeto hijo
     * no tiene estos campos completos, se actualizan los valores correspondientes del hijo.
     * Además, se verifica y se elimina el objeto padre del mapa `dtoMap` si corresponde.
     * Este método realiza las siguientes acciones:
     * 1. Verifica si la tipología del padre y del hijo son diferentes.
     * 2. Si el tipo es diferente y el padre tiene información relevante en el campo `proceso`,
     *    pero el hijo no, se copia esa información al hijo.
     * 3. Si el padre tiene información en el campo `procedimiento`, pero el hijo no, se copia esa información al hijo.
     * 4. Después de actualizar el hijo con la información del padre, se realiza una comprobación
     *    y posible eliminación del padre en el mapa `dtoMap` mediante el método {@link #checkAndRemoveFromMap(ReportOperationalManagementDTO, Map, ReportOperationalManagementDTO)}.
     *
     * @param newDto El objeto {@link ReportOperationalManagementDTO} que se va a actualizar con los datos del padre.
     * @param parent El objeto {@link ReportOperationalManagementDTO} que contiene los datos que se van a copiar al hijo.
     * @param dtoMap El mapa que contiene los objetos {@link ReportOperationalManagementDTO}, utilizado para verificar
     *               y eliminar el padre cuando se actualiza la información del hijo con los datos del padre.
     */
    private void updateDtoFromParent(ReportOperationalManagementDTO newDto, ReportOperationalManagementDTO parent, Map<Long, Set<ReportOperationalManagementDTO>> dtoMap) {
        if (!parent.getIdTipologia().equals(newDto.getIdTipologia())) {
            if (parent.getProceso() != null && newDto.getProceso() == null) {
                newDto.setProceso(parent.getProceso());
                newDto.setProcesoDescripcion(parent.getProcesoDescripcion());
                checkAndRemoveFromMap(newDto, dtoMap, parent);
            } else if (parent.getProcedimiento() != null && newDto.getProcedimiento() == null) {
                newDto.setProcedimiento(parent.getProcedimiento());
                newDto.setProcedimientoDescripcion(parent.getProcedimientoDescripcion());
                checkAndRemoveFromMap(newDto, dtoMap, parent);
            }
        }
    }

    /**
     * Verifica si un objeto {@link ReportOperationalManagementDTO} debe ser eliminado del mapa {@code dtoMap}
     * basado en ciertos criterios. Si el objeto hijo cumple con la condición definida en el método
     * {@link #shouldRemoveFromMap(ReportOperationalManagementDTO, ReportOperationalManagementDTO)},
     * el objeto padre se elimina del mapa utilizando el `idGestionOperativaPadre` del hijo.
     * Este método realiza las siguientes acciones:
     * 1. Obtiene el conjunto de objetos {@link ReportOperationalManagementDTO} asociado al `idGestionOperativa` del padre
     *    en el mapa {@code dtoMap}.
     * 2. Si el objeto no está vacío, se verifica si el objeto padre debe ser eliminado del mapa utilizando el método
     *    {@link #shouldRemoveFromMap(ReportOperationalManagementDTO, ReportOperationalManagementDTO)}.
     * 3. Si la condición es verdadera, el objeto hijo se elimina del mapa utilizando su `idGestionOperativaPadre`.
     *
     * @param newDto El objeto {@link ReportOperationalManagementDTO} que representa al hijo que se evaluará para
     *               eliminación del padre del mapa.
     * @param dtoMap El mapa que contiene los objetos {@link ReportOperationalManagementDTO}, utilizado para verificar
     *               y eliminar el objeto padre si corresponde.
     * @param parent El objeto {@link ReportOperationalManagementDTO} que representa al padre.
     */
    private void checkAndRemoveFromMap(ReportOperationalManagementDTO newDto, Map<Long, Set<ReportOperationalManagementDTO>> dtoMap, ReportOperationalManagementDTO parent) {
        Set<ReportOperationalManagementDTO> parentMap = dtoMap.get(parent.getIdGestionOperativa());
        if (parentMap != null && !parentMap.isEmpty()) {
            ReportOperationalManagementDTO temp = parentMap.iterator().next();
            if (shouldRemoveFromMap(temp, newDto)) {
                dtoMap.remove(newDto.getIdGestionOperativaPadre());
            }
        }
    }

    /**
     * Determina si un objeto {@link ReportOperationalManagementDTO} debe ser eliminado del mapa {@code dtoMap}
     * en base a ciertos criterios. La eliminación se realiza si el objeto hijo cumple con una condición definida
     * según su tipología (proceso o procedimiento) y si la información del objeto hijo coincide con la del objeto
     * padre en el mapa.
     * Este método evalúa las siguientes condiciones:
     * 1. Si el objeto `temp` de tipo {@link ReportOperationalManagementDTO} tiene un `idActividad` no nulo,
     *    se devuelve {@code false}, indicando que no debe ser eliminado.
     * 2. Si la tipología de `temp` es "Proceso", se compara el campo `proceso` del objeto hijo con el del objeto padre.
     *    Si coinciden, se devuelve {@code true} (el objeto debe eliminarse).
     * 3. Si la tipología de `temp` es "Procedimiento", se compara el campo `procedimiento` del objeto hijo con el del objeto padre.
     *    Si coinciden, se devuelve {@code true} (el objeto debe eliminarse).
     * 4. Si ninguna de las condiciones anteriores se cumple, se devuelve {@code false}.
     *
     * @param temp El objeto {@link ReportOperationalManagementDTO} que representa al padre en el mapa, usado para la comparación.
     * @param newDto El objeto {@link ReportOperationalManagementDTO} que representa al hijo que se evaluará para eliminación.
     * @return {@code true} si el objeto hijo debe ser eliminado del mapa, {@code false} si no.
     */
    private boolean shouldRemoveFromMap(ReportOperationalManagementDTO temp, ReportOperationalManagementDTO newDto) {
        if (temp.getIdActividad() != null) {
            return false;
        }

        return switch (temp.getTipologia()) {
            case "Proceso" -> newDto.getProceso().equals(temp.getProceso());
            case "Procedimiento" -> newDto.getProcedimiento().equals(temp.getProcedimiento());
            default -> false;
        };
    }

    /**
     * Aplana un mapa de objetos {@link ReportOperationalManagementDTO} agrupados por su `idGestionOperativa`
     * y agrega los elementos a una lista final, manteniendo el orden lógico de los objetos en función del
     * campo `idGestionOperativa`.
     * Este método realiza las siguientes acciones:
     * 1. Toma todos los valores del mapa {@code dtoMap}, los cuales son conjuntos de objetos {@link ReportOperationalManagementDTO}.
     * 2. Aplana los conjuntos de valores, convirtiéndolos en una secuencia continua de objetos.
     * 3. Ordena los objetos aplanados en función del campo {@code idGestionOperativa}, asegurando que los elementos
     *    estén en un orden lógico basado en este campo.
     * 4. Agrega los objetos ordenados a la lista {@code finalList}.
     *
     * @param dtoMap El mapa que contiene conjuntos de objetos {@link ReportOperationalManagementDTO}, donde la clave
     *               es un identificador único de gestión operativa y los valores son los conjuntos de objetos relacionados.
     * @param finalList La lista en la que se agregarán los objetos aplanados y ordenados.
     */
    private void flattenDtoMap(Map<Long, Set<ReportOperationalManagementDTO>> dtoMap, List<ReportOperationalManagementDTO> finalList) {
        dtoMap.values().stream()
                .flatMap(Set::stream)
                .sorted(Comparator.comparing(ReportOperationalManagementDTO::getIdGestionOperativa))
                .forEach(finalList::add);
    }
}
