package com.metrics.analyzer;

import static org.assertj.core.api.Assertions.assertThat;

import com.metrics.model.SourceInput;
import com.metrics.service.MetricsAnalysisService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ClassMetricsAnalyzerTest {

    @Autowired
    private MetricsAnalysisService service;

    @Test
    void analyzeSourcesComputesInheritanceAndCouplingMetrics() {
        List<SourceInput> inputs = List.of(
            new SourceInput("demo/BaseAccount.java", """
                package demo;
                public class BaseAccount {
                    protected int balance;

                    public void deposit(int amount) {
                        balance += amount;
                    }
                }
                """),
            new SourceInput("demo/PremiumAccount.java", """
                package demo;
                public class PremiumAccount extends BaseAccount {
                    private AuditService auditService = new AuditService();

                    public void withdraw(int amount) {
                        if (amount < balance) {
                            balance -= amount;
                            auditService.record(amount);
                        }
                    }
                }
                """),
            new SourceInput("demo/AuditService.java", """
                package demo;
                public class AuditService {
                    public void record(int amount) {
                        System.out.println(amount);
                    }
                }
                """)
        );

        var response = service.analyzeSources(inputs);
        var premium = response.classMetrics().stream()
            .filter(metric -> metric.className().equals("PremiumAccount"))
            .findFirst()
            .orElseThrow();
        var base = response.classMetrics().stream()
            .filter(metric -> metric.className().equals("BaseAccount"))
            .findFirst()
            .orElseThrow();

        assertThat(premium.dit()).isEqualTo(1);
        assertThat(base.noc()).isEqualTo(1);
        assertThat(premium.noa()).isEqualTo(1);
        assertThat(premium.nom()).isEqualTo(1);
        assertThat(premium.wmc()).isEqualTo(2);
        assertThat(premium.cbo()).isGreaterThanOrEqualTo(2);
    }
}
