<script src="${request.contextPath}/plugin/org.joget.marketplace.EChartMenu/lib/echarts-${element.properties.libraryVersion!}/echarts.min.js"></script>

<#if element.properties.theme! != "">
    <script src="${request.contextPath}/plugin/org.joget.marketplace.EChartMenu/lib/echarts-${element.properties.libraryVersion!}/theme/${element.properties.theme!}.js"></script>
</#if>

<#if element.properties.theme! == "custom">
    <script type="text/javascript">
        customTheme = ${element.properties.customTheme!};
        echarts.registerTheme('custom', customTheme);
    </script>
</#if>

<!--div class="echart_menu_body"-->

<#if element.properties.error! != "">
    ${element.properties.error!}
</#if>

<div id="chart-${element.properties.id!}" style="min-height: ${element.properties.chartHeight!}; max-width: ${element.properties.chartWidth!}" class="echart"></div>
    <style type="text/css">
        <#if element.properties.showTable! != "bottom" && element.properties.showTable! != "top">
            #echart-body-${element.properties.id!} .dataList .table-wrapper, #echart-datalist-${element.properties.id!} .pagebanner, #echart-datalist-${element.properties.id!} .pagelinks, #echart-datalist-${element.properties.id!} .exportlinks{
                display: none !important;
            }
        </#if>

        <#if element.properties.showFilter! != "true">
            #echart-body-${element.properties.id!} .filter_form{
                display: none !important;
            }
        </#if>

        <#if element.properties.showExportLinks! != "true">
            #echart-body-${element.properties.id!} .exportlinks{
                display: none !important;
            }
        </#if>

        /* remove border in progressive theme*/
        body:not(#login) #content > main > .datalist-body-content:not(.quickEdit) {
            border: none;
            box-shadow: none;
        }
    </style>

