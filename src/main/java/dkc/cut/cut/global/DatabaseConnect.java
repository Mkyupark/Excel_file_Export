package dkc.cut.cut.global;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


/**
 * @brief
 * DB 연결 상태 확인 코드
 * Spring Boot는 애플리케이션 실행 시 application.properties 또는 application.yml 파일에서
 * DataSource 설정을 읽어와 자동으로 DB 연결함
 * 이 클래스는 DB 연결이 성공적으로 이루어졌는지 확인하기 위한 코드로,
 * 실제 애플리케이션 기능에는 없어도 되는 코드.
 */

@Component
public class DatabaseConnect implements CommandLineRunner{
	
	@Autowired
    private final DataSource dataSource;

    public DatabaseConnect (DataSource dataSource) {
        this.dataSource = dataSource;
    }
    @Override
    public void run(String... args) {
        testConnection();
    }

    private void testConnection() {
        try (Connection connection = dataSource.getConnection()) {
            System.out.println("DB 연결 성공: " + connection.getMetaData().getURL());
        } catch (SQLException ex) {
            System.err.println("DB 연결 실패: " + ex.getMessage());
        }
    }
}
