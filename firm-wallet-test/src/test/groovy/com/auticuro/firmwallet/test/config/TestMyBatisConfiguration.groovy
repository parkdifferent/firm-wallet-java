package com.auticuro.firmwallet.test.config

import com.baomidou.mybatisplus.core.MybatisConfiguration
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean
import org.apache.ibatis.session.SqlSessionFactory
import org.apache.ibatis.type.JdbcType
import org.mybatis.spring.annotation.MapperScan
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement

import javax.sql.DataSource

@TestConfiguration
@EnableTransactionManagement
@MapperScan(basePackages = ["com.auticuro.firmwallet.query.repository"], sqlSessionFactoryRef = "testSqlSessionFactory")
class TestMyBatisConfiguration {

    @Bean
    @Primary
    DataSource testDataSource() {
        return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .addScript("classpath:schema-h2.sql")
            .build()
    }

    @Bean
    @Primary
    SqlSessionFactory testSqlSessionFactory(DataSource testDataSource) {
        MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean()
        factory.setDataSource(testDataSource)
        factory.setTypeAliasesPackage("com.auticuro.firmwallet.common.model")
        
        // Configure MyBatis-Plus
        MybatisConfiguration configuration = new MybatisConfiguration()
        configuration.setMapUnderscoreToCamelCase(true)
        configuration.setJdbcTypeForNull(JdbcType.NULL)
        factory.setConfiguration(configuration)
        
        // Add MyBatis-Plus plugins
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor()
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor())
        factory.setPlugins(interceptor)
        
        // Set mapper locations even though we're using annotations
        factory.setMapperLocations(new PathMatchingResourcePatternResolver()
            .getResources("classpath*:mapper/*.xml"))
            
        return factory.getObject()
    }
    
    @Bean
    @Primary
    PlatformTransactionManager testTransactionManager(DataSource testDataSource) {
        return new DataSourceTransactionManager(testDataSource)
    }
}
