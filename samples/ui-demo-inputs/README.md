# UI Demo Inputs

These sample files support the recommended course-defense flow in `docs/course-defense-demo.zh-CN.md`.

## Recommended Order

1. Code Input
2. Single File
3. Multiple Files
4. Folder Scan

## Code Input

- Use: `samples/ui-demo-inputs/code-input/CodeInputSample.java`
- Operation: open the file and paste its content into the `Code Input` textarea
- Expected result: direct code analysis with method/class/project metrics
- Talking point: show that the tool accepts user-pasted source code and can analyze it without requiring a project import first

## Single File

- Use: `samples/ui-demo-inputs/single-file/SingleFileDemo.java`
- Operation: upload the file in `Single File` mode
- Expected result: single-file OO metrics with traditional indicators such as complexity and LoC
- Talking point: show that the tool supports lightweight source analysis for a single Java file

## Multiple Files

- Use: upload all `.java` files under `samples/ui-demo-inputs/multiple-files/`
- Expected result: clearer project-level metrics, class relations, and `LK Course-Aligned View`
- Talking point: this is the recommended live-demo path for code metrics because it best shows OO metrics, LK alignment, and project aggregation together

## Folder Scan

- Use: choose the folder `samples/ui-demo-inputs/folder-scan/` in the `Folder Scan` mode
- Expected result: project-wide scan through a source folder with class/method/project summaries
- Talking point: show that the tool can analyze a codebase folder instead of only manual text or one-off uploads

## Related References

- Defense walkthrough: `docs/course-defense-demo.zh-CN.md`
- Evidence index: `docs/course-evidence-index.zh-CN.md`
- Course requirement matrix: `docs/course-requirement-matrix.zh-CN.md`

