package com.metrics.service;

import com.metrics.model.request.ManualEstimateRequest;
import com.metrics.model.request.UseCasePointRequest;
import com.metrics.model.response.EstimationSummary;
import com.metrics.model.response.RiskFinding;
import com.metrics.model.response.UseCasePointSummary;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EstimationService {

    public EstimationSummary summarize(ManualEstimateRequest request) {
        double workloadPersonMonths = (double) request.staffCount() * request.devMonths();
        double productivityPerPersonMonth = 0.0;
        if (workloadPersonMonths > 0) {
            productivityPerPersonMonth = (double) request.loc() / workloadPersonMonths;
        }

        double costPerLoc = request.loc() > 0 ? request.cost() / request.loc() : 0.0;

        return new EstimationSummary(
            true,
            "manual",
            request.loc(),
            request.staffCount(),
            request.devMonths(),
            request.cost(),
            workloadPersonMonths,
            productivityPerPersonMonth,
            costPerLoc,
            UseCasePointSummary.empty()
        );
    }

    public EstimationSummary summarizeUseCasePoints(UseCasePointRequest request) {
        int uaw = request.simpleActors() + (2 * request.averageActors()) + (3 * request.complexActors());
        int uucw = (5 * request.simpleUseCases()) + (10 * request.averageUseCases()) + (15 * request.complexUseCases());
        int uucp = uaw + uucw;
        double ucp = round2(uucp * request.technicalComplexityFactor() * request.environmentalComplexityFactor());

        return new EstimationSummary(
            true,
            "use-case-points",
            0,
            0,
            0,
            0.0,
            0.0,
            0.0,
            0.0,
            new UseCasePointSummary(uaw, uucw, uucp, ucp)
        );
    }

    public List<RiskFinding> buildManualRiskFindings(EstimationSummary summary) {
        List<RiskFinding> findings = new ArrayList<>();
        if (!"manual".equals(summary.mode()) || summary.loc() <= 0 || summary.workloadPersonMonths() <= 0) {
            return findings;
        }

        if (summary.productivityPerPersonMonth() > 2000 || summary.productivityPerPersonMonth() < 25) {
            findings.add(new RiskFinding(
                "MEDIUM",
                "ESTIMATION",
                "项目估算",
                "人月生产率结果偏离常见范围，请复核 LoC、人数与工期输入。"
            ));
        }

        return findings;
    }

    private double round2(double value) {
        return BigDecimal.valueOf(value)
            .setScale(2, RoundingMode.HALF_UP)
            .doubleValue();
    }
}
