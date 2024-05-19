package org.joget.marketplace;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.apps.app.dao.DatalistDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListActionResult;
import org.joget.apps.datalist.model.DataListCollection;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListColumnFormat;
import org.joget.apps.datalist.service.DataListService;
import org.joget.apps.userview.model.Userview;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.apps.userview.service.UserviewUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

public class EChartMenu extends UserviewMenu implements PluginWebSupport {

    private DataList cacheDataList = null;
    private final static String MESSAGE_PATH = "messages/EChartMenu";

    @Override
    public String getName() {
        return "EChart Menu";
    }

    @Override
    public String getVersion() {
        return "7.0.6";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getLabel() {
        //support i18n
        return AppPluginUtil.getMessage("org.joget.marketplace.EChartMenu.pluginLabel", getClassName(), MESSAGE_PATH);
    }

    @Override
    public String getDescription() {
        //support i18n
        return AppPluginUtil.getMessage("org.joget.marketplace.EChartMenu.pluginDesc", getClassName(), MESSAGE_PATH);
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClassName(), "/properties/EChartMenu.json", null, true, MESSAGE_PATH);
    }

    @Override
    public String getCategory() {
        return "Marketplace";
    }

    @Override
    public String getIcon() {
        return "<i class=\"fas fa-chart-bar\"></i>";
    }

    @Override
    public boolean isHomePageSupported() {
        return true; // Can use as first page of the userview
    }

    @Override
    public String getDecoratedMenu() {
        return null; // using default
    }

    protected DataList getDataList() throws BeansException {
        if (cacheDataList == null) {
            // get datalist
            ApplicationContext ac = AppUtil.getApplicationContext();
            AppService appService = (AppService) ac.getBean("appService");
            DataListService dataListService = (DataListService) ac.getBean("dataListService");
            DatalistDefinitionDao datalistDefinitionDao = (DatalistDefinitionDao) ac.getBean("datalistDefinitionDao");
            String id = getPropertyString("datalistId");
            AppDefinition appDef = appService.getAppDefinition(getRequestParameterString("appId"), getRequestParameterString("appVersion"));
            DatalistDefinition datalistDefinition = datalistDefinitionDao.loadById(id, appDef);

            if (datalistDefinition != null) {
                cacheDataList = dataListService.fromJson(datalistDefinition.getJson());

                if (getPropertyString(Userview.USERVIEW_KEY_NAME) != null && getPropertyString(Userview.USERVIEW_KEY_NAME).trim().length() > 0) {
                    cacheDataList.addBinderProperty(Userview.USERVIEW_KEY_NAME, getPropertyString(Userview.USERVIEW_KEY_NAME));
                }
                if (getKey() != null && getKey().trim().length() > 0) {
                    cacheDataList.addBinderProperty(Userview.USERVIEW_KEY_VALUE, getKey());
                }

                cacheDataList.setActionPosition(getPropertyString("buttonPosition"));
                cacheDataList.setSelectionType(getPropertyString("selectionType"));
                cacheDataList.setCheckboxPosition(getPropertyString("checkboxPosition"));
            }
        }
        return cacheDataList;
    }

    @Override
    public String getRenderPage() {
        Map freeMarkerModel = new HashMap();
        freeMarkerModel.put("request", getRequestParameters());
        freeMarkerModel.put("element", this);

        //build filters and datalist table
        String datalistContent = "";
        datalistContent = getDatalistHTML();
        datalistContent = datalistContent.substring(0, datalistContent.length() - 8);

         DataList datalist = getDataList();
        if (datalist != null && datalist.getRows().size() > 0) {
            getBinderData(datalist);
        } else {
            LogUtil.info(getClass().getName(), "Datalist Content: " + datalistContent);
            return "<div>No data available</div>"; 
        }
        if (datalist != null) {
            getBinderData(datalist);
        }

        String libraryVersion = "";

        if (getPropertyString("libraryVersion") != null && !getPropertyString("libraryVersion").isEmpty()) {
            libraryVersion = getPropertyString("libraryVersion");
            setProperty("libraryVersion", libraryVersion);
        } else {
            //use specific default version
            setProperty("libraryVersion", "5.1.2");
        }

        //build chart
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        String content = pluginManager.getPluginFreeMarkerTemplate(freeMarkerModel, getClass().getName(), "/templates/EChart.ftl", MESSAGE_PATH);

        //combine both
        return "<div id=echart-body-" + getPropertyString("id") + " class=\"chart-body-content\">" + datalistContent + content + "</div>";
    }

