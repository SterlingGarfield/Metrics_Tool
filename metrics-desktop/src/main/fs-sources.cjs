const fs = require('fs/promises')
const path = require('path')

function normalizeRelativePath(basePath, filePath) {
  return path.relative(basePath, filePath).split(path.sep).join('/')
}

async function collectJavaFiles(rootPath) {
  const entries = await fs.readdir(rootPath, { withFileTypes: true })
  const nestedFiles = await Promise.all(entries.map(async (entry) => {
    const entryPath = path.join(rootPath, entry.name)
    if (entry.isDirectory()) {
      return collectJavaFiles(entryPath)
    }

    if (entry.isFile() && entry.name.toLowerCase().endsWith('.java')) {
      return [entryPath]
    }

    return []
  }))

  return nestedFiles.flat()
}

async function collectJavaFileDescriptors(rootPath) {
  const files = await collectJavaFiles(rootPath)
  return files.map((filePath) => ({
    path: filePath,
    name: path.basename(filePath),
    relativePath: normalizeRelativePath(rootPath, filePath)
  }))
}

function normalizeSelectedPaths(selections) {
  return (selections || [])
    .map((selection) => (typeof selection === 'string' ? selection : selection?.path))
    .filter(Boolean)
}

async function readSourcesFromPaths(selections) {
  const filePaths = normalizeSelectedPaths(selections)

  return Promise.all(filePaths.map(async (filePath) => ({
    fileName: path.basename(filePath),
    sourceCode: await fs.readFile(filePath, 'utf8')
  })))
}

async function readSourcesFromFolder(selection) {
  const folderPath = typeof selection === 'string' ? selection : selection?.folderPath
  const fileDescriptors = selection?.files?.length
    ? selection.files
    : await collectJavaFileDescriptors(folderPath)

  return Promise.all(fileDescriptors.map(async (file) => ({
    fileName: file.relativePath || normalizeRelativePath(folderPath, file.path),
    sourceCode: await fs.readFile(file.path, 'utf8')
  })))
}

module.exports = {
  collectJavaFileDescriptors,
  readSourcesFromFolder,
  readSourcesFromPaths
}
