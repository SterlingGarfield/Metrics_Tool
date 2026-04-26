<template>
  <aside class="panel results-section metric-info">
    <div class="results-section-heading">
      <p class="results-section-kicker">Guide</p>
      <h2>指标说明</h2>
      <p class="results-section-copy">
        用简洁说明把常见面向对象指标翻译成可读语言。
      </p>
    </div>

    <ul class="metric-guide-list">
      <li><strong>WMC：</strong>类中所有方法圈复杂度之和，便于观察整体复杂程度。</li>
      <li><strong>CBO：</strong>类对外部类型的耦合数量，数值越高通常意味着依赖越分散。</li>
      <li><strong>RFC：</strong>类可响应的方法规模，结合自身方法和可能触发的调用范围。</li>
      <li><strong>LCOM：</strong>类内成员之间的内聚度估计，可辅助识别职责过散的设计。</li>
      <li><strong>LK 新增方法数：</strong>子类相对父类新引入的方法数量，用来观察扩展规模。</li>
      <li><strong>LK 覆写方法数：</strong>子类直接覆写父类方法的数量，用来观察继承定制程度。</li>
      <li><strong>特化指数：</strong>综合覆写方法数、继承深度和方法总数，用来估计子类的特化强度。</li>
    </ul>
  </aside>
</template>
