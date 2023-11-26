package monolink.monolinkquizback;

import monolink.monolinkquizback.config.PropertiesLogger;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class MonolinkQuizBackApplication {

    public static void main(String[] args) {
        configure(new SpringApplicationBuilder()).build().run(args);
    }

    protected static SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(MonolinkQuizBackApplication.class).listeners(new PropertiesLogger());
    }

}
