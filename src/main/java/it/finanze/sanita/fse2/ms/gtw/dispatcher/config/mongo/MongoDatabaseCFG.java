package it.finanze.sanita.fse2.ms.gtw.dispatcher.config.mongo;


import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.mongodb.MongoMetricsCommandListener;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
 

/**
 * 
 * @author vincenzoingenito
 *
 *	Configuration for MongoDB.
 */
@Configuration
@EnableMongoRepositories(basePackages = Constants.ComponentScan.CONFIG_MONGO)
public class MongoDatabaseCFG {

	@Autowired
	private MongoPropertiesCFG mongoPropertiesCFG;
 
    final List<Converter<?, ?>> conversions = new ArrayList<>();

    @Autowired
    private MeterRegistry meter;
    
    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory(){
    	ConnectionString conn = new ConnectionString(mongoPropertiesCFG.getUri());
    	MongoClientSettings settings = MongoClientSettings.builder()
    			.applyConnectionString(conn)
                .addCommandListener(new MongoMetricsCommandListener(meter))
                .build();
    	
    	MongoClient client = MongoClients.create(settings);
        return new SimpleMongoClientDatabaseFactory(client, StringUtility.getDatabaseName(mongoPropertiesCFG.getUri()));
    }

    @Bean
    @Primary
    public MongoTemplate mongoTemplate() {
        final MongoDatabaseFactory factory = mongoDatabaseFactory();
        MappingMongoConverter converter =
                new MappingMongoConverter(new DefaultDbRefResolver(factory), new MongoMappingContext());
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return new MongoTemplate(factory, converter);
    }
  
 
}