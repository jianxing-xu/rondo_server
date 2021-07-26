package cn.xu.rondo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("cn.xu.rondo.mapper")
@SpringBootApplication
public class RondoApplication {
    public static void main(String[] args) {
        SpringApplication.run(RondoApplication.class, args);
    }
}
