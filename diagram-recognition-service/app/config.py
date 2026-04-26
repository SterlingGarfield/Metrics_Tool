from functools import lru_cache
import os
from pydantic import BaseModel, Field


class Settings(BaseModel):
    service_name: str = 'diagram-recognition'
    model_cache_path: str = Field(
        default_factory=lambda: os.getenv('METRICS_TOOL_MODEL_CACHE', 'D:/Projects/SQA/Metrics_Tool/models')
    )


@lru_cache(maxsize=1)
def get_settings() -> Settings:
    return Settings()
