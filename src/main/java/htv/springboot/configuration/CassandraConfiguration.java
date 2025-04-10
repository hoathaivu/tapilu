package htv.springboot.configuration;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.cassandra.CassandraProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.cassandra.CassandraManagedTypes;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.core.convert.CassandraCustomConversions;
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification;
import org.springframework.data.cassandra.core.cql.keyspace.KeyspaceOption;
import org.springframework.data.cassandra.core.mapping.CassandraMappingContext;
import org.springframework.data.cassandra.core.mapping.NamingStrategy;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Configuration
@AllArgsConstructor
public class CassandraConfiguration extends AbstractCassandraConfiguration {

    private final CassandraProperties properties;

    @Override
    public CassandraMappingContext cassandraMappingContext(CassandraManagedTypes cassandraManagedTypes) {
        CassandraMappingContext context = super.cassandraMappingContext(cassandraManagedTypes);
        context.setNamingStrategy(NamingStrategy.SNAKE_CASE);

        return context;
    }

    @Override
    public CassandraCustomConversions customConversions() {
        return CassandraCustomConversions.create(config -> {
            config.registerConverter(new TimestampToOffsetConverter());
            config.registerConverter(new OffsetToTimestampConverter());
        });
    }

    @ReadingConverter
    static class TimestampToOffsetConverter implements Converter<Instant, OffsetDateTime> {

        @Override
        public OffsetDateTime convert(Instant source) {
            return source.atOffset(ZoneOffset.UTC);
        }
    }

    @WritingConverter
    static class OffsetToTimestampConverter implements Converter<OffsetDateTime, Instant> {

        @Override
        public Instant convert(OffsetDateTime source) {
            return source.toInstant();
        }
    }

    @Override
    public List<CreateKeyspaceSpecification> getKeyspaceCreations() {
        CreateKeyspaceSpecification specification = CreateKeyspaceSpecification
                .createKeyspace(getKeyspaceName())
                .with(KeyspaceOption.DURABLE_WRITES, true)
                .ifNotExists();

        return List.of(specification);
    }

    @Override
    protected String getKeyspaceName() {
        return properties.getKeyspaceName();
    }

    @Override
    protected String getLocalDataCenter() {
        return properties.getLocalDatacenter();
    }
}
