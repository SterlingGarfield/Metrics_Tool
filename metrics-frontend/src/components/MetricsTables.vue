<template>
  <section class="panel table-stack">
    <header class="result-heading">
      <p class="result-kicker">指标表</p>
      <h2>类与方法指标明细</h2>
    </header>
    <h3 class="table-subtitle">类指标明细</h3>
    <table>
      <thead>
        <tr>
          <th>类名</th>
          <th>WMC</th>
          <th>CBO</th>
          <th>RFC</th>
          <th>DIT</th>
          <th>NOC</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in classMetrics" :key="item.fileName + item.className">
          <td>{{ item.className }}</td>
          <td>{{ item.wmc }}</td>
          <td>{{ item.cbo }}</td>
          <td>{{ item.rfc }}</td>
          <td>{{ item.dit }}</td>
          <td>{{ item.noc }}</td>
        </tr>
      </tbody>
    </table>

    <h3 class="table-subtitle">方法指标明细</h3>
    <table>
      <thead>
        <tr>
          <th>方法名</th>
          <th>圈复杂度</th>
          <th>代码行数</th>
          <th>最大嵌套深度</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in methodMetrics" :key="item.className + item.methodName">
          <td>{{ item.className }} · {{ item.methodName }}</td>
          <td>{{ item.cyclomaticComplexity }}</td>
          <td>{{ item.loc }}</td>
          <td>{{ item.maxNestingDepth }}</td>
        </tr>
      </tbody>
    </table>
  </section>
</template>

<script setup>
defineProps({
  classMetrics: {
    type: Array,
    required: true
  },
  methodMetrics: {
    type: Array,
    required: true
  }
})
</script>
