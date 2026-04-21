<template>
  <section class="panel results-section table-stack">
    <div class="results-section-heading">
      <p class="results-section-kicker">明细</p>
      <h2>类级指标</h2>
      <p class="results-section-copy">
        先查看类级结构性指标，再下钻到方法级复杂度和嵌套深度细节。
      </p>
    </div>

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

    <h3 class="results-subheading">方法级指标</h3>
    <table>
      <thead>
        <tr>
          <th>方法</th>
          <th>复杂度</th>
          <th>LOC</th>
          <th>嵌套深度</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in methodMetrics" :key="item.className + item.methodName">
          <td>{{ item.className }}#{{ item.methodName }}</td>
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
