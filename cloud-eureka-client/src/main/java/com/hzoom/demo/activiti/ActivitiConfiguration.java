package com.hzoom.demo.activiti;


import com.hzoom.demo.activiti.interceptor.TimeInterceptor;
import com.hzoom.demo.activiti.invoker.MDCCommandInvoker;
import com.hzoom.demo.activiti.listener.ProcessEventListener;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.interceptor.CommandInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

//@Configuration
//@AutoConfigureAfter({DataSourceAutoConfiguration.class})
public class ActivitiConfiguration extends ProcessEngineConfigurationImpl {
    @Value("${spring.datasource.druid.master.cloud0.url}")
    private String url;

    @Value("${spring.datasource.druid.master.cloud0.username}")
    private String userName;

    @Value("${spring.datasource.druid.master.cloud0.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driver;

    @Bean
    public ProcessEngine getProcessEngine() {
        buildDataSource();
        buildCommandInvoker();
        buildInterceptor();
        buildListener();
        return this.buildProcessEngine();
    }

    private void buildListener() {
        List<ActivitiEventListener> eventListeners = new ArrayList<>();
        eventListeners.add(new ProcessEventListener());
        this.setEventListeners(eventListeners);
    }

    private void buildDataSource() {
        this.setJdbcDriver(driver);
        this.setJdbcUrl(url);
        this.setJdbcUsername(userName);
        this.setJdbcPassword(password);
        this.setDatabaseSchemaUpdate("true");
        this.setAsyncExecutorActivate(false);
        this.setHistoryLevel(HistoryLevel.FULL);
    }

    private void buildCommandInvoker() {
        this.setCommandInvoker(new MDCCommandInvoker());
    }

    private void buildInterceptor(){
        List<CommandInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new TimeInterceptor());
        this.setCustomPreCommandInterceptors(interceptors);
    }

    @Override
    public CommandInterceptor createTransactionInterceptor() {
        return null;
    }

}