    protected String getDatalistHTML() {
        Map model = new HashMap();
        model.put("requestParameters", getRequestParameters());

        try {
            // get data list
            DataList dataList = getDataList();

            if (dataList != null) {
                //overide datalist result to use userview result
                DataListActionResult ac = dataList.getActionResult();
                if (ac != null) {
                    if (ac.getMessage() != null && !ac.getMessage().isEmpty()) {
                        setAlertMessage(ac.getMessage());
                    }
                    if (ac.getType() != null && DataListActionResult.TYPE_REDIRECT.equals(ac.getType())
                            && ac.getUrl() != null && !ac.getUrl().isEmpty()) {
                        if ("REFERER".equals(ac.getUrl())) {
                            HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
                            if (request != null && request.getHeader("Referer") != null) {
                                setRedirectUrl(request.getHeader("Referer"));
                            } else {
                                setRedirectUrl("REFERER");
                            }
                        } else {
                            if (ac.getUrl().startsWith("?")) {
                                ac.setUrl(getUrl() + ac.getUrl());
                            }
                            setRedirectUrl(ac.getUrl());
                        }
                    }
                }

                // set data list
                setProperty("dataList", dataList);
            } else {
                setProperty("error", "Data List \"" + getPropertyString("datalistId") + "\" not exist.");
            }
        } catch (BeansException ex) {
            StringWriter out = new StringWriter();
            ex.printStackTrace(new PrintWriter(out));
            String message = ex.toString();
            message += "\r\n<pre class=\"stacktrace\">" + out.getBuffer() + "</pre>";
            setProperty("error", message);
        }

        Map properties = getProperties();
        model.put("properties", properties);

        String result = UserviewUtil.renderJspAsString("userview/plugin/datalist.jsp", model);
        return result;
    }

