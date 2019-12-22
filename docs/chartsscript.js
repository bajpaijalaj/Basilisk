google.charts.load('current', { 'packages': ['bar', 'corechart', 'line'] });
google.charts.setOnLoadCallback(drawsheet);
$(document).ready(function () {
  $("#close").click(function () {
    $("#menuId").slideUp("medium");
    $("#close").hide();
    $("#open").show();
  });
  $("#open").click(function () {
    $("#menuId").slideDown("slow");
    $("#open").hide();
    $("#close").show();
  });
});
function move(temp) {
  if (temp == 'bar') {
    $("#boxplot_chart").slideUp("medium");
    $("#line_chart").slideUp("medium");
    $("#other_stats").slideUp("medium");
    $("#bar_chart").slideDown("medium");
  }
  else if (temp == 'box') {
    $("#bar_chart").slideUp("medium");
    $("#line_chart").slideUp("medium");
    $("#other_stats").slideUp("medium");
    $("#boxplot_chart").slideDown("medium");
  }
  else if (temp == 'line') {
    $("#bar_chart").slideUp("medium");
    $("#boxplot_chart").slideUp("medium");
    $("#other_stats").slideUp("medium");
    $("#line_chart").slideDown("medium");
  }
  else if (temp == 'other') {
    $("#bar_chart").slideUp("medium");
    $("#boxplot_chart").slideUp("medium");
    $("#line_chart").slideUp("medium");
    $("#other_stats").slideDown("medium");
  }
}
function drawChart(response) {
  if (response.isError()) {
    alert('Error in query: ' + response.getMessage() + ' ' + response.getDetailedMessage());
    return;
  }

  var data = response.getDataTable();
  var bar_chart_options = {
      chart: {
        title: 'Performance',
        subtitle: 'Tentris, Fuseki, and Virtuoso'          
      },
      bars: 'vertical',
      legend: { position: 'none' },
      is3D: true, 
      hAxis : {textPosition: '#ffffff'},
      height: 450,
      width: 500,
      colors: ['#1b9e76', '#d95f01', '#7570b2']
  };
  var line_options = {
  chart: {
      title: 'QpS'
  },
  legend: { position: 'none' },
  width: 900,
  height: 500
  };
  var line_data = new google.visualization.DataTable();
  line_data.addColumn('number', 'Day');
  line_data.addColumn('number', 'T');
  line_data.addColumn('number', 'F');
  line_data.addColumn('number', 'V');
  line_data.addRows([[1,  57.8, 60.8, 40.8],[2,  40.1, 61.1, 12.4],[3,  15.2, 60, 14.7],
    [4,  21.8, 13.9, 13.1],[5,  10.0, 27.7, 11.2],[6,   9.9, 12.4,  7.1],
    [7,   4.5, 42.2,  19.5],[8,  12.4, 42.1, 11.8],[9,  26.3, 52.2, 21.8],
    [10, 17.2, 34.2, 31.7],[11,  7.2,  4.2,  8.6],[12,  7.1,  22.8,  6.4],
    [13,  14.6,  9.2,  7.1],[14,  6.2,  15.7,  16.2]]);
  var line_options = {
    chart: {
      title: 'QpS'
    },
    legend: { position: 'none' },
    width: 900,
    height: 500
  };
  var data_box = [
    ['Tentris', 100, 90, 110, 85, 96, 104, 120],
    ['Fuseki', 120, 95, 130, 90, 113, 124, 140],
    ['Virtuoso', 130, 105, 140, 100, 117, 133, 139]
  ];
  var dataTable = new google.visualization.DataTable();
  dataTable.addColumn('string', 'x');
  dataTable.addColumn('number', 'series0');
  dataTable.addColumn('number', 'series1');
  dataTable.addColumn('number', 'series2');
  dataTable.addColumn('number', 'series3');
  dataTable.addColumn('number', 'series4');
  dataTable.addColumn('number', 'series5');
  dataTable.addColumn('number', 'series6');
  dataTable.addColumn({ id: 'max', type: 'number', role: 'interval' });
  dataTable.addColumn({ id: 'min', type: 'number', role: 'interval' });
  dataTable.addColumn({ id: 'firstQuartile', type: 'number', role: 'interval' });
  dataTable.addColumn({ id: 'median', type: 'number', role: 'interval' });
  dataTable.addColumn({ id: 'thirdQuartile', type: 'number', role: 'interval' });
  dataTable.addRows(getBoxPlotValues(data_box));
  function getBoxPlotValues(array) {
    for (var i = 0; i < array.length; i++) {
      var arr = array[i].slice(1).sort(function (a, b) {
        return a - b;
      });
      var max = arr[arr.length - 1];
      var min = arr[0];
      var median = getMedian(arr);
      var firstQuartile = getMedian(arr.slice(0, 4));
      var thirdQuartile = getMedian(arr.slice(3));
      array[i][8] = max;
      array[i][9] = min
      array[i][10] = firstQuartile;
      array[i][11] = median;
      array[i][12] = thirdQuartile;
    }
    return array;
  }
  function getMedian(array) {
    var length = array.length;
    if (length % 2 === 0) {
      var midUpper = length / 2;
      var midLower = midUpper - 1;
      return (array[midUpper] + array[midLower]) / 2;
    } else {
      return array[Math.floor(length / 2)];
    }
  }
  var boxPlot_options = {
    title: '',
    height: 500,
    legend: { position: 'none' },
    hAxis: {
      gridlines: { color: '#fff' }
    },
    lineWidth: 0,
    series: [{ 'color': '#D3362D' }],
    intervals: {
      barWidth: 1,
      boxWidth: 1,
      lineWidth: 2,
      style: 'boxes'
    },
    interval: {
      max: {
        style: 'bars',
        fillOpacity: 1,
        color: '#777'
      },
      min: {
        style: 'bars',
        fillOpacity: 1,
        color: '#777'
      }
    }
  };
  var boxPlot_chart = new google.visualization.LineChart(document.getElementById('boxplot_chart'));
  boxPlot_chart.draw(dataTable, boxPlot_options);

  var bar_chart = new google.charts.Bar(document.getElementById('bar_chart'));
  bar_chart.draw(data, google.charts.Bar.convertOptions(bar_chart_options));

  var line_chart = new google.charts.Line(document.getElementById('line_chart'));
  line_chart.draw(data, google.charts.Line.convertOptions(line_options));
}

  function drawsheet() {
    var queryString2 = encodeURIComponent('select F,avg(G) group by F,E');
    var query2 = new google.visualization.Query(
        ' https://docs.google.com/spreadsheets/d/19DWy_pJGP2ZbV6D3iHW5mioKRBVPkjfSUyL3twmFwB8/gviz/tq?sheet=Sheet1&headers=1&tq=' + queryString2);
    query2.send(drawChart);
  }
 

  function handleSampleDataQueryResponse(response) {
    if (response.isError()) {
      alert('Error in query: ' + response.getMessage() + ' ' + response.getDetailedMessage());
      return;
    }

    var data = response.getDataTable();
    var bar_chart_options = {
        chart: {
          title: 'Performance',
          subtitle: 'Tentris, Fuseki, and Virtuoso'          
        },
        bars: 'vertical',
        legend: { position: 'none' },
        is3D: true, 
        hAxis : {textPosition: '#ffffff'},
        height: 450,
        width:400,
        colors: ['#1b9e76', '#d95f01', '#7570b2']
    };
    var line_options = {
    chart: {
        title: 'QpS'
    },
    legend: { position: 'none' },
    width: 900,
    height: 500
    };

  var bar_chart = new google.charts.Bar(document.getElementById('bar_chart'));
  bar_chart.draw(data, google.charts.Bar.convertOptions(bar_chart_options));

  var line_chart = new google.charts.Line(document.getElementById('line_chart'));
  line_chart.draw(data, google.charts.Line.convertOptions(line_options));
}
function downloadRdf(){
  alert("downloadRDF() method called")
}

function downloadCsv(){
  alert("downloadCSV() method called")
}

function displayGraph(){
  alert("run query for " + document.querySelector('input[name = "store"]:checked').value 
      + " " + document.querySelector('input[name = "store"]:checked').id );

}
function clearGraph(){
  alert("clear() method called")
   var checks = document.querySelectorAll('#stores' + ' input[type="checkbox"]');
   for(var i =0; i< checks.length;i++){
     var check = checks[i];
     if(!check.disabled){
         check.checked = false;
 }
}
}