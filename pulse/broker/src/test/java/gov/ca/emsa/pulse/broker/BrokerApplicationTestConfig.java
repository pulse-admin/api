package gov.ca.emsa.pulse.broker;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import gov.ca.emsa.pulse.broker.manager.impl.PatientQueryService;

@PropertySource("classpath:/application-test.properties")
@EnableTransactionManagement(proxyTargetClass = true)
@SpringBootApplication(scanBasePackages = {
        "gov.ca.emsa.pulse.broker.adapter", "gov.ca.emsa.pulse.broker.dao.**", "gov.ca.emsa.pulse.broker.domain.**",
        "gov.ca.emsa.pulse.broker.dto.**", "gov.ca.emsa.pulse.broker.entity.**", "gov.ca.emsa.pulse.broker.manager.**",
        "gov.ca.emsa.pulse.broker.saml.**", "gov.ca.emsa.pulse.broker.util.**", "gov.ca.emsa.pulse.broker.auth.**",
        "gov.ca.emsa.pulse.service.**", "gov.ca.emsa.pulse.common.soap.**"
})
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class BrokerApplicationTestConfig implements EnvironmentAware {

    private Environment env;

    @Override
    public void setEnvironment(final Environment e) {
        this.env = e;
    }

    @Bean
    public org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean bean = new org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean();
        bean.setPersistenceUnitName(env.getProperty("persistenceUnitName"));
        bean.setPersistenceXmlLocation("classpath*:META-INF/persistence-test.xml");
        return bean;
    }

    @Bean
    public org.springframework.orm.jpa.JpaTransactionManager transactionManager() {
        org.springframework.orm.jpa.JpaTransactionManager bean = new org.springframework.orm.jpa.JpaTransactionManager();
        bean.setEntityManagerFactory(entityManagerFactory().getObject());
        return bean;
    }

    @Bean
    public org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor persistenceAnnotationBeanPostProcessor() {
        return new org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor();
    }

    @Bean
    @Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public PatientQueryService patientQueryService() {
        return new PatientQueryService();
    }
}
