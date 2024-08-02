package eu.mister3551.smokingtracker.ui.graph;

import eu.mister3551.smokingtracker.record.DataPoint;

public interface GraphInterface {
    void onPointClick(DataPoint dataPoint, float x, float y);
}