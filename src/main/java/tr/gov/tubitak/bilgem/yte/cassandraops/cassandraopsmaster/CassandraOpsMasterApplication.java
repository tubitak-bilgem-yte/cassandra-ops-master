package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CassandraOpsMasterApplication {

    public static void main(final String[] args) {
        SpringApplication.run(CassandraOpsMasterApplication.class, args);
    }
}
