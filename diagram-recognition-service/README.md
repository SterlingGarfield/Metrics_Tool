# Diagram Recognition Service

## Recognition bootstrap

Use the Windows bootstrap script from the repository root to create or repair the local Python 3.11 runtime, reinstall the pinned OCR packages, warm the cached models, and verify the runtime environment.

```powershell
# run from D:\Projects\SQA\Metrics_Tool\.worktrees\diagram-recognition-enhancement
powershell -ExecutionPolicy Bypass -File .\scripts\bootstrap-models.ps1
```

To warm models manually after bootstrap (run from `diagram-recognition-service`):

```powershell
.\.venv311\python.exe -m app.tools.warm_models
```

To run the service tests directly (run from `diagram-recognition-service`), use a writable basetemp if Windows blocks the default temp location:

```powershell
.\.venv311\python.exe -m pytest tests -q --basetemp "C:\Users\<user>\AppData\Local\Temp\codex-pytest"
```

If you hit a Windows temp permission error, keep the `--basetemp` override and point it at any writable local folder. The service also keeps its own runtime scratch directories out of git status noise.
