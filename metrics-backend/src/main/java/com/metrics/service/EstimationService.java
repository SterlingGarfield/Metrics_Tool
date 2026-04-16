package com.metrics.service;

import com.metrics.model.request.EstimateProjectRequest;
import com.metrics.model.response.EstimationBasis;
import com.metrics.model.response.ProjectEstimation;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class EstimationService {

    public ProjectEstimation estimate(EstimateProjectRequest request) {
        double workloadPersonMonths = round2(computeWorkload(request));
        double costRate = request.costRatePerPersonMonth() == null ? 15000.0 : request.costRatePerPersonMonth();
        double scheduleMonths = request.targetScheduleMonths() == null
            ? Math.max(1.0, round2(workloadPersonMonths / 2.0))
            : request.targetScheduleMonths();
        int suggestedStaffing = Math.max(1, (int) Math.ceil(workloadPersonMonths / Math.max(1.0, scheduleMonths)));
        double cost = round2(workloadPersonMonths * costRate);

        String basisDetails = String.format(
            Locale.ROOT,
            "Stub inputs: totalLoc=%d, classCount=%d, relationshipCount=%d, useCaseCount=%d, decisionNodeCount=%d, costRate=%.2f, targetSchedule=%s.",
            safeInt(request.totalLoc()),
            safeInt(request.classCount()),
            safeInt(request.relationshipCount()),
            safeInt(request.useCaseCount()),
            safeInt(request.decisionNodeCount()),
            costRate,
            request.targetScheduleMonths() == null ? "auto" : String.format(Locale.ROOT, "%.2f", request.targetScheduleMonths())
        );
        EstimationBasis basis = new EstimationBasis(
            "Backend-local estimation boundary for Task 1 contract wiring.",
            basisDetails
        );
        return new ProjectEstimation(workloadPersonMonths, cost, scheduleMonths, suggestedStaffing, basis);
    }

    private double computeWorkload(EstimateProjectRequest request) {
        double locFactor = safeInt(request.totalLoc()) / 1200.0;
        double classFactor = safeInt(request.classCount()) * 0.08;
        double diagramFactor = safeInt(request.relationshipCount()) * 0.03
            + safeInt(request.useCaseCount()) * 0.12
            + safeInt(request.decisionNodeCount()) * 0.06;
        return Math.max(0.5, locFactor + classFactor + diagramFactor);
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
