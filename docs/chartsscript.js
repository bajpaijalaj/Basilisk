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

function parseData(createGraph) {
	Papa.parse("http://127.0.0.1:3000/expected_csv.csv", {
		download: true,
		complete: function(results) {
			createGraph(results.data);
		}
	});
}

function createGraph(data) {
  var triplestoreTentris = ['Tentris'];
  var triplestoreFuseki = ['Fuseki'];
  var triplestoreVirtuso = ['Virtuoso'];
  var dbtriplestoreTentris = ['Tentris'];
  var dbtriplestoreFuseki = ['Fuseki'];
  var dbtriplestoreVirtuso = ['Virtuoso'];
  var wattriplestoreTentris = ['Tentris'];
  var wattriplestoreFuseki = ['Fuseki'];
  var wattriplestoreVirtuso = ['Virtuoso'];

  var noofclients=[];

	for (var i = 1; i < data.length; i++) {
    if(data[i][3]=="SWDF")
    {
        if(data[i][4]=="Tentris")
        {
            triplestoreTentris.push(data[i][7]);
            noofclients.push(data[i][5]);
        }
        else
        if(data[i][4]=="Fuseki")
        {
          triplestoreFuseki.push(data[i][7]);

        }
        else
        if(data[i][4]=="Virtuoso")
        {
          triplestoreVirtuso.push(data[i][7]);
        }
    }
    else{
      if(data[i][3]=="DBpedia")
    {
        if(data[i][4]=="Tentris")
        {
            dbtriplestoreTentris.push(data[i][7]);
            noofclients.push(data[i][5]);
        }
        else
        if(data[i][4]=="Fuseki")
        {
          dbtriplestoreFuseki.push(data[i][7]);

        }
        else
        if(data[i][4]=="Virtuoso")
        {
          dbtriplestoreVirtuso.push(data[i][7]);
        }
    }
    else{
      if(data[i][4]=="Tentris")
        {
            wattriplestoreTentris.push(data[i][7]);
            noofclients.push(data[i][5]);
        }
        else
        if(data[i][4]=="Fuseki")
        {
          wattriplestoreFuseki.push(data[i][7]);

        }
        else
        if(data[i][4]=="Virtuoso")
        {
          wattriplestoreVirtuso.push(data[i][7]);
        }

    }

    }

		
	}
  var bar_chart = c3.generate({
    bindto: '#bar_chart',
    data: {
        columns: [
            triplestoreTentris,
            triplestoreVirtuso,
            triplestoreFuseki
        ],
        type: 'bar'
    },
    title: {
      text: 'Performance'
    },
    axis: {
      x: {
        label: {
        
        text: 'Number of Clients',
        position: 'outer-center'
        },
        type: 'category',
        categories:  noofclients,
      },
      y: {
        label: {
          text: 'Average QPS per Client',
          position: 'outer-middle'
          }
      }
    },
    bar: {
        width: {
            ratio: 0.5 // this makes bar width 50% of length between ticks
        }
        // or
        //width: 100 // this makes bar width 100px
    },
    tooltip: {
      format: {
          title: function (d) { return "No of clients " + noofclients[d] },
      }
  }
});

var areachart1 = c3.generate({
  bindto:"#boxplot_chart1",
  data: {
      columns: [
        triplestoreTentris,
        triplestoreVirtuso,
        triplestoreFuseki
      ],
      types: {
        Tentris: 'area-spline',
        Fuseki: 'area-spline',
        Virtuoso:'area-spline'
          // 'line', 'spline', 'step', 'area', 'area-step' are also available to stack
      },
      groups: [['Tentris', 'Fuseki','Virtuoso']]
  },
  title: {
    text: 'SWDF'
  },
  axis: {
    x: {
      label: {
      
      text: 'Number of Clients',
      position: 'outer-center'
      },
      type: 'category',
      categories:  noofclients,
    },
    y: {
      label: {
        text: 'Average QPS per Client',
        position: 'outer-middle'
        }
    }
  },
  tooltip: {
    format: {
        title: function (d) { return "No of clients " + noofclients[d] },
    }
}

});
var areachart2 = c3.generate({
  bindto:"#boxplot_chart2",
  data: {
      columns: [
        dbtriplestoreTentris,
        dbtriplestoreVirtuso,
        dbtriplestoreFuseki
      ],
      types: {
        Tentris: 'area-spline',
        Fuseki: 'area-spline',
        Virtuoso:'area-spline'
          // 'line', 'spline', 'step', 'area', 'area-step' are also available to stack
      },
      groups: [['Tentris', 'Fuseki','Virtuoso']]
  },
  title: {
    text: 'DBpedia'
  },
  axis: {
    x: {
      label: {
      
      text: 'Number of Clients',
      position: 'outer-center'
      },
      type: 'category',
      categories:  noofclients,
    },
    y: {
      label: {
        text: 'Average QPS per Client',
        position: 'outer-middle'
        }
    }
  },
  tooltip: {
    format: {
        title: function (d) { return "No of clients " + noofclients[d] },
    }
}

});
var areachart3 = c3.generate({
  bindto:"#boxplot_chart3",
  data: {
      columns: [
        wattriplestoreTentris,
        wattriplestoreVirtuso,
        wattriplestoreFuseki
      ],
      types: {
        Tentris: 'area-spline',
        Fuseki: 'area-spline',
        Virtuoso:'area-spline'
          // 'line', 'spline', 'step', 'area', 'area-step' are also available to stack
      },
      groups: [['Tentris', 'Fuseki','Virtuoso']]
  },
  title: {
    text: 'WatDiv'
  },
  axis: {
    x: {
      label: {
      
      text: 'Number of Clients',
      position: 'outer-center'
      },
      type: 'category',
      categories:  noofclients,
    },
    y: {
      label: {
        text: 'Average QPS per Client',
        position: 'outer-middle'
        }
    }
  },
  tooltip: {
    format: {
        title: function (d) { return "No of clients " + noofclients[d] },
    }
}

});

var chart1 = c3.generate({
  bindto: '#line_chart1',
  title: {
    text: 'SWDF'
  },
  data: {
      x: 'x',
      columns: [
          ['x', 1, 4, 8, 16, 32],
          triplestoreTentris,
          triplestoreVirtuso,
          triplestoreFuseki
          
      ]
  },
  axis: {
    x: {
      label: {
      text: 'Number of Clients',
      position: 'outer-center'
      }
    },
    y: {
      label: {
        text: 'Average QPS',
        position: 'outer-middle'
        }
    }
  }
});
var chart2 = c3.generate({
  bindto: '#line_chart2',
  title: {
    text: 'Dbpedia'
  },
  data: {
      x: 'x',
      columns: [
          ['x', 1, 4, 8, 16, 32],
          dbtriplestoreTentris,
          dbtriplestoreVirtuso,
          dbtriplestoreFuseki
          
      ]
  },
  axis: {
    x: {
        label: {
          text: 'Number of Clients',
        position: 'outer-center'
        }
    },
    y: {
      label: {
      text: 'Average QPS',
      position: 'outer-middle'
      }
    }
  }
});
var chart3 = c3.generate({
  bindto: '#line_chart3',
  title: {
    text: 'WatDiv'
  },
  data: {
      x: 'x',
      
      columns: [
          ['x', 1, 4, 8, 16, 32],
          wattriplestoreTentris,
          wattriplestoreVirtuso,
          wattriplestoreFuseki
          
      ]
  },
  axis: {
    x: {
      label: {
        text: 'Number of Clients',
      position: 'outer-center'
      }
    },
    y: {
      label: {
        text: 'Average QPS',
        position: 'outer-middle'
        }
    }
  }
});

}
parseData(createGraph);

function downloadCsv()
{
  window.open('../expected_csv.csv', 'Download');

}