    protected void getBinderData(DataList datalist) {
        DataListCollection binderdata;
        DataListColumn[] columns;

        try {

            if (getPropertyString("chartUseAllDataRows").equalsIgnoreCase("true")) {
                binderdata = datalist.getRows(datalist.getTotal(), 0);
            } else {
                binderdata = datalist.getRows();
            }

            columns = datalist.getColumns();
        } catch (Exception e) {
            LogUtil.error(EChartMenu.class.getName(), e, "Not able to retrieve data from binder");
            setProperty("error", ResourceBundleUtil.getMessage("userview.sqlchartmenu.error.invalidData"));
            return;
        }

        if (binderdata != null && !binderdata.isEmpty()) {
            try {
                if (check(getPropertyString("chartType"), new String[]{"pie"})) {
                    JSONArray legend = new JSONArray();
                    JSONArray seriesArray = new JSONArray();
                    JSONArray seriesData = new JSONArray();
                    JSONObject series = new JSONObject();

                    String chartType = getPropertyString("chartType");
                    String seriesCustomization = getProperty("seriesCustomization").toString();
                    String showValueLabel = getProperty("showValueLabel").toString();

                    if (!seriesCustomization.isEmpty()) {
                        series = new JSONObject(seriesCustomization);
                    }

                    if (showValueLabel.equalsIgnoreCase("true")
                            && seriesCustomization.isEmpty()) {
                        JSONObject labelObj = new JSONObject("{ show: true, formatter: '{b} {c} ({d}%)' }");
                        series.put("label", labelObj);
                    }

                    series.put("type", chartType);

                    for (Object r : binderdata) {
                        JSONObject dataPoint = new JSONObject();
                        String mappingValue = getPropertyString("mapping_value");
                        //String seriesName = getColumnLabel(mappingValue, columns);
                        String name = getBinderFormattedValue(datalist, r, getPropertyString("mapping_x"));

                        legend.put(name);

                        dataPoint.put("name", name);
                        dataPoint.put("value", getBinderFormattedValue(datalist, r, mappingValue));
                        seriesData.put(dataPoint);
                    }

                    series.put("data", seriesData);
                    seriesArray.put(series);
                    setProperty("seriesData", seriesArray.toString());
                    setProperty("ticks", legend.toString());
                    setProperty("legends", legend.toString());

                } else if (check(getPropertyString("chartType"), new String[]{"bar", "xy", "area", "line", "donut", "custom"})) {
                    String chartType = getPropertyString("chartType");

                    if (check(chartType, new String[]{"bar", "xy", "area", "line"})) {
                        setProperty("xyaxis", "true");
                    }
                    Object[] mapping_values = (Object[]) getProperty("mapping_values");

                    JSONArray seriesArray = new JSONArray();
                    int seriesCount = 0;

                    if (mapping_values.length == 0) {
                        Collection col = new ArrayList();
                        //treat each column in the dataset as a series other than the column chosen as X-axis
                        for (DataListColumn c : columns) {
                            if (!c.getName().equalsIgnoreCase(getPropertyString("mapping_x"))) {
                                HashMap m = new HashMap();
                                m.put("value", c.getName());
                                m.put("customization", "");
                                m.put("showValueLabel", "true");
                                col.add(m);
                            }
                        }
                        mapping_values = col.toArray();
                    }

                    //generate series data
                    for (int i = 0; i < mapping_values.length; i++) {
                        Map mapping = (HashMap) mapping_values[i];
                        String seriesName = getColumnLabel(mapping.get("value").toString(), columns);

                        //String legend = mapping.get("legend").toString();
                        String customization = mapping.get("customization").toString();
                        String showValueLabel = mapping.get("showValueLabel").toString();

                        JSONObject series = new JSONObject();

                        if (!customization.isEmpty()) {
                            series = new JSONObject(customization);
                        }

                        series.put("name", seriesName);

                        if (showValueLabel.equalsIgnoreCase("true") && customization.isEmpty()) {
                            if (check(getPropertyString("chartType"), new String[]{"donut"})) {
                                JSONObject labelObj = new JSONObject("{ show: true, formatter: '{b} {c} ({d}%)' }");
                                if (series.has("label")) {
                                    series.put("label", deepMerge(labelObj, (JSONObject) series.get("label")));
                                } else {
                                    series.put("label", labelObj);
                                }
                            } else {
                                JSONObject labelObj = new JSONObject("{ show: true }");
                                if (series.has("label")) {
                                    series.put("label", deepMerge(labelObj, (JSONObject) series.get("label")));
                                } else {
                                    series.put("label", labelObj);
                                }
                            }
                        }

                        if (getPropertyString("chartType").equalsIgnoreCase("donut") && customization.isEmpty()) {
                            //apply donut styling to separate the series if no customization set
                            chartType = "pie";

                            JSONArray radius = new JSONArray();
                            JSONObject labelObj;
                            switch (seriesCount) {
                                case 0:
                                    radius.put("0%");
                                    radius.put("30%");

                                    if (series.has("radius")) {
                                        series.put("radius", deepMerge(series, new JSONObject(radius)));
                                    } else {
                                        series.put("radius", radius);
                                    }

                                    labelObj = new JSONObject("{ position : 'inside', show: true }");
                                    if (series.has("label")) {
                                        series.put("label", deepMerge(series.getJSONObject("label"), labelObj));
                                    } else {
                                        series.put("label", labelObj);
                                    }
                                    break;
                                case 1:
                                    radius.put("40%");
                                    radius.put("70%");

                                    if (series.has("radius")) {
                                        series.put("radius", deepMerge(series, new JSONObject(radius)));
                                    } else {
                                        series.put("radius", radius);
                                    }

                                    labelObj = new JSONObject("{ position : 'outside', show: true }");
                                    if (series.has("label")) {
                                        series.put("label", deepMerge(series.getJSONObject("label"), labelObj));
                                    } else {
                                        series.put("label", labelObj);
                                    }
                                    break;
                                case 2:
                                    radius.put("80%");
                                    radius.put("100%");

                                    if (series.has("radius")) {
                                        series.put("radius", deepMerge(series, new JSONObject(radius)));
                                    } else {
                                        series.put("radius", radius);
                                    }

                                    labelObj = new JSONObject("{label : { position : 'outside', show: true }}");
                                    if (series.has("label")) {
                                        series.put("label", deepMerge(series, labelObj));
                                    } else {
                                        series.put("label", labelObj);
                                    }
                                    break;
                                default:
                                    break;
                            }
                        } else if (getPropertyString("chartType").equalsIgnoreCase("donut")) {
                            chartType = "pie";
                        } else if (getPropertyString("chartType").equalsIgnoreCase("area")) {
                            chartType = "line";
                            series.put("areaStyle", new JSONObject());
                        }

                        if (chartType.equalsIgnoreCase("custom") && series.has("type")) {
                            if (series.getString("type").equalsIgnoreCase("line")) {
                                setProperty("xyaxis", "true");
                            }
                        } else {
                            series.put("type", chartType);
                        }

                        if (getPropertyString("smoothLine").equalsIgnoreCase("true") && customization.isEmpty()) {
                            series.put("smooth", true);
                        }

                        JSONArray seriesData = new JSONArray();
                        for (Object r : binderdata) {
                            String y = getBinderFormattedValue(datalist, r, mapping.get("value").toString());

                            if (getPropertyString("chartType").equalsIgnoreCase("donut")) {
                                String x = getBinderFormattedValue(datalist, r, getPropertyString("mapping_x"));
                                JSONObject dataPoint = new JSONObject();
                                dataPoint.put("name", x);
                                dataPoint.put("value", y);
                                seriesData.put(dataPoint);
                            } else {
                                seriesData.put(y);
                            }
                        }

                        series.put("data", seriesData);
                        seriesArray.put(series);
                        seriesCount++;
                    }
                    setProperty("seriesData", seriesArray.toString());

                    //generate legends
                    JSONArray legends = new JSONArray();
                    for (int i = 0; i < mapping_values.length; i++) {
                        Map mapping = (HashMap) mapping_values[i];
                        String seriesName = getColumnLabel(mapping.get("value").toString(), columns);
                        legends.put(seriesName);
                    }
                    setProperty("legends", legends.toString());

                    //generate ticks
                    JSONArray ticks = new JSONArray();
                    for (Object r : binderdata) {
                        String legendString = getBinderFormattedValue(datalist, r, getPropertyString("mapping_x"));
                        ticks.put(legendString);
                    }
                    setProperty("ticks", ticks.toString());
                }
            } catch (JSONException e) {
                LogUtil.error(EChartMenu.class.getName(), e, "Not able to render chart data");
                setProperty("error", AppPluginUtil.getMessage("userview.EChart.error.chartRendering", getClassName(), MESSAGE_PATH));
            }
        } else {
            setProperty("error", ResourceBundleUtil.getMessage("userview.processStatus.noData"));
        }
    }

