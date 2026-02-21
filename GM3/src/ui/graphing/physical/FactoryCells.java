package ui.graphing.physical;

import core.document.graph.PhysicalCloud;
import core.document.graph.PhysicalNic;
import core.document.graph.PhysicalNode;
import core.document.graph.PhysicalPort;
import javafx.scene.control.MenuItem;
import ui.graphing.Cell;
import ui.graphing.Visualization;

import java.util.List;

public class FactoryCells implements Visualization.INodeCellFactory<PhysicalNode> {
    public FactoryCells() {

    }

    @Override
    public Cell<PhysicalNode> uiFor(PhysicalNode node) {
        if(node instanceof PhysicalPort port) {
            return new CellPort(port);
        } else if(node instanceof PhysicalCloud cloud) {
            return new CellCloud(cloud);
        } else if(node instanceof PhysicalNic nic) {
            return new CellNic(nic);
        } else {
            return null;
        }
    }

    @Override
    public List<MenuItem> getFactoryMenuItems() {
        //TODO: Eventually menu items for the physical graph cell factory will be warranted.
        return null;
    }
}
