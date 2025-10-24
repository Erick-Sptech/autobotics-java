package school.sptech;

import org.apache.commons.dbcp2.BasicDataSource;

public class Conecction {
    private BasicDataSource dataSource;

    public Conecction(){
        dataSource = new BasicDataSource();

        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/autobotics");
        dataSource.setUsername("Aluno");
        dataSource.setPassword("sptech");

    }

    public BasicDataSource getDataSource(){
        return dataSource;
    }

}
