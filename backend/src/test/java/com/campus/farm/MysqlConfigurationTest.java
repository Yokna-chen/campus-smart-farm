package com.campus.farm;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

class MysqlConfigurationTest {
  @Test void mysqlProfileUsesDocumentedDatabaseEnvironmentVariables() {
    YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
    yaml.setResources(new ClassPathResource("application-mysql.yml"));
    Properties properties = yaml.getObject();

    assertThat(properties.getProperty("spring.datasource.url")).startsWith("${DB_URL:");
    assertThat(properties.getProperty("spring.datasource.username")).startsWith("${DB_USERNAME:");
    assertThat(properties.getProperty("spring.datasource.password")).startsWith("${DB_PASSWORD:");
  }
}
