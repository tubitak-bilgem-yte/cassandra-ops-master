package tr.gov.tubitak.bilgem.yte.cassandraops.cassandraopsmaster.modules.cassandraopsmaster.api.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentUtil {
    private Environment environment;

    public String getCassandraDataPath() {
        return environment.getProperty("nodeagent.backup.cassandraDataPath").trim();
    }

    public boolean getCleanOldBackups() {
        return environment.getProperty("nodeagent.backup.cleanOldBackups", Boolean.class);
    }

    @Autowired
    protected void setEnvironment(final Environment environment) {
        this.environment = environment;
    }
}
