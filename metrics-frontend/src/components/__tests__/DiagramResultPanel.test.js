import { readFileSync } from 'node:fs'
import { join } from 'node:path'
import { fireEvent, render, screen, within } from '@testing-library/vue'
import DiagramResultPanel from '../DiagramResultPanel.vue'

const themeStyles = readFileSync(join(process.cwd(), 'src/styles/theme.css'), 'utf8')

describe('DiagramResultPanel', () => {
  test('renders a chinese result summary with raw payload preserved', async () => {
    const diagramResult = {
      diagramType: 'class',
      sourceType: 'image',
      confidence: {
        overall: 0.83,
        directlyMeasurable: true
      },
      elements: [
        { type: 'class', name: 'OrderService', stereotype: 'service', confidence: 0.94 },
        { type: 'class', name: 'OrderRepository', stereotype: 'repository', confidence: 0.88 }
      ],
      relations: [
        { source: 'OrderService', target: 'OrderRepository', type: 'dependency', confidence: 0.9 }
      ],
      metrics: [
        { name: '节点总数', value: 2, unit: '个', description: '识别到的节点总数' },
        { name: '关系总数', value: 1, unit: '条', description: '识别到的关系总数' }
      ],
      issues: [
        { level: 'warning', code: 'LOW_CONFIDENCE', message: '部分边界存在歧义' }
      ]
    }

    render(DiagramResultPanel, {
      props: {
        diagramResult
      }
    })

    expect(screen.getByRole('heading', { name: '设计图识别结果' })).toBeInTheDocument()
    expect(screen.getByText('类图')).toBeInTheDocument()
    expect(screen.getByText('图片输入')).toBeInTheDocument()
    expect(screen.getByText('83%')).toBeInTheDocument()
    expect(screen.getByText('2')).toBeInTheDocument()
    expect(screen.getByText('1')).toBeInTheDocument()
    expect(screen.getByText('OrderService')).toBeInTheDocument()
    expect(screen.getByText('OrderService -> OrderRepository')).toBeInTheDocument()
    expect(screen.getByText('部分边界存在歧义')).toBeInTheDocument()

    await fireEvent.click(screen.getByText('原始结果'))
    const rawResult = screen.getByRole('region', { name: '原始结果' })
    expect(within(rawResult).getByText(/"diagramType": "class"/)).toBeInTheDocument()
  })

  test('keeps the expanded raw payload inside a scrollable viewport', async () => {
    const diagramResult = {
      diagramType: 'flow',
      sourceType: 'structured',
      confidence: {
        overall: 0.91,
        directlyMeasurable: true
      },
      elements: [],
      relations: [],
      metrics: [],
      issues: []
    }

    render(DiagramResultPanel, {
      props: {
        diagramResult
      }
    })

    await fireEvent.click(screen.getByText('原始结果'))
    const rawResult = screen.getByRole('region', { name: '原始结果' })
    const rawPre = within(rawResult).getByText(/"diagramType": "flow"/).closest('pre')

    expect(themeStyles).toMatch(/\.result-raw__body\s*\{[^}]*max-height:\s*320px;/s)
    expect(themeStyles).toMatch(/\.result-raw__body\s*\{[^}]*overflow-x:\s*auto;/s)
    expect(themeStyles).toMatch(/\.result-raw__body\s*\{[^}]*overflow-y:\s*auto;/s)
    expect(rawPre).not.toBeNull()
    expect(themeStyles).toMatch(/\.result-raw pre\s*\{[^}]*min-width:\s*100%;/s)
  })
})
