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
            series: ${element.properties.seriesData!}
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