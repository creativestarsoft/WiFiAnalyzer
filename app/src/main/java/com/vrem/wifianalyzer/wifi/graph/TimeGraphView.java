/*
 *    Copyright (C) 2015 - 2016 VREM Software Development <VREMSoftwareDevelopment@gmail.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.vrem.wifianalyzer.wifi.graph;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.view.View;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.band.WiFiBand;
import com.vrem.wifianalyzer.wifi.model.WiFiData;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

class TimeGraphView {
    private final MainContext mainContext = MainContext.INSTANCE;

    private final WiFiBand wiFiBand;
    private final GraphView graphView;
    private final Map<WiFiDetail, LineGraphSeries<DataPoint>> seriesMap;
    private final GraphViewUtils graphViewUtils;
    private int scanCount;

    private TimeGraphView(@NonNull View view, int graphViewId, @NonNull Resources resources, @NonNull WiFiBand wiFiBand) {
        this.wiFiBand = wiFiBand;
        this.graphView = makeGraphView(view, graphViewId, resources);
        this.seriesMap = new TreeMap<>();
        this.graphViewUtils = new GraphViewUtils(graphView, seriesMap, mainContext.getSettings().getTimeGraphLegend());
        this.scanCount = 0;
    }

    static TimeGraphView make2(@NonNull View view, Resources resources) {
        return new TimeGraphView(view, R.id.timeGraph2, resources, WiFiBand.GHZ_2);
    }

    static TimeGraphView make5(@NonNull View view, @NonNull Resources resources) {
        return new TimeGraphView(view, R.id.timeGraph5, resources, WiFiBand.GHZ_5);
    }

    private GraphView makeGraphView(@NonNull View view, int graphViewId, @NonNull Resources resources) {
        return new GraphViewBuilder(view, graphViewId)
                .setLabelFormatter(new TimeAxisLabel())
                .setVerticalTitle(resources.getString(R.string.graph_axis_y))
                .setHorizontalTitle(resources.getString(R.string.graph_time_axis_x))
                .build();
    }

    void update(@NonNull WiFiData wiFiData) {
        Set<WiFiDetail> newSeries = new TreeSet<>();
        for (WiFiDetail wiFiDetail : wiFiData.getWiFiDetails(wiFiBand, mainContext.getSettings().getSortBy())) {
            addData(newSeries, wiFiDetail);
        }
        graphViewUtils.updateSeries(newSeries);
        graphViewUtils.updateLegend(mainContext.getSettings().getTimeGraphLegend());
        graphViewUtils.setVisibility(wiFiBand);
        scanCount++;
    }

    private void addData(@NonNull Set<WiFiDetail> newSeries, @NonNull WiFiDetail wiFiDetail) {
        newSeries.add(wiFiDetail);
        LineGraphSeries<DataPoint> series = seriesMap.get(wiFiDetail);
        if (series == null) {
            series = new LineGraphSeries<>();
            setSeriesOptions(series, wiFiDetail);
            graphView.addSeries(series);
            seriesMap.put(wiFiDetail, series);
        }
        series.appendData(new DataPoint(scanCount, wiFiDetail.getWiFiSignal().getLevel()), true, scanCount + 1);
        graphViewUtils.setOnDataPointTapListener(series);
    }

    private void setSeriesOptions(@NonNull LineGraphSeries<DataPoint> series, @NonNull WiFiDetail wiFiDetail) {
        series.setColor(graphViewUtils.getColor().getPrimary());
        series.setDrawBackground(false);
        series.setTitle(graphViewUtils.getTitle(wiFiDetail));
    }
}