    protected String getBinderFormattedValue(DataList dataList, Object o, String name) {
        DataListColumn[] columns = dataList.getColumns();
        for (DataListColumn c : columns) {
            if (c.getName().equalsIgnoreCase(name)) {
                String value;
                try {
                    value = DataListService.evaluateColumnValueFromRow(o, name).toString();
                    Collection<DataListColumnFormat> formats = c.getFormats();
                    if (formats != null) {
                        for (DataListColumnFormat f : formats) {
                            if (f != null) {
                                value = f.format(dataList, c, o, value);
                                return value;
                            } else {
                                return value;
                            }
                        }
                    } else {
                        return value;
                    }
                } catch (Exception ex) {

                }
            }
        }
        return "";
    }

    protected boolean check(String value, String[] list) {
        List<String> valueList = new ArrayList<>();
        valueList.addAll(Arrays.asList(list));
        return valueList.contains(value);
    }

    /**
     * Merge "source" into "target".If fields have equal name, merge them
     * recursively.
     *
     * @param source
     * @param target
     * @return the merged object (target).
     * @throws org.json.JSONException
     */
    public static JSONObject deepMerge(JSONObject source, JSONObject target) throws JSONException {
        for (String key : JSONObject.getNames(source)) {
            Object value = source.get(key);
            if (!target.has(key)) {
                // new value for "key":
                target.put(key, value);
            } else {
                // existing value for "key" - recursively deep merge:
                if (value instanceof JSONObject) {
                    JSONObject valueJson = (JSONObject) value;
                    deepMerge(valueJson, target.getJSONObject(key));
                } else {
                    target.put(key, value);
                }
            }
        }
        return target;
    }

    protected String getColumnLabel(String columnName, DataListColumn[] columns) {
        for (DataListColumn column : columns) {
            if (column.getName().equalsIgnoreCase(columnName)) {
                return column.getLabel();
            }
        }
        return "";
    }

    @Override
    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        boolean isAdmin = WorkflowUtil.isCurrentUserInRole(WorkflowUserManager.ROLE_ADMIN);
        if (!isAdmin) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String action = request.getParameter("action");
        if ("getDatalistColumns".equals(action)) {
            try {
                ApplicationContext ac = AppUtil.getApplicationContext();
                AppDefinition appDef = AppUtil.getCurrentAppDefinition();
                DatalistDefinitionDao datalistDefinitionDao = (DatalistDefinitionDao) ac.getBean("datalistDefinitionDao");
                DataListService dataListService = (DataListService) ac.getBean("dataListService");

                String datalistId = request.getParameter("id");
                DatalistDefinition datalistDefinition = datalistDefinitionDao.loadById(datalistId, appDef);

                DataList datalist;
                if (datalistDefinition != null) {
                    datalist = dataListService.fromJson(datalistDefinition.getJson());
                    DataListColumn[] datalistcolumns = datalist.getColumns();

                    //JSONObject jsonObject = new JSONObject();
                    JSONArray columns = new JSONArray();
                    for (int i = 0; i < datalistcolumns.length; i++) {
                        JSONObject column = new JSONObject();
                        column.put("value", datalistcolumns[i].getName());
                        column.put("label", datalistcolumns[i].getLabel());
                        columns.put(column);
                    }
                    columns.write(response.getWriter());
                } else {
                    JSONArray columns = new JSONArray();
                    columns.write(response.getWriter());
                }

            } catch (IOException | JSONException | BeansException e) {
                LogUtil.error(EChartMenu.class.getName(), e, "Webservice getColumns");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }
}
