package com.metrics.service;

import com.metrics.model.request.EstimateProjectRequest;
import com.metrics.model.DiagramType;
import com.metrics.model.response.EstimationBasis;
import com.metrics.model.response.ProjectEstimation;
import com.metrics.model.response.UcpBreakdown;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class EstimationService {

    private static final double DEFAULT_COST_RATE = 15000.0;
    private static final double STANDARD_UCP_PER_PERSON_MONTH = 8.0;
    private static final double SIMPLIFIED_UCP_PER_PERSON_MONTH = 10.0;

    public ProjectEstimation estimate(EstimateProjectRequest request) {
        UcpBreakdown ucpBreakdown = buildUcpBreakdown(request);
        double heuristicWorkload = computeHeuristicWorkload(request);
        double workloadPersonMonths = round2(computeFinalWorkload(heuristicWorkload, ucpBreakdown));
        double costRate = request.costRatePerPersonMonth() == null ? DEFAULT_COST_RATE : request.costRatePerPersonMonth();
        double scheduleMonths = request.targetScheduleMonths() == null
            ? inferSchedule(workloadPersonMonths)
            : request.targetScheduleMonths();
        int suggestedStaffing = Math.max(1, (int) Math.ceil(workloadPersonMonths / Math.max(1.0, scheduleMonths)));
        double cost = round2(workloadPersonMonths * costRate);

        double diagramMultiplier = diagramMultiplier(request.diagramType());
        String ucpPath = ucpBreakdown.standardInputUsed() ? "standard" : "simplified";
        String basisDetails = String.format(
            Locale.ROOT,
            "Inputs: totalLoc=%d, classCount=%d, relationshipCount=%d, useCaseCount=%d, decisionNodeCount=%d, diagramType=%s, diagramMultiplier=%.2f, costRate=%.2f, targetSchedule=%s. UCP path=%s, UAW=%.2f, UUCW=%.2f, UUCP=%.2f, TCF=%.2f, EF=%.2f, UCP=%.2f, UCPWorkload=%.2f.",
            safeInt(request.totalLoc()),
            safeInt(request.classCount()),
            safeInt(request.relationshipCount()),
            safeInt(request.useCaseCount()),
            safeInt(request.decisionNodeCount()),
            request.diagramType().value(),
            diagramMultiplier,
            costRate,
            request.targetScheduleMonths() == null ? "auto" : String.format(Locale.ROOT, "%.2f", request.targetScheduleMonths()),
            ucpPath,
            ucpBreakdown.uaw(),
            ucpBreakdown.uucw(),
            ucpBreakdown.uucp(),
            ucpBreakdown.technicalComplexityFactor(),
            ucpBreakdown.environmentalFactor(),
            ucpBreakdown.ucp(),
            ucpBreakdown.workloadPersonMonths()
        );
        EstimationBasis basis = new EstimationBasis(
            ucpBreakdown.standardInputUsed()
                ? "Heuristic blend of code size, OO coupling, and diagram complexity with standard UCP calibration."
                : "Heuristic blend of code size, OO coupling, and diagram complexity with simplified UCP fallback.",
            basisDetails
        );
        return new ProjectEstimation(workloadPersonMonths, cost, scheduleMonths, suggestedStaffing, basis, ucpBreakdown);
    }

    private double computeFinalWorkload(double heuristicWorkload, UcpBreakdown ucpBreakdown) {
        if (ucpBreakdown.standardInputUsed()) {
            return Math.max(0.5, ucpBreakdown.workloadPersonMonths());
        }
        double blended = heuristicWorkload * 0.65 + ucpBreakdown.workloadPersonMonths() * 0.35;
        return Math.max(0.5, blended);
    }

    private UcpBreakdown buildUcpBreakdown(EstimateProjectRequest request) {
        if (hasStandardUcpInput(request)) {
            return buildStandardUcpBreakdown(request);
        }
        return buildSimplifiedUcpBreakdown(request);
    }

    private boolean hasStandardUcpInput(EstimateProjectRequest request) {
        return request.simpleActorCount() != null
            || request.averageActorCount() != null
            || request.complexActorCount() != null
            || request.simpleUseCaseCount() != null
            || request.averageUseCaseCount() != null
            || request.complexUseCaseCount() != null;
    }

    private UcpBreakdown buildStandardUcpBreakdown(EstimateProjectRequest request) {
        int simpleActor = safeInt(request.simpleActorCount());
        int averageActor = safeInt(request.averageActorCount());
        int complexActor = safeInt(request.complexActorCount());
        int simpleUseCase = safeInt(request.simpleUseCaseCount());
        int averageUseCase = safeInt(request.averageUseCaseCount());
        int complexUseCase = safeInt(request.complexUseCaseCount());

        double uaw = simpleActor + averageActor * 2.0 + complexActor * 3.0;
        double uucw = simpleUseCase * 5.0 + averageUseCase * 10.0 + complexUseCase * 15.0;
        double uucp = uaw + uucw;
        double tcf = request.technicalComplexityFactor() == null ? 1.0 : request.technicalComplexityFactor();
        double ef = request.environmentalFactor() == null ? 1.0 : request.environmentalFactor();
        double ucp = round2(uucp * tcf * ef);
        double workload = round2(ucp / STANDARD_UCP_PER_PERSON_MONTH);

        return new UcpBreakdown(
            true,
            simpleActor,
            averageActor,
            complexActor,
            simpleUseCase,
            averageUseCase,
            complexUseCase,
            round2(uaw),
            round2(uucw),
            round2(uucp),
            round2(tcf),
            round2(ef),
            ucp,
            workload
        );
    }

    private UcpBreakdown buildSimplifiedUcpBreakdown(EstimateProjectRequest request) {
        int classCount = safeInt(request.classCount());
        int relationshipCount = safeInt(request.relationshipCount());
        int decisionNodeCount = safeInt(request.decisionNodeCount());
        int useCaseCount = safeInt(request.useCaseCount());

        int simpleActor = Math.max(1, classCount == 0 ? 1 : (int) Math.ceil(classCount / 3.0));
        int averageActor = Math.max(0, (int) Math.round(relationshipCount / 8.0));
        int complexActor = decisionNodeCount >= 15 ? 1 : 0;

        int simpleUseCase = 0;
        int averageUseCase = useCaseCount > 0 ? useCaseCount : Math.max(1, (int) Math.round(decisionNodeCount / 3.0));
        int complexUseCase = 0;

        double uaw = simpleActor + averageActor * 2.0 + complexActor * 3.0;
        double uucw = simpleUseCase * 5.0 + averageUseCase * 10.0 + complexUseCase * 15.0;
        double uucp = uaw + uucw;
        double tcf = 0.9
            + Math.min(
                0.4,
                relationshipCount * 0.004
                    + decisionNodeCount * 0.003
                    + Math.max(0.0, diagramMultiplier(request.diagramType()) - 1.0) * 0.5
            );
        double ef = 1.0;
        double ucp = round2(uucp * tcf * ef);
        double workload = round2(ucp / SIMPLIFIED_UCP_PER_PERSON_MONTH);

        return new UcpBreakdown(
            false,
            simpleActor,
            averageActor,
            complexActor,
            simpleUseCase,
            averageUseCase,
            complexUseCase,
            round2(uaw),
            round2(uucw),
            round2(uucp),
            round2(tcf),
            round2(ef),
            ucp,
            workload
        );
    }

    private double computeHeuristicWorkload(EstimateProjectRequest request) {
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
