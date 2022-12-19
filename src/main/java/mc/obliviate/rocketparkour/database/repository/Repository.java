package mc.obliviate.rocketparkour.database.repository;

import mc.obliviate.rocketparkour.config.ConfigurationHandler;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;

import java.util.*;

public abstract class Repository {

    protected Session session;

    private String host;
    private int port;
    private String username;
    private String password;
    private String database;
    private Map<String, String> properties;
    private List<Class<?>> annotatedClasses;

    public Repository(String database) {
        this(ConfigurationHandler.getConfig().getString("mysql.host"),
                ConfigurationHandler.getConfig().getInt("mysql.port"),
                ConfigurationHandler.getConfig().getString("mysql.username"),
                ConfigurationHandler.getConfig().getString("mysql.password"), database);
    }

    public Repository(String host,
                      int port,
                      String username,
                      String password,
                      String database) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.database = database;
        this.properties = new HashMap<>();
        this.annotatedClasses = new ArrayList<>();
    }

    public final Repository host(String host) {
        this.host = host;
        return this;
    }

    public final Repository port(int port) {
        this.port = port;
        return this;
    }

    public final Repository username(String username) {
        this.username = username;
        return this;
    }

    public final Repository password(String password) {
        this.password = password;
        return this;
    }

    public final Repository database(String database) {
        this.database = database;
        return this;
    }

    public final Repository annotatedClass(Class<?> clazz) {
        this.annotatedClasses.add(clazz);
        return this;
    }

    public final Repository annotatedClasses(Class<?>... classes) {
        this.annotatedClasses.addAll(Arrays.asList(classes));
        return this;
    }

    public final Repository setAnnotatedClasses(List<Class<?>> classes) {
        this.annotatedClasses = classes;
        return this;
    }

    public final Repository property(String key, String value) {
        this.properties.put(key, value);
        return this;
    }

    public final Repository setProperties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }

    public final Session getSession() {
        return this.session;
    }

    public final void connect() {
        String url = "jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database + "?createDatabaseIfNotExist=true";
        Configuration configuration= new Configuration();
        configuration.setProperty("show_sql", "true")
                .setProperty("dialect", "org.hibernate.dialect.MySQLDialect")
                .setProperty("hibernate.hbm2ddl.auto", "update")
                .setProperty("hibernate.connection.autocommit", "true")
                .setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver")
                .setProperty("hibernate.connection.url", url)
                .setProperty("hibernate.connection.username", this.username)
                .setProperty("hibernate.connection.password", this.password);

        this.properties.forEach(configuration::setProperty);
        this.annotatedClasses.forEach(configuration::addAnnotatedClass);
        this.session = configuration.buildSessionFactory().openSession();
    }
}
