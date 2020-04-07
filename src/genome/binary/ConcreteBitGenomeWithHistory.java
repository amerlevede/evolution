package genome.binary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import util.Assert;

/**
 * Concrete implementation of {@link BitGenomeWithHistory}.
 * The fields and methods in this class are not implemented in BitGenomeWithHistory because they do not apply to Views.
 */
class ConcreteBitGenomeWithHistory extends BitGenomeWithHistory {

    private final List<BoolWithHistory> bits;

    public ConcreteBitGenomeWithHistory(List<BoolWithHistory> bits) {
        this.bits = bits;
    }


    @Override
    public int size() {
        return this.bits.size();
    }

    @Override
    public boolean get(int index) {
        Assert.index(this, index);
        return this.bits.get(index).b;
    }

    @Override
    public void set(int index, boolean val) {
        Assert.index(this, index);
        this.bits.get(index).b = val;
        this.transids = null;
    }

    @Override
    public void shiftLeft(int n) {
        if (n > 0) {
            for (int i=0; i<n; i++) {
                this.bits.remove(0);
                this.bits.add(new BoolWithHistory(false));
            }
        } else if (n < 0) {
            this.shiftRight(-n);
        }
        this.transids = null;
    }

    @Override
    public void shiftRight(int n) {
        if (n > 0) {
            for (int i=0; i<n; i++) {
                this.bits.remove(this.size()-1);
                this.bits.add(0, new BoolWithHistory(false));
            }
        } else if (n < 0) {
            this.shiftLeft(-n);
        }
        this.transids = null;
    }

    @Override
    public void delete(int inclusiveStart, int exclusiveEnd) {
        Assert.splice(this, inclusiveStart, exclusiveEnd);
        int len = exclusiveEnd - inclusiveStart;
        if (len >= this.size()) throw new IllegalArgumentException("Attempted to delete whole genome");

        for (int i=0; i<len; i++) {
            this.bits.remove(inclusiveStart);
        }
        this.transids = null;
    }

    @Override
    public void insert(int index, BitGenomeWithHistory g, int inclusiveStart, int exclusiveEnd) {
        Assert.splice(this, index);
        Assert.notNull(g);
        Assert.splice(g, inclusiveStart, exclusiveEnd);

        int len = exclusiveEnd - inclusiveStart;

        // Handle case when read and write Genomes refer to the same data
        if (this.refersTo() == g.refersTo()) {
            this.insert(index, g.copy(inclusiveStart, exclusiveEnd));
            return;
        }

        for (int i=0; i<len; i++) {
           this.bits.add(index+i, new BoolWithHistory(g.get(inclusiveStart+i), g.getId(inclusiveStart+i)));
        }
        this.transids = null;
    }

    @Override
    public void paste(int index, BitGenomeWithHistory g, int inclusiveStart, int exclusiveEnd) {
        int len = exclusiveEnd - inclusiveStart;
        int overwritelen = Math.min(len, this.size()-index);

        if (this.refersTo() == g.refersTo()) {
            this.paste(index, g.copy(inclusiveStart, exclusiveEnd), 0, exclusiveEnd - inclusiveStart);
            return;
        }


        for (int i=0; i<overwritelen; i++) {
            this.bits.set(index+i, new BoolWithHistory(g.get(inclusiveStart+i), g.getId(inclusiveStart+i)));
        }
        for (int i=overwritelen; i<len; i++) {
            this.bits.add(new BoolWithHistory(g.get(inclusiveStart+i), g.getId(inclusiveStart+i)));
        }

        this.transids = null;
    }

    @Override
    public long getId(int index) {
        Assert.index(this, index);
        return this.bits.get(index).id;
    }

    private Map<Long,List<Integer>> transids = null;

    private void refreshTransIds() {
        this.transids = new HashMap<>();
        for (int i=0; i<this.size(); i++) {
            Long iid = this.getId(i);
            if (this.transids.containsKey(iid)) {
                this.transids.get(iid).add(i);
            } else {
                this.transids.put(iid, new ArrayList<>(List.of(i)));
            }
        }
    }

    @Override
    public IntStream homologs(long id) {
        if (transids == null) {
            this.refreshTransIds();
        }
        return this.transids.containsKey(id)
                ? this.transids.get(id).stream().mapToInt(Integer::intValue)
                : IntStream.empty();
    }

    @Override
    public LongStream ids() {
    	if (transids == null) {
            this.refreshTransIds();
        }
        return this.transids.keySet().stream().mapToLong(i->i);
    }

    @Override
    public BitGenomeWithHistory refersTo() {
        return this;
    }

    @Override
    public String toString() {
    	return BinaryGenome.toString(this);
    }

}
