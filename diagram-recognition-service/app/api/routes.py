from pathlib import Path

from fastapi import APIRouter, File, Form, HTTPException, UploadFile

from app.config import get_settings
from app.models.contracts import (
    DiagramAnalysisResponse,
    HealthResponse,
    ModelsStatusResponse,
    StructuredAnalyzeRequest,
)
from app.services.image.recover import analyze_image_diagram
from app.services.models.download_manager import describe_model_cache
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


@router.get('/models/status', response_model=ModelsStatusResponse)
def models_status() -> ModelsStatusResponse:
    status = describe_model_cache()
    return ModelsStatusResponse(
        ready=status.ready,
        modelsLoaded=status.ready,
        modelCachePath=str(status.model_cache_path),
        presentAssets=status.present_assets,
        missingAssets=status.missing_assets,
    )


@router.post('/analyze/structured', response_model=DiagramAnalysisResponse)
def analyze_structured(request: StructuredAnalyzeRequest) -> DiagramAnalysisResponse:
    return build_structured_analysis(
        diagram_type=request.diagramType,
        file_name=request.fileName,
        source_suffix=request.sourceSuffix,
        source=request.source,
    )


@router.post('/analyze/image', response_model=DiagramAnalysisResponse)
def analyze_image(
    diagramType: str = Form(...),
    file: UploadFile = File(...),
) -> DiagramAnalysisResponse:
    if diagramType not in {'class', 'flow', 'usecase'}:
        raise HTTPException(status_code=400, detail=f'Unsupported diagram type: {diagramType}')

    suffix = Path(file.filename or '').suffix.lower()
    if suffix not in {'.png', '.jpg', '.jpeg'}:
        raise HTTPException(status_code=400, detail='Unsupported image type. Use PNG or JPG files.')

    payload = file.file.read()
    if not payload:
        raise HTTPException(status_code=400, detail='Uploaded image is empty.')

    try:
        return analyze_image_diagram(diagramType, file.filename or 'uploaded-image', payload)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
