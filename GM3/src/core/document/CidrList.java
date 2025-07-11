package core.document;

import javafx.collections.FXCollections;
import javafx.collections.ModifiableObservableListBase;
import util.Cidr;

import java.util.concurrent.CopyOnWriteArrayList;

public class CidrList extends ModifiableObservableListBase<Cidr> {
    private final java.util.List<Cidr> backingList;

    public CidrList() {
        this.backingList = new CopyOnWriteArrayList<>();
    }

    @Override
    public Cidr get(int index) {
        return backingList.get(index);
    }

    @Override
    public int size() {
        return backingList.size();
    }

    @Override
    protected void doAdd(int index, Cidr element) {
        if(element == null) {
            return;
        }
        for(Cidr cidr : this.backingList) {
            if(cidr.overlaps(element)) {
                return;
            }
        }
        backingList.add(index, element);
    }

    @Override
    protected Cidr doRemove(int index) {
        return backingList.remove(index);
    }

    @Override
    protected Cidr doSet(int index, Cidr element) {
        return backingList.set(index, element);
    }
}
