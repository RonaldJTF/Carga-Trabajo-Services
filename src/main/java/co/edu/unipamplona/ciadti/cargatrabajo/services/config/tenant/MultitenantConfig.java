package co.edu.unipamplona.ciadti.cargatrabajo.services.config.tenant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Realiza la configuración de las instancias
 * Nota: El nombre de las variables se definen en el respectivo properties del profile activo, por lo que son leídas, y luego
 * con ese nombre se trae su valor de las variables de entorno del sistema.
* */
@Configuration
public class MultitenantConfig {

    @Value("${defaultTenant}")
    private String defaultTenant;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Autowired
    private ResourceLoader resourceLoader;

    @Bean
    public DataSource dataSource() throws IOException {
        File[] files = this.getAllTenantFiles();
        Map<Object, Object> resolvedDataSources = new HashMap<>();

        for (File propertyFile : files) {
            Properties tenantProperties = new Properties();
            DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
            try {
                tenantProperties.load(new FileInputStream(propertyFile));
                String tenantId = tenantProperties.getProperty("name");
                dataSourceBuilder.driverClassName(tenantProperties.getProperty("datasource.driver-class-name"));
                dataSourceBuilder.username(tenantProperties.getProperty("datasource.username"));
                dataSourceBuilder.password(tenantProperties.getProperty("datasource.password"));
                dataSourceBuilder.url(tenantProperties.getProperty("datasource.url"));
                resolvedDataSources.put(tenantId, dataSourceBuilder.build());
            } catch (IOException exp) {
                throw new RuntimeException("Problem in tenant datasource:" + exp);
            }
        }

        AbstractRoutingDataSource dataSource = new MultitenantDataSource();
        dataSource.setDefaultTargetDataSource(resolvedDataSources.get(defaultTenant));
        dataSource.setTargetDataSources(resolvedDataSources);

        dataSource.afterPropertiesSet();
        return dataSource;
    }

    private File[] getAllTenantFiles() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:" + activeProfile + "/allTenants");
        File tenantDirectory = resource.getFile();
        return tenantDirectory.listFiles();
    }
}