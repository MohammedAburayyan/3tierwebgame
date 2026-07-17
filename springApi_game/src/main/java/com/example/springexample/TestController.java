package com.example.springexample;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Demonstrates the Postgres data tier. Active only under the {@code db} profile —
 * without it the JPA autoconfiguration (and therefore {@link TestRepository}) is
 * switched off, so this controller must not load or the context would fail wiring
 * a repository bean that doesn't exist. Run with {@code -Dspring-boot.run.profiles=db}.
 */
@RestController
@RequestMapping("/api")
@Profile("db")
public class TestController {

    private final TestRepository testRepository;

    public TestController(TestRepository testRepository) {
        this.testRepository = testRepository;
    }

    @GetMapping("/tests")
    public List<Test> getAllTests() {
        return testRepository.findAll();
    }
}
