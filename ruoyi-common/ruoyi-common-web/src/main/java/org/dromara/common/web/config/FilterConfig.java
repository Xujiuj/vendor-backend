package org.dromara.common.web.config;

import jakarta.servlet.DispatcherType;
import org.dromara.common.web.config.properties.OpenApiProtectionProperties;
import org.dromara.common.web.config.properties.XssProperties;
import org.dromara.common.web.filter.OpenApiProtectionFilter;
import org.dromara.common.web.filter.OpenApiProtectionStore;
import org.dromara.common.web.filter.RepeatableFilter;
import org.dromara.common.web.filter.RedisOpenApiProtectionStore;
import org.dromara.common.web.filter.XssFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * Filter配置
 *
 * @author Lion Li
 */
@AutoConfiguration
@EnableConfigurationProperties({XssProperties.class, OpenApiProtectionProperties.class})
public class FilterConfig {

    @Bean
    @ConditionalOnMissingBean
    public OpenApiProtectionStore openApiProtectionStore() {
        return new RedisOpenApiProtectionStore();
    }

    @Bean
    @ConditionalOnProperty(value = "open-api.protection.enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<OpenApiProtectionFilter> openApiProtectionFilter(
        OpenApiProtectionProperties properties, OpenApiProtectionStore store) {
        FilterRegistrationBean<OpenApiProtectionFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new OpenApiProtectionFilter(properties, store));
        registration.addUrlPatterns(properties.getPathPattern());
        registration.setName("openApiProtectionFilter");
        registration.setOrder(FilterRegistrationBean.HIGHEST_PRECEDENCE);
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        return registration;
    }

    @Bean
    @ConditionalOnProperty(value = "xss.enabled", havingValue = "true")
    @FilterRegistration(
        name = "xssFilter",
        urlPatterns = "/*",
        order = FilterRegistrationBean.HIGHEST_PRECEDENCE + 1,
        dispatcherTypes = DispatcherType.REQUEST
    )
    public XssFilter xssFilter() {
        return new XssFilter();
    }

    @Bean
    @FilterRegistration(name = "repeatableFilter", urlPatterns = "/*")
    public RepeatableFilter repeatableFilter() {
        return new RepeatableFilter();
    }

}
