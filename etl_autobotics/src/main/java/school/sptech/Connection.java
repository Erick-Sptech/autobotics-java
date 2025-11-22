package school.sptech;

import org.apache.commons.dbcp2.BasicDataSource;

public class Connection {
    private BasicDataSource dataSource;

    public Connection(){
        dataSource = new BasicDataSource();

        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://44.198.97.37:3306/autobotics");
        dataSource.setUsername("agente");
        dataSource.setPassword("sptech");

    }

    public BasicDataSource getDataSource(){
        return dataSource;
    }

}
