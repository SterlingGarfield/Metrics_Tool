from pathlib import Path

from fastapi import APIRouter, File, Form, HTTPException, UploadFile

from app.config import get_settings
from app.models.contracts import (
    DiagramAnalysisResponse,
    HealthResponse,
    ModelsStatusResponse,
    StructuredAnalyzeRequest,
)
from app.services.structured.metric_extractor import build_structured_analysis

router = APIRouter(prefix='/recognition', tags=['recognition'])
_REQUIRED_MODEL_ASSETS = ['paddleocr']


@router.get('/health', response_model=HealthResponse)
def health() -> HealthResponse:
    settings = get_settings()
    return HealthResponse(
        status='UP',
        service=settings.service_name,
        modelCachePath=settings.model_cache_path,
    )


@router.get('/models/status', response_model=ModelsStatusResponse)
def models_status() -> ModelsStatusResponse:
    settings = get_settings()
    model_cache_path = Path(settings.model_cache_path)
    present_assets = [asset for asset in _REQUIRED_MODEL_ASSETS if (model_cache_path / asset).exists()]
    missing_assets = [asset for asset in _REQUIRED_MODEL_ASSETS if asset not in present_assets]
    ready = not missing_assets
    return ModelsStatusResponse(
        ready=ready,
        modelsLoaded=ready,
        modelCachePath=str(model_cache_path),
        presentAssets=present_assets,
        missingAssets=missing_assets,
    )


@router.post('/analyze/structured', response_model=DiagramAnalysisResponse)
def analyze_structured(request: StructuredAnalyzeRequest) -> DiagramAnalysisResponse:
    return build_structured_analysis(
        diagram_type=request.diagramType,
        file_name=request.fileName,
        source_suffix=request.sourceSuffix,
        source=request.source,
    )


@router.post('/analyze/image')
def analyze_image(
    diagramType: str = Form(...),
    file: UploadFile = File(...),
):
    _ = (diagramType, file)
    raise HTTPException(status_code=501, detail='Image recognition pipeline is not implemented yet')
