from typing import Dict, List, Literal
from pydantic import BaseModel, Field

DiagramType = Literal['class', 'flow', 'usecase']
SourceType = Literal['structured', 'image']
IssueLevel = Literal['info', 'warning', 'error']


class StructuredAnalyzeRequest(BaseModel):
    diagramType: DiagramType
    fileName: str = Field(min_length=1)
    sourceSuffix: str = Field(min_length=1)
    source: str = Field(min_length=1)


class DiagramElement(BaseModel):
    type: str
    name: str
    stereotype: str
    confidence: float = 1.0


class DiagramRelation(BaseModel):
    source: str
    target: str
    type: str
    confidence: float = 1.0


class DiagramMetricValue(BaseModel):
    name: str
    value: float
    unit: str
    description: str


class ConfidenceSummary(BaseModel):
    overall: float
    directlyMeasurable: bool


class RecognitionIssue(BaseModel):
    level: IssueLevel
    code: str
    message: str


class DiagramAnalysisResponse(BaseModel):
    diagramType: DiagramType
    sourceType: SourceType
    elements: List[DiagramElement]
    relations: List[DiagramRelation]
    metrics: List[DiagramMetricValue]
    confidence: ConfidenceSummary
    issues: List[RecognitionIssue]


class ParsedDiagram(BaseModel):
    elements: List[DiagramElement]
    relations: List[DiagramRelation]
    metadata: Dict[str, float] = Field(default_factory=dict)


class HealthResponse(BaseModel):
    status: str
    service: str
    modelCachePath: str


class ModelsStatusResponse(BaseModel):
    ready: bool
    modelsLoaded: bool
    modelCachePath: str
    presentAssets: List[str] = Field(default_factory=list)
    missingAssets: List[str] = Field(default_factory=list)
