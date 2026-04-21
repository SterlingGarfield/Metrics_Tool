<template>
  <aside class="panel results-section metric-info">
    <div class="results-section-heading">
      <p class="results-section-kicker">参考</p>
      <h2>指标说明</h2>
      <p class="results-section-copy">
        下面这些说明用于帮助快速理解常见面向对象度量指标的含义。
      </p>
    </div>

    <ul class="metric-guide-list">
      <li><strong>WMC：</strong>类中所有方法圈复杂度之和，用于反映类的整体复杂程度。</li>
      <li><strong>CBO：</strong>类对外部类型的耦合数量，越高通常表示依赖越分散。</li>
      <li><strong>RFC：</strong>类可响应的方法规模，综合自身方法和调用到的不同方法。</li>
      <li><strong>LCOM：</strong>类内成员之间的内聚度估计，可辅助识别职责过散的设计。</li>
    </ul>
  </aside>
</template>
