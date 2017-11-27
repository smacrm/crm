startProcessExport = function(){
    var btn = $(".doExportBtn");
    btn.data('onclick', btn.attr('onclick'));
    btn.attr('onclick', 'return false;');
    btn.find("img").show();
};

finishProcessExport = function(){
    var btn = $(".doExportBtn");
    btn.attr('onclick', btn.data('onclick'));
    btn.find("img").hide();
};

$(document).ready(function () {
    
    if($("#lineChart").length){
        var lineChartOptions = {
            maintainAspectRatio: false,
        };
        var lineChartCanvas = $("#lineChart").get(0).getContext("2d");
        var lineChartOptions = lineChartOptions;
        lineChartOptions.datasetFill = false;

        var lineChart = new Chart(lineChartCanvas, {
            type: 'line',
            data: lineChartData,
            options: lineChartOptions
        });
        //lineChart.Line(lineChartData, lineChartOptions);
    }

    //-------------
    //- PIE CHART -
    //-------------
    // Get context with jQuery - using jQuery's .get() method.
    
    if($("#pieChart").length){
        var pieOptions = {
            maintainAspectRatio: false,
        };
        var pieChartCanvas = $("#pieChart").get(0).getContext("2d");
        var pieChart = new Chart(pieChartCanvas, {
            type: 'pie',
            data: pieChartData,
            options: pieOptions,
        });
    }

    //-------------
    //- BAR CHART -
    //-------------
    if($("#barChart").length){
        var barChartOptions = {
            maintainAspectRatio: false,
            datasetFill: false
        };

        barChartData.datasets[1].fillColor = "#00a65a";
        barChartData.datasets[1].strokeColor = "#00a65a";
        barChartData.datasets[1].pointColor = "#00a65a";

        var barChartCanvas = $("#barChart").get(0).getContext("2d");
        var barChart = new Chart(barChartCanvas,{
            type: 'bar',
            data: barChartData,
            options: barChartOptions
        });
    }
    
    // resize
    var reportPanelHeight = $('#reportPanel').innerHeight();
    $('#reportPanel\\:reportChart').css({
        height: (reportPanelHeight - $('#reportPanel .page-header').innerHeight() - $('#reportPanel\\:tabview').innerHeight() - 25) + 'px', 
        overflowX: 'hidden', 
        overflowY: 'auto'});
});
