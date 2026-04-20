package com.metrics.service;

import com.metrics.model.request.EstimateProjectRequest;
import com.metrics.model.DiagramType;
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
            ? inferSchedule(workloadPersonMonths)
            : request.targetScheduleMonths();
        int suggestedStaffing = Math.max(1, (int) Math.ceil(workloadPersonMonths / Math.max(1.0, scheduleMonths)));
        double cost = round2(workloadPersonMonths * costRate);

        double diagramMultiplier = diagramMultiplier(request.diagramType());
        String basisDetails = String.format(
            Locale.ROOT,
            "Inputs: totalLoc=%d, classCount=%d, relationshipCount=%d, useCaseCount=%d, decisionNodeCount=%d, diagramType=%s, diagramMultiplier=%.2f, costRate=%.2f, targetSchedule=%s.",
            safeInt(request.totalLoc()),
            safeInt(request.classCount()),
            safeInt(request.relationshipCount()),
            safeInt(request.useCaseCount()),
            safeInt(request.decisionNodeCount()),
            request.diagramType().value(),
            diagramMultiplier,
            costRate,
            request.targetScheduleMonths() == null ? "auto" : String.format(Locale.ROOT, "%.2f", request.targetScheduleMonths())
        );
        EstimationBasis basis = new EstimationBasis(
            "Heuristic blend of code size, OO coupling, and diagram complexity.",
            basisDetails
        );
        return new ProjectEstimation(workloadPersonMonths, cost, scheduleMonths, suggestedStaffing, basis);
    }

    private double computeWorkload(EstimateProjectRequest request) {
        double locFactor = safeInt(request.totalLoc()) / 1100.0;
        double classFactor = safeInt(request.classCount()) * 0.07;
        double couplingFactor = safeInt(request.relationshipCount()) * 0.035;
        double diagramFactor = safeInt(request.relationshipCount()) * 0.015
            + safeInt(request.useCaseCount()) * 0.12
            + safeInt(request.decisionNodeCount()) * 0.06;
        double workload = (locFactor + classFactor + couplingFactor + diagramFactor) * diagramMultiplier(request.diagramType());
        return Math.max(0.5, workload);
    }

    private double diagramMultiplier(DiagramType diagramType) {
        return switch (diagramType) {
            case CLASS -> 1.0;
            case FLOW -> 1.08;
            case USECASE -> 1.12;
        };
    }

    private double inferSchedule(double workloadPersonMonths) {
        double inferred = 0.85 * Math.pow(Math.max(workloadPersonMonths, 0.5), 0.38) + 0.8;
        return round2(Math.max(1.0, inferred));
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
