<template>
  <section class="panel table-stack">
    <h2>Class Metrics</h2>
    <table>
      <thead>
        <tr>
          <th>Class</th>
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

    <h2>Method Metrics</h2>
    <table>
      <thead>
        <tr>
          <th>Method</th>
          <th>Complexity</th>
          <th>LOC</th>
          <th>Depth</th>
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