<script type="text/javascript">
    var dom = document.getElementById("chart-${element.properties.id!}");
    <#if element.properties.theme! != "">
        var eChart = echarts.init(dom, '${element.properties.theme!}');
    <#else>
        var eChart = echarts.init(dom);
    </#if>
    // specify chart configuration item and data
    var option = {
      <#if element.properties.title! != "">
        title: {
            text: '${element.properties.title!}'
        },
      </#if>
      <#if element.properties.showToolTip! == "true">
        tooltip: {
            show : true
        },
      </#if>
        <#if element.properties.xyaxis! == "true">
          <#if element.properties.horizontal! == "true">
            yAxis: {
          <#else>
            xAxis : {
          </#if>
                name: "${element.properties.categoryAxisLabel!}",
                data: ${element.properties.ticks!},
                type: "${element.properties.xAxisDisplayAS!}"
              <#if element.properties.xAxisBoundaryGap! == "false">
                ,boundaryGap: ${element.properties.xAxisBoundaryGap!}
              </#if>
            },
          <#if element.properties.horizontal! == "true">
            xAxis: {
          <#else>
            yAxis : {
        </#if>
              name: "${element.properties.valueAxisLabel!}",
              type: "value"
          },
        </#if>
        <#if element.properties.showLegend! != "">
          legend: {
              data: ${element.properties.legends!}
          },
        </#if>
        <#if element.properties.optionCustomization! != "">
            ${element.properties.optionCustomization!},
        </#if>

    series: ${element.properties.seriesData!}.map(function (seriesItem) {
        return Object.assign({}, seriesItem, {
            label: {
                <#if element.properties.enableDataLabels! == "true">
                    show: true,
                <#else>
                    show: false,
                </#if>
                position: 'inside',
                formatter: function (params) {
                    // Convert the value to a number
                    var currencyValue = parseFloat(params.value);
                    var userCurrencyPrefix = "${element.properties.prefix!}";
                    var userCurrencyPostfix = "${element.properties.postfix!}";
                    var formattedValue;

                    // if us formatting, prefix is not empty and disableDecimal checkbox conditions
                    <#if element.properties.useThousandSeparator! == "true">
                        <#if element.properties.style! == 'us' && (element.properties.prefix! != '' || element.properties.postfix! != '')>
                            <#if element.properties.disableDecimal! != "true">
                                formattedValue = currencyValue.toLocaleString('en-US', {style: 'decimal', minimumFractionDigits: 2});
                            <#else>
                                formattedValue = currencyValue.toLocaleString('en-US', {style: 'decimal', minimumFractionDigits: 0});
                            </#if>
                            return userCurrencyPrefix + " " + formattedValue + " " + userCurrencyPostfix; // Format the label with the currency value and thousand separator
                        </#if>

                        // if us formatting, prefix is empty and disableDecimal checkbox conditions
                        <#if element.properties.style! == 'us' && (element.properties.prefix! == '' || element.properties.postfix! == '')>
                            <#if element.properties.disableDecimal! != "true">
                                formattedValue = currencyValue.toLocaleString('en-US', {style: 'decimal', minimumFractionDigits: 2});
                            <#else>
                                formattedValue = currencyValue.toLocaleString('en-US', {style: 'decimal', minimumFractionDigits: 0});
                            </#if>
                            return formattedValue; // Format the label with the currency value and thousand separator
                        </#if>
                    <#else>
                        <#if element.properties.disableDecimal! != "true">
                            return userCurrencyPrefix + " " + currencyValue.toFixed(2) + userCurrencyPostfix;
                        <#else>
                            return userCurrencyPrefix + " " + currencyValue + " " + userCurrencyPostfix; // Handle other cases
                        </#if>
                    </#if>

                    // conditions for euro formatting
                    <#if element.properties.useThousandSeparator! == "true">
                        <#if element.properties.style! == 'euro' && (element.properties.prefix! != '' || element.properties.postfix! != '')>
                            <#if element.properties.disableDecimal! != "true">
                                formattedValue = currencyValue.toLocaleString('tr-TR', {style: 'decimal', minimumFractionDigits: 2});
                            <#else>
                                formattedValue = currencyValue.toLocaleString('tr-TR', {style: 'decimal', minimumFractionDigits: 0});
                            </#if>
                            return userCurrencyPrefix + " " + formattedValue + " " + userCurrencyPostfix; // Format the label with the currency value and thousand separator
                        </#if>

                        // if euro formatting, prefix is empty and disableDecimal checkbox conditions
                        <#if element.properties.style! == 'euro' && (element.properties.prefix! == '' || element.properties.postfix! == '')>
                            <#if element.properties.disableDecimal != "true">
                                formattedValue = currencyValue.toLocaleString('tr-TR', {style: 'decimal', minimumFractionDigits: 2});
                            <#else>
                                formattedValue = currencyValue.toLocaleString('tr-TR', {style: 'decimal', minimumFractionDigits: 0});
                            </#if>
                            return formattedValue; // Format the label with the currency value and thousand separator
                        </#if>
                    <#else>
                        <#if element.properties.disableDecimal! != "true">
                            return userCurrencyPrefix + " " + currencyValue.toFixed(2) + userCurrencyPostfix;
                        <#else>
                            return userCurrencyPrefix + " " + currencyValue + userCurrencyPostfix; // Handle other cases
                        </#if>
                    </#if>
                }
            }
        });
    })
};


    // use configuration item and data specified to show chart
    eChart.setOption(option);
    $("#chart-${element.properties.id!}").data("eChart", eChart);

    $(window).off("resize.chart-${element.properties.id}");
    $(window).on("resize.chart-${element.properties.id}", function () {
        if ($("#chart-${element.properties.id!}").data("eChart")) {
            $("#chart-${element.properties.id!}").css({
                width: "100%",
                height: "100%"
            });
            $("#chart-${element.properties.id!}").data("eChart").resize();
        } else {
            $(window).off("resize.chart-${element.properties.id}");
        }
    });

    <#if element.properties.showTable! == "bottom">
        $("#echart-body-${element.properties.id!} .dataList").detach().appendTo("#echart-body-${element.properties.id!}");
    </#if>
</script>
    ${element.properties.customChartFooter!}
</div>
