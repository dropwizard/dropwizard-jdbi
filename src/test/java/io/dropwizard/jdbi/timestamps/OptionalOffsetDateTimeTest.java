package io.dropwizard.jdbi.timestamps;

import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class OptionalOffsetDateTimeTest {

    private final Environment env = new Environment("test-optional-offset-date-time");

    private TaskDao dao;

    @BeforeEach
    public void setupTests() throws IOException {
        final DataSourceFactory dataSourceFactory = new DataSourceFactory();
        dataSourceFactory.setDriverClass("org.h2.Driver");
        dataSourceFactory.setUrl("jdbc:h2:mem:optional-offset-date-time-" + System.currentTimeMillis() + "?user=sa");
        dataSourceFactory.setInitialSize(1);
        final DBI dbi = new DBIFactory().build(env, dataSourceFactory, "test");
        try (Handle h = dbi.open()) {
            h.execute("CREATE TABLE IF NOT EXISTS tasks (" +
                    "id INT PRIMARY KEY, " +
                    "assignee VARCHAR(255) NOT NULL, " +
                    "start_date TIMESTAMP, " +
                    "end_date TIMESTAMP, " +
                    "comments VARCHAR(1024) " +
                    ")");
        }
        dao = dbi.onDemand(TaskDao.class);
    }

    @Test
    public void testPresent() {
        final OffsetDateTime startDate = OffsetDateTime.of(2007, 12, 3, 10, 15, 30, 375_000_000, ZoneOffset.UTC);
        final OffsetDateTime endDate = startDate.plusDays(1L);
        dao.insert(1, Optional.of("John Hughes"), startDate, Optional.of(endDate), Optional.empty());

        assertThat(dao.findEndDateById(1).get()).isEqualTo(endDate);
    }

    @Test
    public void testAbsent() {
        dao.insert(2, Optional.of("Kate Johansen"), OffsetDateTime.of(2007, 12, 3, 10, 15, 30, 375_000_000, ZoneOffset.UTC),
                Optional.empty(), Optional.of("To be done"));

        assertThat(dao.findEndDateById(2).isPresent()).isFalse();
    }

    interface TaskDao {

        @SqlUpdate("INSERT INTO tasks(id, assignee, start_date, end_date, comments) " +
                "VALUES (:id, :assignee, :start_date, :end_date, :comments)")
        void insert(@Bind("id") int id, @Bind("assignee") Optional<String> assignee,
                    @Bind("start_date") OffsetDateTime startDate, @Bind("end_date") Optional<OffsetDateTime> endDate,
                    @Bind("comments") Optional<String> comments);

        @SqlQuery("SELECT end_date FROM tasks WHERE id = :id")
        @SingleValueResult
        Optional<OffsetDateTime> findEndDateById(@Bind("id") int id);
    }
}
