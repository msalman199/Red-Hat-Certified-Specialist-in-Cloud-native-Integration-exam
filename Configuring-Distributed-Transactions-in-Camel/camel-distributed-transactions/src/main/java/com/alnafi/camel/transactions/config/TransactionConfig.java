package com.alnafi.camel.transactions.config;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import org.apache.activemq.ActiveMQXAConnectionFactory;
import org.apache.camel.component.jms.JmsComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.jms.ConnectionFactory;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;
import java.util.Properties;

@Configuration
public class TransactionConfig {

    @Bean(name = "atomikosTransactionManager")
    @Primary
    public JtaTransactionManager transactionManager() throws Exception {
        UserTransactionManager userTransactionManager = new UserTransactionManager();
        userTransactionManager.setForceShutdown(false);
        
        UserTransaction userTransaction = new UserTransactionImp();
        
        JtaTransactionManager jtaTransactionManager = new JtaTransactionManager();
        jtaTransactionManager.setTransactionManager(userTransactionManager);
        jtaTransactionManager.setUserTransaction(userTransaction);
        
        return jtaTransactionManager;
    }

    @Bean(name = "xaDataSource")
    public DataSource xaDataSource() {
        AtomikosDataSourceBean dataSource = new AtomikosDataSourceBean();
        dataSource.setUniqueResourceName("h2XADataSource");
        dataSource.setXaDataSourceClassName("org.h2.jdbcx.JdbcDataSource");
        
        Properties properties = new Properties();
        properties.setProperty("URL", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        properties.setProperty("user", "sa");
        properties.setProperty("password", "password");
        
        dataSource.setXaProperties(properties);
        dataSource.setPoolSize(5, 10);
        
        return dataSource;
    }

    @Bean(name = "jmsConnectionFactory")
    public ConnectionFactory jmsConnectionFactory() {
        ActiveMQXAConnectionFactory connectionFactory = new ActiveMQXAConnectionFactory();
        connectionFactory.setBrokerURL("vm://localhost?broker.persistent=false");
        connectionFactory.setUserName("admin");
        connectionFactory.setPassword("admin");
        return connectionFactory;
    }

    @Bean(name = "jms")
    public JmsComponent jmsComponent() throws Exception {
        JmsComponent jmsComponent = new JmsComponent();
        jmsComponent.setConnectionFactory(jmsConnectionFactory());
        jmsComponent.setTransactionManager(transactionManager());
        jmsComponent.setTransacted(true);
        return jmsComponent;
    }
}
