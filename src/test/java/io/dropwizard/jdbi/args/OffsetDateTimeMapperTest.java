package io.dropwizard.jdbi.args;

import org.junit.jupiter.api.Test;
import org.skife.jdbi.v2.StatementContext;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OffsetDateTimeMapperTest {

    private final ResultSet resultSet = mock(ResultSet.class);
    private final StatementContext ctx = mock(StatementContext.class);

    @Test
    public void mapColumnByName() throws Exception {
        final Instant now = OffsetDateTime.of(2007, 12, 3, 10, 15, 30, 375_000_000, ZoneOffset.UTC).toInstant();

        when(resultSet.getTimestamp("name")).thenReturn(Timestamp.from(now));

        OffsetDateTime actual = new OffsetDateTimeMapper().mapColumn(resultSet, "name", ctx);

        assertThat(actual).isEqualTo(OffsetDateTime.ofInstant(now, ZoneId.systemDefault()));
    }

    @Test
    public void mapColumnByName_TimestampIsNull() throws Exception {
        when(resultSet.getTimestamp("name")).thenReturn(null);

        OffsetDateTime actual = new OffsetDateTimeMapper().mapColumn(resultSet, "name", ctx);

        assertThat(actual).isNull();
    }

    @Test
    public void mapColumnByIndex() throws Exception {
        final Instant now = OffsetDateTime.of(2007, 12, 3, 10, 15, 30, 375_000_000, ZoneOffset.UTC).toInstant();

        when(resultSet.getTimestamp(1)).thenReturn(Timestamp.from(now));

        OffsetDateTime actual = new OffsetDateTimeMapper().mapColumn(resultSet, 1, ctx);

        assertThat(actual).isEqualTo(OffsetDateTime.ofInstant(now, ZoneId.systemDefault()));
    }

    @Test
    public void mapColumnByIndex_TimestampIsNull() throws Exception {
        when(resultSet.getTimestamp(1)).thenReturn(null);

        OffsetDateTime actual = new OffsetDateTimeMapper().mapColumn(resultSet, 1, ctx);

        assertThat(actual).isNull();
    }
}
