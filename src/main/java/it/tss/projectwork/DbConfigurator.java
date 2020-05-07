/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tss.projectwork;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.sql.DataSourceDefinition;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import org.flywaydb.core.Flyway;

/**
 *
 * @author alfonsodomenici
 */
@DataSourceDefinition(
        className = "com.maria.jdbc.Driver",
        name = "java:global/jdbc/MyDS",
        serverName = DbConfigurator.MYSQL_HOST,
        portNumber = DbConfigurator.MYSQL_PORT,
        user = DbConfigurator.MYSQL_USR,
        password = DbConfigurator.MYSQL_USER_PWD,
        databaseName = DbConfigurator.MYSQL_DB
)
@Singleton()
@Startup
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class DbConfigurator {

    public static final String MYSQL_HOST = "localhost";
    public static final int MYSQL_PORT = 3306;
    public static final String MYSQL_PROTOCOL = "tcp";
    public static final String MYSQL_ROOT_PWD = "root";
    public static final String MYSQL_USR = "pwapp";
    public static final String MYSQL_USER_PWD = "pwapp";
    public static final String MYSQL_DB = "projectwork";

    private String jdbcBaseUrl;

    @PostConstruct
    public void init() {
        System.out.println("----------------------- Init DbConfiguration-----------------------");
        jdbcBaseUrl = "jdbc:mariadb://" + MYSQL_HOST + ":" + MYSQL_PORT + "/";
        create();
        //migrate();
        System.out.println("----------------------- End DbConfiguration-----------------------");
    }

    public void create() {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DbConfigurator.class.getName()).log(Level.SEVERE, null, ex);
        }
        try ( Connection conn = DriverManager.getConnection(jdbcBaseUrl, "root", MYSQL_ROOT_PWD)) {
            // create a Statement
            try ( Statement stmt = conn.createStatement()) {
                //execute query
                try ( ResultSet rs = stmt.executeQuery("SELECT SCHEMA_NAME"
                        + "  FROM INFORMATION_SCHEMA.SCHEMATA"
                        + " WHERE SCHEMA_NAME = '" + MYSQL_DB + "'")) {
                    if (rs.next()) {
                        System.out.println("---------------- check database ok -------------------");
                        return;
                    }
                    stmt.executeUpdate("create database projectwork character set UTF8");
                    stmt.executeUpdate("grant all on projectwork.* to 'pwapp'@'%' identified by 'pwapp'");
                    System.out.println("---------------- database created -------------------");
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DbConfigurator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean migrate() {
        Flyway flyway = Flyway
                .configure()
                .dataSource(jdbcBaseUrl + MYSQL_DB, MYSQL_USR, MYSQL_USER_PWD)
                .baselineOnMigrate(true)
                .load();
        int result = flyway.migrate();
        System.out.println("-------------------------result migrate------------- " + result);
        return result > 0;
    }
}
