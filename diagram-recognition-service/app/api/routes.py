from fastapi import APIRouter
from app.config import get_settings
from app.models.contracts import DiagramAnalysisResponse, HealthResponse, StructuredAnalyzeRequest
from app.services.structured.metric_extractor import build_structured_analysis

router = APIRouter(prefix='/recognition', tags=['recognition'])


@router.get('/health', response_model=HealthResponse)
def health() -> HealthResponse:
    settings = get_settings()
    return HealthResponse(
        status='UP',
        service=settings.service_name,
        modelCachePath=settings.model_cache_path,
    )


@router.post('/analyze/structured', response_model=DiagramAnalysisResponse)
def analyze_structured(request: StructuredAnalyzeRequest) -> DiagramAnalysisResponse:
    return build_structured_analysis(
        diagram_type=request.diagramType,
        file_name=request.fileName,
        source_suffix=request.sourceSuffix,
        source=request.source,
    )
