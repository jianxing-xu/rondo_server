package cn.xu.roundo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("mapper")
@SpringBootApplication
public class RoundoApplication {
    public static void main(String[] args) {
        SpringApplication.run(RoundoApplication.class, args);
    }
}
