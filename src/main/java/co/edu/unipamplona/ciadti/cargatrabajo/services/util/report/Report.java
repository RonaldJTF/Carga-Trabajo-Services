package co.edu.unipamplona.ciadti.cargatrabajo.services.util.report;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Methods;
import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Trace;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

@RequiredArgsConstructor
@Service
public class Report {
    private final ResourceLoader resourceLoader;

    public byte[] converterToPDF(Map<String, Object> parameters, JRBeanCollectionDataSource dataSource, String filePath) throws CiadtiException {
        JasperReport jasperReport;
        JRPdfExporter exporter;
        ByteArrayOutputStream outputStream;
        byte[] fileBytes = null;
        try {
            //jasperReport = JasperCompileManager.compileReport(getClass().getClassLoader().getResourceAsStream(filePath));
            Resource resource = resourceLoader.getResource("classpath:" + filePath);
            File file = resource.getFile();
            jasperReport = (JasperReport) JRLoader.loadObject(file);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            exporter = new JRPdfExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            outputStream = new ByteArrayOutputStream();
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
            exporter.exportReport();
            fileBytes = outputStream.toByteArray();
        } catch (JRException | IOException e) {
            Trace.logError(this.getClass().getName(), Methods.getCurrentMethodName(this.getClass()), e);
            throw new CiadtiException("Ha ocurrido un error al generar el reporte", 500);
        }
        return fileBytes;
    }
}
