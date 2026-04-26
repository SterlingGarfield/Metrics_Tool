package com.metrics.desktop;

import com.metrics.model.response.AnalysisResponse;
import java.util.Map;

public record DesktopCliResponse(
    String status,
    AnalysisResponse analysis,
    Object payload,
    Map<String, Object> appStatus,
    String error
) {
    public static DesktopCliResponse success(AnalysisResponse analysis) {
        return new DesktopCliResponse("ok", analysis, null, null, null);
    }

    public static DesktopCliResponse payload(Object payload) {
        return new DesktopCliResponse("ok", null, payload, null, null);
    }

    public static DesktopCliResponse status(Map<String, Object> appStatus) {
        return new DesktopCliResponse("ok", null, null, appStatus, null);
    }

    public static DesktopCliResponse error(String error) {
        return new DesktopCliResponse("error", null, null, null, error);
    }
}
