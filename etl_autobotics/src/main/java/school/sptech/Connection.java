package school.sptech;

import org.apache.commons.dbcp2.BasicDataSource;

public class Connection {
    private BasicDataSource dataSource;

    public Connection(){
        dataSource = new BasicDataSource();

        String bd = System.getenv("BD_IP");

        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://" + bd + ":3306/autobotics");
        dataSource.setUsername("agente");
        dataSource.setPassword("sptech");

    }

    public BasicDataSource getDataSource(){
        return dataSource;
    }

}
