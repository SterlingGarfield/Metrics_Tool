<template>
  <aside class="panel">
    <header class="result-heading">
      <p class="result-kicker">指标说明</p>
      <h2>指标说明</h2>
    </header>
    <ul>
      <li>LK 课程对齐视图：类规模、方法规模、属性规模、关系总数、关系密度和继承深度分布。</li>
      <li>CK 明细：通过类表查看每个类的 WMC、CBO、RFC、DIT 和 NOC。</li>
      <li>WMC：类内方法圈复杂度之和。</li>
      <li>CBO：引用到的不同协作类型数量。</li>
      <li>RFC：方法数量加上不同被调用方法数量。</li>
      <li>LCOM：基于字段使用重叠得到的内聚缺失估计。</li>
    </ul>
  </aside>
</template>
